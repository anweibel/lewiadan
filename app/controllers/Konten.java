package controllers;

import models.Konto;
import play.mvc.With;

@CRUD.For(Konto.class)
@With(Secure.class)
@Check("administrator")
public class Konten extends CRUD {

}
