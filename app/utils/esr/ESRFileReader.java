package utils.esr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
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

import models.Import;
import models.Konto;

import org.apache.commons.lang.CharUtils;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * Liest ESR-Files der Post und schreibt sie in die Datenbank.
 * 
 */
public class ESRFileReader {

	private static Logger logger = Logger.getLogger(ESRFileReader.class);
	private static String logFile = "ESRInsert.log";
	private static String statisikFilePath = "ESRStatistik.csv";
	public static boolean errorOccured = false;
	BufferedWriter statisikFile;

	private BufferedReader setFile(String pfad) throws IOException {

		try {

			FileWriter fstream = new FileWriter(statisikFilePath, true);
			statisikFile = new BufferedWriter(fstream);

			// FileReader always assumes default encoding is OK!
			return new BufferedReader(new FileReader(pfad));

		} catch (IOException ex) {
			logger.error(ex.toString());
			throw ex;
		}
	}

	private ZahlungResult getZahlungen(BufferedReader brInputFile, Import currentImport)
			throws IOException {

		ArrayList<EsrZahlung> zahlungen = new ArrayList<EsrZahlung>();

		String warnings = "";
		
		Collection<String> lines = getLines(brInputFile);
		for (String line : lines) {
			String txCode = line.substring(0, 3);
			
			logger.warn("line:[" + line + "]");
			
			if (!isKnownTxCode(txCode)) {

				if (txCode.equals("005")) {
					errorOccured = true;
					logger.warn("Ohalätz, ein Stornorecord:");
					logger.warn(line);
					warnings += "Ohalätz, ein Stornorecord:<br/>" + line + "<br/>";
				} else if (txCode.equals("999")) {
					logger.warn("Totalrecord:");
					logger.warn(line);
				} else {
					errorOccured = true;
					logger.warn("UnknownTxCode (" + txCode + ") on this line:");
					logger.warn(line);
					warnings += "UnknownTxCode (" + txCode + ") on this line:<br/>" + line + "<br/>";
				}
				continue;
			}

			EsrZahlung zahlung = new EsrZahlung();

			String kundenNrStr = line.substring(3, 12);
			long kundenNr = Long.parseLong(kundenNrStr);
			String sZahlerID = line.substring(18, 32);
			long zahlerID = Long.parseLong(sZahlerID);
			zahlung.setZahlerID(zahlerID);

			String versandID = line.substring(32, 38);

			// Spezialfall wegen Issue1
			if (versandID.endsWith("2010")) {
				versandID = "2010";
				sZahlerID = line.substring(18, 34);
				zahlerID = Long.parseLong(sZahlerID);
				zahlung.setZahlerID(zahlerID);
			}

			if (versandID.endsWith("143")) {
				versandID = "143";
				sZahlerID = line.substring(18, 35);
				zahlerID = Long.parseLong(sZahlerID);
				zahlung.setZahlerID(zahlerID);
			}

			zahlung.setVersandID(versandID);

			String zBetrag = line.substring(39, 49);
			double betrag = Double.parseDouble(zBetrag) / 100;
			zahlung.setBetrag(betrag);

			String gutschriftDatum = line.substring(71, 77);
			String gutschriftDatumSQL = "20" + gutschriftDatum.substring(0, 2)
					+ "-" + gutschriftDatum.substring(2, 4) + "-"
					+ gutschriftDatum.substring(4, 6);
			zahlung.setDatum(java.sql.Date.valueOf(gutschriftDatumSQL));

			Konto konto = Konto.find("kundennr",kundenNr).first();
			zahlung.setKonto(konto); // PC-Konto

			java.util.Date today = new java.util.Date();
			zahlung.setBemerkung("ESR-Versand '" + versandID + "'. Eingefuegt am "
					+ DateFormat.getInstance().format(today));
			
			zahlung.setVersandID(versandID);
			
			zahlung.setImport(currentImport);

			if (!(versandID.substring(0, 2).equals("20") || (versandID
					.equals("143")))) {

				errorOccured = true;
				logger.warn("Die folgende Referenznummer konnte keinem Versand zugeordnet werden: " + line.substring(18, 38));
				warnings += "Die folgende Referenznummer konnte keinem Versand zugeordnet werden: " + line.substring(18, 38) + "\n";
				logger.warn("Betrag: " + betrag + "; Datum: " + gutschriftDatum);
				warnings += "Betrag: " + betrag + "; Datum: " + gutschriftDatum + "\n";
				logger.warn(line);
				continue;
			}

			// ZahlungscodeID wird in processFiles() ergänzt.

			addToStatistik(zahlung);
			zahlungen.add(zahlung);

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

	private boolean isKnownTxCode(String txCode) {
		boolean ret = false;
		if (txCode.equals("102") || txCode.equals("112")
				|| txCode.equals("002") || txCode.equals("012"))
			ret = true;
		return ret;
	}

	private Collection<String> getLines(BufferedReader brInputFile)
			throws IOException {

		ArrayList<String> list = new ArrayList<String>();

		try {
			int len = 128;
			char[] buffer = new char[len];
			while (brInputFile.ready()) {
				brInputFile.read(buffer, 0, len);
				if(!CharUtils.isAsciiNumeric(buffer[0])){
					continue;
				}
				String line = new String(buffer);
				list.add(line);
			}
		} finally {
			brInputFile.close();
		}
		return list;
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
				
				BufferedReader brInputFile = setFile(file.getAbsolutePath());
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
					return answer;
				}
				
				logger.debug("Max(id) from zahlungen = "
						+ connector.maxDBIdZahlung());

				Import currentImport = new Import();
				currentImport.created = new Date();
				currentImport.type = "esr";
				currentImport.md5 = md5;
				currentImport.save();
				
				ZahlungResult zahlungResult = getZahlungen(brInputFile, currentImport);

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
