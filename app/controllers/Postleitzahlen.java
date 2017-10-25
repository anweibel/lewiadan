package controllers;

import models.Postleitzahl;
import play.mvc.With;

@CRUD.For(Postleitzahl.class)
@With(Secure.class)
public class Postleitzahlen extends CRUD{

	public static void getOrt(String plz){
		Postleitzahl postleitzahl = Postleitzahl.find("plz", plz).first();
		renderJSON(postleitzahl);
	}
}
