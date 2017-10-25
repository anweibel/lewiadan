package models;

import javax.persistence.Entity;
import javax.persistence.Table;

import play.db.jpa.Model;

@Entity
@Table(name = "zahlungscode")
public class Zahlungscode extends Model{
	public String code;
	
	public String toString(){
		return code;
	}
}
