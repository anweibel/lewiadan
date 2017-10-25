package utils.esr;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import play.db.DB;
import models.Konto;
import models.Mitglied;
import models.Zahlung;
import models.Zahlungscode;

public class DBConnector {

	public void insertZahlung(EsrZahlung zahlung) throws Exception {

		Zahlung z = new Zahlung();
		z.mitglied = Mitglied.findById(zahlung.getZahlerID());
		z.zahlungscode = Zahlungscode.findById(zahlung.getZahlungsCodeID());
		z.konto = zahlung.getKonto();
		z.betrag = new BigDecimal(zahlung.getBetrag());
		z.datum = zahlung.getDatum();
		z.bemerkung = zahlung.getBemerkung();
		z.versandId = Long.parseLong(zahlung.getVersandID());
		z.esrImport = zahlung.getImport();
				
		z.save();
	}

	public String zahlungAlreadyExists(EsrZahlung zahlung) throws Exception {

		List<Zahlung> zahlungen = Zahlung.find("nid = ? and zbetrag = ? and zdatum = ?", zahlung.getZahlerID(), zahlung.getBetrag(), zahlung.getDatum()).fetch();

		if (zahlungen != null && zahlungen.size() > 0) {
			String ret = "";
			for(Zahlung z : zahlungen){
				ret += z.bemerkung + "\n<br/>";
			}
			return ret;
		} else {
			return null;
		}
	}

	public Zahler getZahler(long zahlerID) throws Exception {

		Mitglied mitglied = Mitglied.findById(zahlerID);
		
		Zahler zahler = new Zahler();

		if (mitglied != null) {
			zahler.setName(mitglied.name);
			zahler.setVorname(mitglied.vorname);
			zahler.setStrasse(mitglied.strasse);
			zahler.setPlz(mitglied.plz);
			zahler.setOrt(mitglied.ort);
			zahler.setMitgliedTyp(mitglied.mitgliedType.toString());
		} else {
			throw new ZahlerUnknown();
		}

		return zahler;
	}

	public long maxDBIdZahlung() throws Exception {

		String query = "select max(id) as max from zahlungen";
		PreparedStatement stmt;

		Connection conn = DB.getConnection();
		stmt = conn.prepareStatement(query);

		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			return rs.getLong("max");
		} else {
			throw new Exception("maxDBIdZahlung: Konnte id nicht lesen.");
		}
	}
}
