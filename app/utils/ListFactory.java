package utils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import models.Kategorie;
import models.Konto;
import models.Zahlung;
import models.Zahlungscode;
import be.objectify.led.PropertyContext;
import be.objectify.led.factory.object.AbstractObjectFactory;

/**
 */
public class ListFactory extends AbstractObjectFactory<List>{
    
	private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	
	public List createObject(String propertyName,
                                 String propertyValue,
                                 PropertyContext propertyContext){
    	
    	if(propertyName.equals("Kategorien")){
    		return extractKategorien(propertyValue);
    	} else if(propertyName.equals("Zahlungen")){
    		return extractZahlungen(propertyValue);
    	} else {
    		throw new RuntimeException(propertyName + " ist kein bekanntes Listen-Objekt");
    	}
    			
    }

	private List extractZahlungen(String propertyValue) {
		List<Zahlung> zahlungen = new ArrayList<Zahlung>();
    	for(String z : propertyValue.split("/")){
	    	Zahlung zahlung = new Zahlung();
    		
    		String[] array = z.split(":");
    		
    		try {
				zahlung.datum = DATE_FORMAT.parse(array[0].trim());
			} catch (ParseException e) {
				throw new RuntimeException("Problem beim Verarbeiten von Zahlung '" + propertyValue + "'.", e);
			}
	    	zahlung.betrag = new BigDecimal(array[1].trim());
	    	
	    	if(StringUtils.isNotBlank(array[2]))
	    		zahlung.konto = (Konto)Konto.find("name", array[2].trim()).first();
	    	
	    	if(StringUtils.isNotBlank(array[3]))
	    		zahlung.zahlungscode = (Zahlungscode)Zahlungscode.find("code", array[3].trim()).first();
	    	
	    	if(array.length > 4 && StringUtils.isNotBlank(array[4]))
	    		zahlung.bemerkung = array[4].replaceAll("&#58;", ":").replaceAll("&#47;", "/");
    		
	    	zahlung.save();
	    	
    		zahlungen.add(zahlung);
    	}
		
		return zahlungen;
	}

	private List extractKategorien(String propertyValue) {
		List<Kategorie> kategories = new ArrayList<Kategorie>();
    	for(String kat : propertyValue.split(",")){
	    	kategories.add((Kategorie)Kategorie.find("katshortname", kat.trim()).first());
    	}
        return kategories;
	}

    public Class<List> getBoundClass(){
        return List.class;
    }
}
