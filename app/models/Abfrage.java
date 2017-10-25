package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import play.data.validation.MaxSize;
import play.db.jpa.Model;

@Entity
@Table(name="abfragen")
public class Abfrage extends Model{

	@Column(name="sqltitle")
	@MaxSize(64)
	public String titel;
	
	@MaxSize(1024 * 8)
	@Column(name="sqltext")
	public String text;
	
	public String toString(){
		return titel;
	}
}
