package utils.esr;

import java.sql.Date;

import models.Import;
import models.Konto;

public class EsrZahlung {
	
	private Date datum;
	private long zahlerID;
	private double betrag;
	private Konto konto;
	private String bemerkung;
	private Long zahlungsCodeID;
	private String versandID;
	private Import esrImport;
	
	public Date getDatum() {
		return datum;
	}
	public void setDatum(Date datum) {
		this.datum = datum;
	}
	public long getZahlerID() {
		return zahlerID;
	}
	public void setZahlerID(long zahlerID) {
		this.zahlerID = zahlerID;
	}
	public double getBetrag() {
		return betrag;
	}
	public void setBetrag(double betrag) {
		this.betrag = betrag;
	}
	public Konto getKonto() {
		return konto;
	}
	public void setKonto(Konto konto) {
		this.konto = konto;
	}
	public String getBemerkung() {
		return bemerkung;
	}
	public void setBemerkung(String bemerkung) {
		this.bemerkung = bemerkung;
	}
	public Long getZahlungsCodeID() {
		return zahlungsCodeID;
	}
	public void setZahlungsCodeID(Long zahlungsCodeID) {
		this.zahlungsCodeID = zahlungsCodeID;
	}
	
	public String getVersandID() {
		return versandID;
	}
	public void setVersandID(String versandID) {
		this.versandID = versandID;
	}
	public Import getImport() {
		return esrImport;
	}
	public void setImport(Import esrImport) {
		this.esrImport = esrImport;
	}
	public String toString(){
		return "\nDatum = " + datum +
			   "\nZahlerID = " + zahlerID +
			   "\nBetrag = " + betrag +
			   "\nKontoID = " + (konto == null ? "?" : konto.id) +
			   "\nesrImport = " + (konto == null ? "?" : esrImport.id) +
			   "\nversandId = " + versandID;
	}

}
