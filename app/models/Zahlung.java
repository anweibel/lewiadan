package models;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;

import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
@Table(name = "zahlungen")
public class Zahlung extends Model implements Comparable{
	
	private static SimpleDateFormat FORMAT = new SimpleDateFormat("dd.MM.yyyy");
	
	@Column(name = "zbetrag")
	@Required
	public BigDecimal betrag;
	
	@Column(name = "zbemerkung")
	@MaxSize(64)
	public String bemerkung;
	
	@Column(name = "versandid")
	public Long versandId;
	
	@Column(name = "zdatum")
	@Required
	public Date datum;
	
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nid")
	public Mitglied mitglied;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "zcid")
    @Required
    public Zahlungscode zahlungscode;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "koid")
    @Required
    public Konto konto;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "importid")
    public Import esrImport;
    
    public String toString(){
    	return betrag + " CHF am " + FORMAT.format(datum) 
    			+ (zahlungscode!=null?(". Code " + zahlungscode.code):". Kein Code") 
    			+ (konto!=null?(". Konto " + konto.name):". Kein Konto")
    			+ (StringUtils.isNotBlank(bemerkung)?(". Bemerkung: " + bemerkung):"");
    }

	@Override
	public int compareTo(Object o) {

		Zahlung other = (Zahlung)o;
		if(other.zahlungscode == null && this.zahlungscode == null) {
			return this.datum.compareTo(other.datum);
		} else if(this.zahlungscode != null && other.zahlungscode == null) {
			return -1;
		} else if(other.zahlungscode != null && this.zahlungscode == null) {
			return 1;
		} else if (!this.zahlungscode.code.equals(other.zahlungscode.code)){
			return (-1) * this.zahlungscode.code.compareTo(other.zahlungscode.code);
		} else {
			return this.datum.compareTo(other.datum);
		}
	}
}
