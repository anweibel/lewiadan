package models;

import java.math.BigInteger;

import utils.esr.PruefzifferBerechner;


public class EsrMitglied extends Mitglied {

	private static PruefzifferBerechner pruefzifferBerechner = new PruefzifferBerechner();
	public String referenznummer; 
	
	public EsrMitglied(Mitglied mitglied, Long versandId) {
		this.id = mitglied.id;
		this.alert = mitglied.alert;
		this.anrede = mitglied.anrede;
		this.briefanrede = mitglied.briefanrede;
		this.email = mitglied.email;
		this.email2 = mitglied.email2;
		this.erfdatum = mitglied.erfdatum;
		this.fax = mitglied.fax;
		this.handy = mitglied.handy;
		this.kategorien = mitglied.kategorien;
		this.land = mitglied.land;
		this.memo = mitglied.memo;
		this.mitgliedType = mitglied.mitgliedType;
		this.mutdatum = mitglied.mutdatum;
		this.name = mitglied.name;
		this.name2 = mitglied.name2;
		this.ort = mitglied.ort;
		this.plz = mitglied.plz;
		this.postfach = mitglied.postfach;
		this.strasse = mitglied.strasse;
		this.tel1 = mitglied.tel1;
		this.tel2 = mitglied.tel2;
		this.vorname = mitglied.vorname;
		this.vorname2 = mitglied.vorname2;
		this.zahlungen = mitglied.zahlungen;
		
		int pruefziffer = pruefzifferBerechner.computeNumber(new BigInteger(this.id.toString() + versandId));
		
		this.referenznummer = this.id + "" + versandId + "" + pruefziffer;
	}

}
