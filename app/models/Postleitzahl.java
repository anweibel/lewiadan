package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import play.db.jpa.Model;

@Entity
@Table(name="postleitzahlen")
public class Postleitzahl extends Model{
	
	@Column(name="plzplz")
	public String plz;
	
	@Column(name="plzname")
	public String name;
	
	public String toString(){
		return plz + " " + name;
	}
}
