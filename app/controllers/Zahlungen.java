package controllers;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import models.Zahlung;
import play.data.binding.Binder;
import play.db.Model;
import play.exceptions.TemplateNotFoundException;
import play.mvc.With;
import utils.esr.CamtFileReader;
import utils.esr.ESRFileReader;

@CRUD.For(Zahlung.class)
@With(Secure.class)
public class Zahlungen extends CRUD {
	
	public static void blank(String nid) throws Exception {
		flash.put("nid", nid);
		render();
	}
	
    public static void save(String id) throws Exception {
        ObjectType type = ObjectType.get(getControllerClass());
        notFoundIfNull(type);
        Model object = type.findById(id);
        notFoundIfNull(object);
        Binder.bindBean(params.getRootParamNode(), "object", object);
        validation.valid(object);
        if (validation.hasErrors()) {
            renderArgs.put("error", play.i18n.Messages.get("crud.hasErrors"));
            try {
                render(request.controller.replace(".", "/") + "/show.html", type, object);
            } catch (TemplateNotFoundException e) {
                render("CRUD/show.html", type, object);
            }
        }
        object._save();
        flash.success(play.i18n.Messages.get("crud.saved", type.modelName));
        if (params.get("_save") != null) {
        	redirect("Mitglieder.show", ((Zahlung)object).mitglied._key());
        }
        redirect(request.controller + ".show", object._key());
    }
    
    public static void create() throws Exception {
        ObjectType type = ObjectType.get(getControllerClass());
        notFoundIfNull(type);
        Constructor<?> constructor = type.entityClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        Model object = (Model) constructor.newInstance();
        Binder.bindBean(params.getRootParamNode(), "object", object);
        validation.valid(object);
        if (validation.hasErrors()) {
            renderArgs.put("error", play.i18n.Messages.get("crud.hasErrors"));
            try {
                render(request.controller.replace(".", "/") + "/blank.html", type, object);
            } catch (TemplateNotFoundException e) {
                render("CRUD/blank.html", type, object);
            }
        }
        object._save();
        flash.success(play.i18n.Messages.get("crud.created", type.modelName));
        if (params.get("_save") != null) {
        	redirect("Mitglieder.show", ((Zahlung)object).mitglied._key());
        }
        redirect(request.controller + ".show", object._key());
    }
    
    public static void delete(String id) throws Exception {
        ObjectType type = ObjectType.get(getControllerClass());
        notFoundIfNull(type);
        Model object = type.findById(id);
        notFoundIfNull(object);
        Object mitgliedKey = ((Zahlung)object).mitglied._key();
        try {
            object._delete();
        } catch (Exception e) {
            flash.error(play.i18n.Messages.get("crud.delete.error", type.modelName));
            redirect(request.controller + ".show", object._key());
        }
        flash.success(play.i18n.Messages.get("crud.deleted", type.modelName));
        redirect("Mitglieder.show", mitgliedKey);
    }
    
    public static void camtImport(File esrImportFile){
    	
    	if(esrImportFile != null) {
    	
    		CamtFileReader cfr = new CamtFileReader();
	    	
	    	List<File> files = new ArrayList<File>();
	    	files.add(esrImportFile);
	    	try {
				String answer = cfr.processFiles(files);
				if(cfr.errorOccured){
					flash.error(answer);
				} else {
					flash.success(answer);
				}
				
			} catch (Exception e) {
				flash.error("Ein Fehler ist aufgetreten: " + e.getMessage());
			}
    	}
			
    	render();
    }
    
    public static void esrImport(File esrImportFile){
    	
    	if (esrImportFile != null){
	    	if (esrImportFile.getName().endsWith("GSOA123.V11")) {
	    		flash.error("Diesen Dateinamen akzeptiere ich nicht!\n<br/>Bitte Datei so umbennenen, dass der Name das Datum enthält, das im Mail der Post erwähnt wird.");
	    		render();
			}
	    	
	    	ESRFileReader efr = new ESRFileReader();
	    	
	    	List<File> files = new ArrayList<File>();
	    	files.add(esrImportFile);
	    	try {
				String answer = efr.processFiles(files);
				if(efr.errorOccured){
					flash.error(answer);
				} else {
					flash.success(answer);
				}
				
			} catch (Exception e) {
				flash.error("Ein Fehler ist aufgetreten: " + e.getMessage());
			}
    	}
    	
    	render();
    }
}
