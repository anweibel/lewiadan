package utils.esr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.pattern.LogEvent;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import models.Import;
import models.Konto;
import play.libs.XPath;

/**
 * Liest ESR-Files der Post und schreibt sie in die Datenbank.
 * 
 */
public class CamtFileReader {

	private static Logger logger = Logger.getLogger(CamtFileReader.class);
	private static String logFile = "ESRInsert.log";
	private static String statisikFilePath = "ESRStatistik.csv";
	public static boolean errorOccured = false;
	BufferedWriter statisikFile;

    
	private Document loadXML(File file) throws IOException, ParserConfigurationException, SAXException {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		return builder.parse(file);
	}
	
	private ZahlungResult getZahlungen(File file, Import currentImport)
			throws IOException {

		FileWriter fstream = new FileWriter(statisikFilePath, true);
		statisikFile = new BufferedWriter(fstream);
		
		ArrayList<EsrZahlung> zahlungen = new ArrayList<EsrZahlung>();

		String warnings = "";
		
		try {
			
			Document camtFile = loadXML(file);
			
			String kundenNrStr = XPath.selectText("Document//BkToCstmrDbtCdtNtfctn//Ntfctn//Ntry//NtryRef", camtFile);
			logger.warn("kundenNrStr=" + kundenNrStr);
			long kundenNr = Long.parseLong(kundenNrStr);
			Konto konto = Konto.find("kundennr",kundenNr).first();
				
			for(Node zahlung : XPath.selectNodes("Document//BkToCstmrDbtCdtNtfctn//Ntfctn//Ntry//NtryDtls//TxDtls", camtFile)) {
			    
				EsrZahlung esrZahlung = new EsrZahlung();
				esrZahlung.setImport(currentImport);
				
				
				String amount = XPath.selectText("Amt", zahlung);
			    String ref = XPath.selectText("RmtInf//Strd//CdtrRefInf//Ref", zahlung);

				esrZahlung.setKonto(konto); // PC-Konto
				
				String gutschriftDatum = XPath.selectText("RltdDts//AccptncDtTm", zahlung);;
				String gutschriftDatumSQL = gutschriftDatum.substring(0, 10);
				esrZahlung.setDatum(java.sql.Date.valueOf(gutschriftDatumSQL));
				
				String sZahlerID = ref.substring(0, ref.length() - 7);
				logger.warn("zahlerID=" + sZahlerID);
				long zahlerID = Long.parseLong(sZahlerID);
				esrZahlung.setZahlerID(zahlerID);
				
				String versandID = StringUtils.right(ref,7).substring(0,6);
				logger.warn("versandID=" + versandID);
				esrZahlung.setVersandID(versandID);
				
				if (!(versandID.substring(0, 2).equals("20") || (versandID
						.equals("143")))) {

					errorOccured = true;
					logger.warn("Die folgende Referenznummer konnte keinem Versand zugeordnet werden: " + ref);
					warnings += "Die folgende Referenznummer konnte keinem Versand zugeordnet werden: " + ref + "\n";
					logger.warn("Betrag: " + amount + "; Datum: " + gutschriftDatum);
					warnings += "Betrag: " + amount + "; Datum: " + gutschriftDatum + "\n";
					continue;
				}
				
				double betrag = Double.parseDouble(amount) / 100;
				esrZahlung.setBetrag(betrag);
				
				java.util.Date today = new java.util.Date();
				esrZahlung.setBemerkung("ESR-Versand '" + versandID + "'. Eingefuegt am "
						+ DateFormat.getInstance().format(today));
				
				addToStatistik(esrZahlung);
				zahlungen.add(esrZahlung);
			    
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (ParserConfigurationException e) {
			logger.error(e.getMessage());
		} catch (SAXException e) {
			logger.error(e.getMessage());
		}
		
		ZahlungResult result = new ZahlungResult();
		result.setZahlungen(zahlungen);
		result.setWarnings(warnings);;
		return result;
	}

	private void addToStatistik(EsrZahlung zahlung) throws IOException {

		statisikFile.write(zahlung.getDatum() + ";" + zahlung.getVersandID()
				+ ";" + zahlung.getBetrag());
		statisikFile.newLine();
	}

	public String processFiles(List<File> files)
			throws Exception {
		try {

			errorOccured = false;
			String answer = "Folgende Probleme sind aufgetreten:<br/>\n";
			prepareLogger();
			DBConnector connector = new DBConnector();
			int counter = 0;

			for (File file : files) {

				logger.debug("Start processing " + file.getAbsolutePath());
				
				MessageDigest md = MessageDigest.getInstance("MD5");
				RandomAccessFile f = new RandomAccessFile(file.getAbsolutePath(), "r");
				byte[] fileContent = new byte[(int)f.length()];
				f.read(fileContent);
				md.update(fileContent);
				byte[] digest = md.digest();
				String md5 = Arrays.toString(digest);
				
				Import earlierImport = Import.find("md5", md5).first();
				if(earlierImport != null){
					answer += "Diese Datei wurde bereits eingelesen, und zwar um " + earlierImport.created;
					logger.error(answer);
					f.close();
					return answer;
				}
				
				logger.debug("Max(id) from zahlungen = "
						+ connector.maxDBIdZahlung());

				Import currentImport = new Import();
				currentImport.created = new Date();
				currentImport.type = "esr";
				currentImport.md5 = md5;
				currentImport.save();
				
				ZahlungResult zahlungResult = getZahlungen(file, currentImport);

				Collection<EsrZahlung> zahlungen = zahlungResult.getZahlungen();
				answer += zahlungResult.getWarnings();
				
				for (EsrZahlung z : zahlungen) {

					Zahler zahler;
					try {
						logger.debug("==============================");
						zahler = connector.getZahler(z.getZahlerID());
					} catch (Exception e) {
						errorOccured = true;
						answer += "Zahler mit der ID "
								+ z.getZahlerID()
								+ " in der Datenbank nicht gefunden!<br/>\nEine Zahlung von "
								+ z.getBetrag() + " am " + z.getDatum()
								+ " konnte niemandem zugeordnet werden.<br/>\n";
						logger.error("Zahler mit der ID " + z.getZahlerID()
								+ " in der Datenbank nicht gefunden!");
						logger.debug("==============================");
						continue;
					}
					logger.debug("==============================");
					
					String bemerkungen = connector.zahlungAlreadyExists(z);
					if (bemerkungen != null) {
						
						String text = "\n<br/>Zahlung das Zahlers mit der ID " + z.getZahlerID()
								+ " mit dem Betrag " + z.getBetrag() + " am " + z.getDatum()
								+ " bereits vorhanden, und zwar mit der/den folgende Bemerkungen: " + bemerkungen
								+ "\nZahlung dennoch importiert.<br/>\n"; 
						
						errorOccured = true;
						
						logger.error(text);
						
						answer += text;
					}
					logger.debug("Zahler mit der ID " + z.getZahlerID()
							+ " ist:" + zahler.toString());
					Long zahlungsCodeID = getZahlungsCodeId(zahler
							.getMitgliedTyp());
					z.setZahlungsCodeID(zahlungsCodeID);
					logger.debug("Inserting Zahlung: " + z.toString());
					logger.debug("==============================");
					connector.insertZahlung(z);
					counter++;
				}

				logger.debug("Finished processing " + file.getAbsolutePath());
				statisikFile.close();
			}
			logger.debug("processFiles finished.");

			if (answer.endsWith("aufgetreten:<br/>\n")) {
				answer = "Es wurden " + counter
						+ " Zahlungen in die Datenbank übernommen.";
				if (errorOccured)
					answer += " Es sind jedoch Probleme aufgetreten. Mehr Informationen dazu in der Logdatei "
							+ logFile;
			} else {
				answer += "Mehr Informationen dazu in der Logdatei " + logFile;
				answer += ".<br>\n"
						+ counter
						+ " Zahlungen wurden trotzdem erfolgreich in die Datenbank übernommen.";
			}

			logger.debug("Answer = " + answer);
			logger.debug("Max(id) from zahlungen = "
					+ connector.maxDBIdZahlung());
			return answer;

		} catch (Exception e) {
			logger.error(e.toString());
			e.printStackTrace();
			throw e;
		}
	}

	// Alles hardcodiert... :-(
	private Long getZahlungsCodeId(String mitgliedTyp) {

		// Standard: Spende
		Long zahlungsCodeID = Long.valueOf(1);

		if (mitgliedTyp == null)
			return zahlungsCodeID;

		// Mitglied
		if (mitgliedTyp.equals("C"))
			zahlungsCodeID = Long.valueOf(4);

		// Abo
		if (mitgliedTyp.equals("D"))
			zahlungsCodeID = Long.valueOf(3);

		return zahlungsCodeID;
	}

	private void prepareLogger() {
		PatternLayout layout = new PatternLayout(
				"%d{ISO8601} %-5p [%t] %c: %m%n");

		try {
			FileAppender fileAppender;
			fileAppender = new FileAppender(layout, logFile);
			logger.addAppender(fileAppender);
		} catch (IOException e) {
			e.printStackTrace();
		}

		logger.setLevel(Level.ALL);

		logger.debug("Logger initialized");
	}
}
