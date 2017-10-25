package controllers;

import static play.modules.excel.RenderExcel.renderAsync;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import models.Adressliste;
import models.EsrMitglied;
import models.Mitglied;
import models.MitgliedType;

import org.apache.commons.lang.StringUtils;

import play.Play;
import play.db.DB;
import play.db.Model;
import controllers.CRUD.For;
import controllers.CRUD.ObjectType;

@For(Adressliste.class)
public class Adresslisten extends CRUD{

	private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	
	private static boolean isDerby = Play.configuration.getProperty("db.driver").contains("derby"); 
	        
    public static void create(String beschreibung, String sprache, String plzVon, String plzBis, String land, MitgliedType mitgliedType, 
    		Date zahlungVon, Date zahlungBis, Float zahlungSumme, List<String> kategorie, List<String> ausschlussKategorie, 
    		Integer anzahlBesteSpender, Boolean adminAdressenAusschlissen) throws Exception{

    	if(adminAdressenAusschlissen == null)
    		adminAdressenAusschlissen = Boolean.FALSE;
    	
    	Pattern p = Pattern.compile("[^A-Za-z0-9]");

    	if(StringUtils.isBlank(beschreibung)){
    		flash.error("'Beschreibung' darf nicht leer sein.");
    		redirect(request.controller + ".blank");
    	}
    	
    	if(StringUtils.isNotBlank(plzVon) && p.matcher(plzVon).find()){
    		flash.error("'PLZ von' darf nur Zahlen und Buchstaben enthalten. Keine Sonderzeichen, keine Umlaute.");
    		redirect(request.controller + ".blank");
    	}
    	
    	if(StringUtils.isNotBlank(plzBis) && p.matcher(plzBis).find()){
    		flash.error( "'PLZ bis' darf nur Zahlen und Buchstaben enthalten. Keine Sonderzeichen, keine Umlaute.");
    		redirect(request.controller + ".blank");
    	}
    		
    	if(StringUtils.isNotBlank(land) && p.matcher(land).find()){
    		flash.error("'Land' darf nur Zahlen und Buchstaben enthalten. Keine Sonderzeichen, keine Umlaute.");
    		redirect(request.controller + ".blank");
    	}
    	
    	boolean nurBesteSpender = false;
    	if(anzahlBesteSpender != null && anzahlBesteSpender > 0){
    		nurBesteSpender = true;
    	}
    	
    	boolean nurSpenderMitSumme = false;
    	if(zahlungBis != null && zahlungVon != null && zahlungSumme != null){
    		nurSpenderMitSumme = true;
    	}
    	
    	if(nurSpenderMitSumme == false && (zahlungBis != null || zahlungVon != null || zahlungSumme != null)){
    		flash.error("Um nach der Spendensumme zu filtern, müssen der Betrag sowie das Von- und Bis-Datum ausgefüllt sein.");
    		redirect(request.controller + ".blank");
    	}
    	
    	String select = "SELECT n.id as id FROM namen n ";
    	
    	if(nurSpenderMitSumme){
    		select += "LEFT OUTER JOIN zahlungen z ON n.id = z.nid ";
    	}
    	if(nurBesteSpender){
    		select += "LEFT JOIN "
    			+ "(SELECT nid, SUM(zbetrag*(1-0.9/1000*LEAST(GREATEST((DATEDIFF(NOW(),zdatum)-365), 0), 1000))) AS bewertung " 
    			+ "  FROM zahlungen "
    			+ "  WHERE zcid IN (1,3,4) "
    			+ "  GROUP BY nid) "
    			+ "AS bewertungsSelect "
    			+ "on bewertungsSelect.nid = n.id " ; 
    	}
    		
    	select += "WHERE (n.alert = 0 OR n.alert IS NULL) ";
    	
    	sprache = "" + sprache.charAt(0);
    	select += " AND sprache ='" + sprache + "'";
    	
    	if(adminAdressenAusschlissen){
    		select += " AND (mitglied != 'X' OR mitglied IS NULL) ";
    	}
    	
    	if(StringUtils.isNotBlank(plzVon)){
    		select += " AND UPPER(n.plz) >= '" + plzVon.trim().toUpperCase() + "'";
    	}
    	if(StringUtils.isNotBlank(plzBis)){
    		select += " AND UPPER(n.plz) <= '" + plzBis.trim().toUpperCase() + "'";
    	}
    	if(StringUtils.isNotBlank(land)){
    		select += " AND UPPER(n.land) = '" + land.trim().toUpperCase() + "'";
    	}
    	if(mitgliedType != null){
    		select += " AND n.mitglied = '" + mitgliedType.toString() + "'";
    	}
    	
    	if(nurSpenderMitSumme){
    		if(isDerby){
    			select += " AND z.zdatum BETWEEN TIMESTAMP('" + DATE_FORMAT.format(zahlungVon) + " 00:00:00') AND TIMESTAMP('" + DATE_FORMAT.format(zahlungBis) + " 00:00:00')";
    		} else {    			
    			select += " AND z.zdatum BETWEEN '" + DATE_FORMAT.format(zahlungVon) + "' AND '" + DATE_FORMAT.format(zahlungBis) + "'";
    		}
    	}
    	
    	String kategorieCriteria = "";
    	for(String kat : kategorie){
    		if(StringUtils.isNotBlank(kat)){
    			kategorieCriteria += " OR (select count(*) from zugkat k where n.id = k.nid and k.kid = " + Integer.parseInt(kat) + ") > 0";
    		}
    	}
    	
    	if(StringUtils.isNotBlank(kategorieCriteria))
    		select += " AND ( 1=0 " + kategorieCriteria + " )";
    	
    	for(String kat : ausschlussKategorie){
    		if(StringUtils.isNotBlank(kat)){
    			select += " AND (select count(*) from zugkat k where n.id = k.nid and k.kid = " + Integer.parseInt(kat) + ") = 0";
    		}
    	}
    	
    	select += " GROUP BY n.id ";
    	
    	if(nurSpenderMitSumme){
    		select += " HAVING SUM(zbetrag) > " + zahlungSumme;
    	}
    	
    	if(nurBesteSpender){
    		select += " ORDER BY bewertung DESC ";
    	}
    	
    	Connection con = DB.getConnection();
    	PreparedStatement statement = con.prepareStatement(select);
    	
    	if(nurBesteSpender){
    		statement.setMaxRows(anzahlBesteSpender);
    	}
    	
    	ResultSet results = statement.executeQuery();
    	
    	List<Mitglied> mitglieder = new ArrayList<Mitglied>();
    	
    	while(results.next()){
    		Long id = results.getLong("id");
    		mitglieder.add((Mitglied)Mitglied.findById(id));
    	}
    	
    	Adressliste adressliste = new Adressliste();
    	adressliste.beschreibung = beschreibung + " (" + sprache + ")";
    	adressliste.mitglieder = mitglieder;
    	adressliste.datum = new Date();
    	adressliste.save();
    	
    	redirect(request.controller + ".show", adressliste.id);
    }
    
    public static void save(Long id, String beschreibung) throws Exception{
    	
    	ObjectType type = ObjectType.get(getControllerClass());
    	Model object = type.findById(id.toString());
    	
    	if(StringUtils.isBlank(beschreibung)){
	    	flash.error("Hoppla! 'Beschreibung der resultierenden Adressliste' darf nicht leer sein!");
	        render(request.controller.replace(".", "/") + "/show.html", type, object);
    	}
    	
    	Adressliste adressliste = Adressliste.findById(id);
    	adressliste.beschreibung = beschreibung;
    	adressliste.save();
    	
    	flash.success("Name der Adressliste geändert!");
        render(request.controller.replace(".", "/") + "/show.html", type, object);
    }
    
    public static void subtract(Long idOrig, Long idMinus, String beschreibung) throws Exception{
    	
    	if(StringUtils.isBlank(beschreibung)){
	    	flash.error("Hoppla! 'Beschreibung der resultierenden Adressliste' darf nicht leer sein!");
	    	ObjectType type = ObjectType.get(getControllerClass());
	    	Model object = type.findById(idOrig.toString());
	        render(request.controller.replace(".", "/") + "/show.html", type, object);
    	}
    	
    	String select = "SELECT nid FROM zugadrliste orig WHERE orig.adr_id = " + idOrig
    			+ " AND orig.nid NOT IN (SELECT nid FROM zugadrliste minus WHERE minus.adr_id = " + idMinus + ")";
    	
    	Adressliste adressliste = new Adressliste();
    	adressliste.beschreibung = beschreibung;
    	adressliste.datum = new Date();
    	adressliste.save();
    	
    	String insert = "INSERT INTO zugadrliste SELECT " + adressliste.id + ", nid FROM (" + select + ") AS innerSelect";
    	
    	Connection con = DB.getConnection();
    	Statement statement = con.createStatement();
    	statement.execute(insert);
    	
    	flash.success("Die neue Adresseliste '" + beschreibung +"' wurde erstellt. Sie besteht aus der Subtraktion zweier früherer Adresslisten.");
    	
    	redirect(request.controller + ".show", adressliste.id);
    }
    
    public static void exportExcel(Long id, String kompakt, String full) throws InterruptedException, ExecutionException{
    	
    	String templateName = "app/views/Adresslisten/";
    	if(kompakt != null){
    		templateName += "export.xls";
    	} else if (full != null){
    		templateName += "exportFull.xls";
    	}
    	
    	Adressliste adressliste = Adressliste.findById(id);
    	List<Mitglied> mitglieder = adressliste.mitglieder;

    	Map<String, Object> beans = new HashMap<String, Object>();
    	beans.put("mitglieder", mitglieder);
    	
    	String dateiname = "export";
    	if(StringUtils.isNotBlank(adressliste.beschreibung)){
    		dateiname = adressliste.beschreibung.replace(" ", "_") + "_" + DATE_FORMAT.format(adressliste.datum);
    	}
    	
		String filename = dateiname + ".xls";
		response.setHeader("Content-Disposition", "attachment; filename="+ filename);
		
		renderAsync(templateName, beans, "").get().apply(request, response);
    }
    
    public static void exportEsr(Long id, Long versandId) throws InterruptedException, ExecutionException{
    	Adressliste adressliste = Adressliste.findById(id);
    	List<Mitglied> mitglieder = adressliste.mitglieder;

    	List<EsrMitglied> esrMitglieder = new ArrayList<EsrMitglied>();
    	for(Mitglied mitglied : mitglieder){
    		esrMitglieder.add(new EsrMitglied(mitglied, versandId));
    	}
    	
    	Map<String, Object> beans = new HashMap<String, Object>();
    	beans.put("mitglieder", esrMitglieder);
    	
    	String dateiname = "ESR_export";
    	if(StringUtils.isNotBlank(adressliste.beschreibung)){
    		dateiname = "ESR_" + adressliste.beschreibung.replace(" ", "_") + "_" + DATE_FORMAT.format(adressliste.datum);
    	}
    	
		String filename = dateiname + ".xls";
		response.setHeader("Content-Disposition", "attachment; filename="+ filename);
		
		renderAsync("app/views/Adresslisten/exportEsr.xls", beans, "").get().apply(request, response);
    }
}
