package controllers;

import static play.modules.excel.RenderExcel.renderAsync;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import models.Adressliste;
import models.Kategorie;
import models.Mitglied;
import models.MitgliedType;

import org.apache.commons.lang.StringUtils;

import play.db.DB;
import play.mvc.Controller;
import play.mvc.With;

@With(Secure.class)
public class Datenexport extends Controller{

	private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	
    public static void index() {
        render();
    }
    
    public static void exportSql(String text) throws Exception{
    	
    	text = text.replace(";", "").replace("\"", "'");
    	List<Mitglied> mitglieder = Mitglied.find("id in (" + text + ")").fetch();
    	
    	renderExcel(mitglieder);
    }
    
    public static void export(String plzVon, String plzBis, String land, MitgliedType mitgliedType, 
    		Date zahlungVon, Date zahlungBis, Float zahlungSumme, List<String> kategorie) throws Exception{

    	Pattern p = Pattern.compile("[^A-Za-z0-9]");
    	request.params.all();
    	if(StringUtils.isNotBlank(plzVon) && p.matcher(plzVon).find()){
    		renderArgs.put("error", "'PLZ von' darf nur Zahlen und Buchstaben enthalten. Keine Sonderzeichen, keine Umlaute.");
    		render("/crud/abfragen"); //TODO: Richtig validieren!
    	}
    	
    	if(StringUtils.isNotBlank(plzBis) && p.matcher(plzBis).find()){
    		renderArgs.put("error", "'PLZ bis' darf nur Zahlen und Buchstaben enthalten. Keine Sonderzeichen, keine Umlaute.");
    		render("/crud/abfragen");
    	}
    		
    	if(StringUtils.isNotBlank(land) && p.matcher(land).find()){
    		renderArgs.put("error", "'Land' darf nur Zahlen und Buchstaben enthalten. Keine Sonderzeichen, keine Umlaute.");
    		render("crud/abfragen");
    	}
    	
    	String select = "SELECT n.id as id FROM namen n " +
    			"LEFT OUTER JOIN zahlungen z ON n.id = z.nid " +
    			"WHERE n.alert = 0 ";
    	
    	if(StringUtils.isNotBlank(plzVon)){
    		select += " AND UPPER(plz) => '" + plzVon.trim().toUpperCase() + "'";
    	}
    	if(StringUtils.isNotBlank(plzBis)){
    		select += " AND UPPER(plz) <= '" + plzBis.trim().toUpperCase() + "'";
    	}
    	if(StringUtils.isNotBlank(land)){
    		select += " AND UPPER(land) = '" + land.trim().toUpperCase() + "'";
    	}
    	if(mitgliedType != null){
    		select += " AND mitglied = '" + mitgliedType.toString() + "'";
    	}
    	
    	if(zahlungBis != null && zahlungVon != null && zahlungSumme != null){
    		select += " AND z.zdatum BETWEEN '" + DATE_FORMAT.format(zahlungVon) + "' AND '" + DATE_FORMAT.format(zahlungVon) + "'";
    	}
    	
    	for(String kat : kategorie){
    		if(StringUtils.isNotBlank(kat)){
    			select += " AND (select count(*) from zugkat k where n.id = k.nid and k.kid = " + Integer.parseInt(kat) + ") > 0";
    		}
    	}
    	
    	select += " GROUP BY n.id ";
    	
    	if(zahlungBis != null && zahlungVon != null && zahlungSumme != null){
    		select += " HAVING SUM(zbetrag) > " + zahlungSumme;
    	}
    	
    	Connection con = DB.getConnection();
    	PreparedStatement statement = con.prepareStatement(select);   	
    	
    	ResultSet results = statement.executeQuery();
    	
    	List<Mitglied> mitglieder = new ArrayList<Mitglied>();
    	
    	while(results.next()){
    		Long id = results.getLong("id");
    		mitglieder.add((Mitglied)Mitglied.findById(id));
    	}
    	
    	Adressliste adressliste = new Adressliste();
    	adressliste.beschreibung = "try";
    	adressliste.mitglieder = mitglieder;
    	adressliste.datum = new Date();
    	adressliste.save();
    	

    	renderExcel(mitglieder);
    }

	private static void renderExcel(List<Mitglied> mitglieder)
			throws InterruptedException, ExecutionException {
		Map<String, Object> beans = new HashMap<String, Object>();
    	beans.put("mitglieder", mitglieder);
    	
		String filename = "export.xls";
		response.setHeader("Content-Disposition", "attachment; filename="+ filename);
		
		renderAsync("app/views/Adresslisten/export.xls", beans, "").get().apply(request, response);
	}
    
    
}
