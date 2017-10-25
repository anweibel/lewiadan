package controllers;

import models.Zahlungscode;
import play.mvc.With;

@CRUD.For(Zahlungscode.class)
@With(Secure.class)
@Check("administrator")
public class Zahlungscodes extends CRUD {

}
