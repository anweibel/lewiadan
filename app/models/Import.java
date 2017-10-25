package models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
@Table(name = "imports")
public class Import extends Model {
	
	@Column(name = "md5")
	@Required
	public String md5;
	
	@Column(name = "created")
	@Required
	public Date created;
	
	@Column(name = "type")
	@Required
	public String type;
}
