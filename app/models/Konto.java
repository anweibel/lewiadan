package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import play.db.jpa.Model;

@Entity
@Table(name="konten")
public class Konto extends Model{
	
	@Column(name="kname")
	public String name;
	
	@Column(name="kundennr")
	public Long kundenNummer;
	
	@Column(name="std")
	public Boolean isStandard;

	public String toString(){
		return name + " - " + kundenNummer + (isStandard?" (Standardkonto)":"");
	}
}
