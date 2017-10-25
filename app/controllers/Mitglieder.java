package controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import models.Mitglied;
import play.mvc.With;
import utils.ExcelBinder;
import utils.ExcelBindingException;

@CRUD.For(Mitglied.class)
@With(Secure.class)
public class Mitglieder extends CRUD{
	
	public static void delete(String id){
		Mitglied mitglied = Mitglied.findById(Long.valueOf(id));
		notFoundIfNull(mitglied);
		
		if(mitglied.zahlungen.size() == 0){
			try{
				mitglied._delete();
			}  catch (Exception e) {
	            flash.error(play.i18n.Messages.get("crud.delete.error", "Mitglied"));
	            redirect(request.controller + ".show", id);
	        }
		} else {
		    flash.error("Mitglied hat noch Zahlungen und kann nicht gelöscht werden.");
            redirect(request.controller + ".show", id);
		}
		
		redirect(request.controller + ".list");
	}
	
	public static void deleteProvisorisch(String id) throws Exception{
		Mitglied mitglied = Mitglied.findById(Long.valueOf(id));
		notFoundIfNull(mitglied);
		
		mitglied.alert = 2;
		mitglied._save();
		
		redirect(request.controller + ".show", id);
	}
	
	public static void undelete(String id) throws Exception{
		Mitglied mitglied = Mitglied.findById(Long.valueOf(id));
		notFoundIfNull(mitglied);
		
		mitglied.alert = null;
		mitglied._save();
		
		redirect(request.controller + ".show", id);
	}
	
	public static void uploadMitglieder(File mitgliederFile) throws ExcelBindingException {
		List<Mitglied> mitglieder = ExcelBinder.bind(mitgliederFile, Mitglied.class);
		
		List<Mitglied> doppelteMitglieder = new ArrayList<Mitglied>();
		List<Mitglied> ungueltigeMitglieder = new ArrayList<Mitglied>();
		
		for (Mitglied mitglied : mitglieder) {
			if(mitglied.overrideImport != null && !mitglied.overrideImport.toLowerCase().contains("ignor") 
			    && (StringUtils.isBlank(mitglied.name) || StringUtils.isBlank(mitglied.plz) || StringUtils.isBlank(mitglied.ort) 
					|| StringUtils.isBlank(mitglied.land) || StringUtils.isBlank(mitglied.briefanrede) || StringUtils.isBlank(mitglied.sprache)
					|| mitglied.mitgliedType == null)){
				ungueltigeMitglieder.add(mitglied);
				continue;
			}
			
			if(mitglied.warnIfDoppeltesMitglied() && mitglied.alreadyExists()){
				doppelteMitglieder.add(mitglied);
			}
		}
		
		if(!ungueltigeMitglieder.isEmpty()){
			flash.error("Bei einem oder mehreren Mitgliedern fehlen Name, Vorname, PLZ oder Mitgliedstyp, siehe Liste unten. <br/>Es wurden keine Datensätze importiert.");
			renderTemplate("Mitglieder/importData.html", ungueltigeMitglieder, null);
		} else if(!doppelteMitglieder.isEmpty()){
			flash.error("Ein oder mehrere Mitglieder in der Excel-Datei existieren bereits, siehe Liste unten. <br/>Es wurden keine Datensätze importiert.");
			renderTemplate("Mitglieder/importData.html", doppelteMitglieder, null);
		} else{
		
			int anzahl = 0;
			for (Mitglied mitglied : mitglieder) {
				if(mitglied.doImport()){
					mitglied.save();
					anzahl++;
				}
			}
			if(anzahl == 1)
				flash.success("Ein Mitglied importiert");
			else
				flash.success(anzahl + " Mitglieder importiert");
			
			importData(Collections.EMPTY_LIST);
		}
	}
	
	public static void importData(List<Mitglied> doppelteMitglieder){
		render(doppelteMitglieder);
	}
	
	public static void suche(String vorname, String name, String strasse, String plz, String ort, String land, String email, String memo){
		
		List<String> params = new ArrayList<String>();
		String query = "";
		
		query = addParam(vorname, params, query, "vorname", "vorname2");
		query = addParam(name, params, query, "name", "name2");
		query = addParam(strasse, params, query, "strasse", "postfach");
		query = addParam(plz, params, query, "plz");
		query = addParam(ort, params, query, "ort");
		query = addParam(land, params, query, "land");
		query = addParam(email, params, query, "email", "email2");
		query = addParam(memo, params, query, "memo");
		
		List<Mitglied> resultate = new ArrayList<Mitglied>();
		if(StringUtils.isNotBlank(query)){
			resultate = Mitglied.find(query, params.toArray()).fetch();
		}
		
		render(vorname, name, strasse, plz, ort, land, email, memo, resultate);
	}

	private static String addParam(String value, List<String> params, String query, String... columnNames) {
		if(StringUtils.isNotBlank(value)){
			
			if(StringUtils.isNotBlank(query))
				query += "AND (";
			else
				query += "(";
			
			boolean isFirst = true;
			for(String columnName : columnNames){
				
				if(!isFirst)
					query += " OR ";
				
				query += "(lower(" + columnName + " ) like ? )";
				params.add(value.toLowerCase().replace("*", "%"));
				isFirst = false;
			}
			query += ") ";
		}
		return query;
	}
}
