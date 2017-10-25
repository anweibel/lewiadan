package models;

import java.util.Collections;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Table;

import play.db.jpa.Model;

@Entity
@Table(name = "kategorien")
public class Kategorie extends Model implements Comparable<Kategorie>{

	public String katname;
	public String katshortname;
	
	public String toString(){
		
		if(katshortname != null)
			return katname + " (" + katshortname + ")";
		else
			return katname;
	}
	
	@Override
	public int compareTo(Kategorie other) {

		if(other == null)
			return 0;
		
		return this.katname.compareTo(other.katname);
	}
	
	public static List<Kategorie> findAllSorted(){
		List<Kategorie> all = Kategorie.findAll();
		Collections.sort(all);
		return all;
	}
}
