package controllers;

import models.Kategorie;
import play.mvc.With;

@CRUD.For(Kategorie.class)
@With(Secure.class)
@Check("administrator")
public class Kategorien extends CRUD{

}
