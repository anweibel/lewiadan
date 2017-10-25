package models;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;

import play.data.validation.MaxSize;
import play.db.jpa.Model;

@Entity
@Table(name="adresslisten")
public class Adressliste extends Model implements Comparable<Adressliste>{
	
	private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
	
	@MaxSize(1024)
	public String beschreibung;
	
	@MaxSize(1024 * 8)
	public String schritte;
	
	public Date datum;
	
	@ManyToMany
	@JoinTable(name = "zugadrliste", joinColumns = { @JoinColumn(name = "adr_id") }, inverseJoinColumns = { @JoinColumn(name = "nid") })
	public List<Mitglied> mitglieder;
	
	@Override
	public String toString(){
		return beschreibung + " (" + DATE_FORMAT.format(datum) + ")";
	} 
	
	@Override
	public int compareTo(Adressliste other) {

		if(other == null)
			return 0;
		
		return this.datum.compareTo(other.datum);
	}
	
	public static List<Adressliste> findAllSorted(){
		List<Adressliste> all = Adressliste.findAll();
		Collections.sort(all);
		return all;
	}
}
