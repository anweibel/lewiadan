package models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;

import play.data.validation.Check;
import play.data.validation.CheckWith;
import play.data.validation.Email;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.templates.JavaExtensions;
import controllers.CRUD.Hidden;

@Entity
@Table(name = "namen")
public class Mitglied extends Model {

	private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	
	@MaxSize(value = 20)
	@be.objectify.led.Property("Anrede")
	public String anrede;
	
	@MaxSize(value = 50)
	@Index(name="vornameIndex")
	@be.objectify.led.Property("Vorname")
	public String vorname;
	
	@Required
	@MaxSize(value = 50)
	@Index(name="nameIndex")
	@be.objectify.led.Property("Name")
	public String name;
	
	@MaxSize(value = 50)
	@be.objectify.led.Property("Vorname2")
	public String vorname2;
	
	@MaxSize(value = 50)
	@be.objectify.led.Property("Name2")
	public String name2;
	
	@MaxSize(value = 30)
	@be.objectify.led.Property("Strasse")
	public String strasse;
	
	@MaxSize(value = 20)
	@be.objectify.led.Property("Postfach")
	public String postfach;
	
	@Required
	@MaxSize(value = 6)
	@CheckWith(PostleitzahlCheck.class)
	@Index(name="plzIndex")
	@be.objectify.led.Property("PLZ")
	public String plz;
	
	@Required
	@MaxSize(value = 30)
	@be.objectify.led.Property("Ort")
	public String ort;
	
	@Required
	@MaxSize(value = 2)
	@be.objectify.led.Property("Land")
	public String land = "CH";
	
	@MaxSize(value = 30)
	@be.objectify.led.Property("Telefon")
	public String tel1;
	
	@MaxSize(value = 30)
	@be.objectify.led.Property("Telefon2")
	public String tel2;
	
	@MaxSize(value = 30)
	@be.objectify.led.Property("Fax")
	public String fax;
	
	@MaxSize(value = 30)
	@be.objectify.led.Property("Handy")
	public String handy;
	
	@Email
	@MaxSize(value = 50)
	@be.objectify.led.Property("Email")
	public String email;
	
	@Email
	@MaxSize(value = 50)
	@be.objectify.led.Property("Email2")
	public String email2;
	
	@Required
	@MaxSize(value = 70)
	@be.objectify.led.Property("Briefanrede")
	public String briefanrede;
	
	@Enumerated(EnumType.STRING)
	@Column(name="mitglied")
	@be.objectify.led.Property("Mitgliedertyp")
	public MitgliedType mitgliedType;
	
	@Required
	@MaxSize(value = 1)
	@be.objectify.led.Property("Sprache")
	public String sprache;
	
	@MaxSize(value = 1024)
	@be.objectify.led.Property("Bemerkung")
	public String memo;
	
	@Transient
	@be.objectify.led.Property("Override Import")
	public String overrideImport;
	
	@Hidden
	public Date mutdatum;
	
	@Hidden
	public Date erfdatum;
	
	@Hidden
	public Integer alert;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "mitglied")
	@OrderBy("datum ASC")
	@be.objectify.led.Property("Zahlungen")
	public List<Zahlung> zahlungen;

	@ManyToMany(cascade = CascadeType.DETACH)
	@JoinTable(name = "zugkat", joinColumns = { @JoinColumn(name = "nid") }, inverseJoinColumns = { @JoinColumn(name = "kid") })
	@be.objectify.led.Property("Kategorien")
	public List<Kategorie> kategorien;

	public String toString() {
		return vorname==null?"":vorname + " " + name + ", " + plz + " " + ort;
	}
		
	@PrePersist
	public void prePersist() {
		erfdatum = new Date();
		mutdatum = erfdatum;
		ensureBacklinks();
	}

	@PreUpdate
	public void preUpdate() {
		mutdatum = new Date();
		ensureBacklinks();
	}
	
	private void ensureBacklinks() {
		
		if (zahlungen == null)
			return;
		
		for(Zahlung zahlung : zahlungen){
			if(zahlung.mitglied == null){
				zahlung.mitglied = this;
			}
		}
	}
	
	public String getSpracheNice(){
		
		if(this.sprache == null){
			return "";
		}
		if(this.sprache.equals("D")){
			return "Deutsch";
		}
		if(this.sprache.equals("F")){
			return "Français";
		}
		return "Error: unknown Language";
	}
	
	public void setSprache(String sprache){
		if(sprache != null){
			this.sprache = "" + sprache.charAt(0);
		}
	}

	public String getSerialisedKategorien(){
		
		StringBuilder result = new StringBuilder();
		
		if(kategorien == null)
			return "";
		
		for(Kategorie kat:kategorien){
			if(result.length() > 0)
				result.append(",");
			result.append(kat.katshortname);
		}
		
		return result.toString();
	}
	
	public String getSerialisedZahlungen(){
		
		StringBuilder result = new StringBuilder();
		
		if(zahlungen == null)
			return "";
		
		for(Zahlung z:zahlungen){
			if(result.length() > 0)
				result.append("/");
			
			result.append(DATE_FORMAT.format(z.datum)).append(":");
			result.append(z.betrag).append(":");
			result.append(z.konto==null?"":z.konto.name).append(":");
			result.append(z.zahlungscode==null?"":z.zahlungscode.code).append(":");
			
			if(StringUtils.isNotBlank(z.bemerkung)){
				String escapedBemerkung = z.bemerkung.replaceAll(":", "&#58;").replaceAll("/", "&#47;");
				result.append(escapedBemerkung);
			}
		}
		
		return result.toString();
	}
	
	static class PostleitzahlCheck extends Check {
		 
	    public boolean isSatisfied(Object mitglied, Object plz) {
	    	
	    	setMessage("validation.plz");
	    	if(((Mitglied)mitglied).land.toUpperCase().equals("CH")){
	    		return ((String)plz).matches("\\d\\d\\d\\d");
	    	} else {
	    		return true;
	    	}
	    }
	}

	public boolean alreadyExists() {
		if(StringUtils.isNotEmpty(vorname) && Mitglied.find("lower(vorname) = lower(?) and lower(name) = lower(?) and plz = ?", vorname, name, plz).fetch().isEmpty())
			return false;
		else if (StringUtils.isEmpty(vorname) && Mitglied.find("lower(name) = lower(?) and plz = ?", name, plz).fetch().isEmpty())
			return false;
		else 
			return true;
	}
	
	public boolean warnIfDoppeltesMitglied(){
		if(StringUtils.isNotBlank(overrideImport) && (overrideImport.toLowerCase().contains("ignor") || overrideImport.toLowerCase().contains("import")))
			return false;
		else
			return true;
	}
	
	public boolean doImport(){
		if(StringUtils.isNotBlank(overrideImport) && (overrideImport.toLowerCase().contains("ignor")))
			return false;
		else
			return true;
	}
	
}
