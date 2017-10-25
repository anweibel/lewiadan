package controllers;

import models.Abfrage;
import play.mvc.With;

@CRUD.For(Abfrage.class)
@With(Secure.class)
public class Abfragen extends CRUD{

}
