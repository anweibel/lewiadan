package controllers;

import models.Kategorie;
import models.Konto;
import models.Postleitzahl;
import models.User;
import models.Zahlungscode;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.test.Fixtures;

@OnApplicationStart
public class Bootstrap extends Job{

    public void doJob() {
        if(User.count() == 0){
        	User adminUser = User.createNewUser("admin", "bleiberecht", Boolean.TRUE);
        	adminUser.save();
        }
        if(Kategorie.count() == 0){
        	Kategorie kat1 = new Kategorie();
        	kat1.katname = "Spender";
        	kat1.katshortname = "SPE";
        	kat1.save();
        	
        	Kategorie kat2 = new Kategorie();
        	kat2.katname = "Sympis";
        	kat2.katshortname = "SYM";
        	kat2.save();
        }
        
        if(Zahlungscode.count() == 0){
        	Zahlungscode zc1 = new Zahlungscode();
        	zc1.code = "Spende";
        	
        	Zahlungscode zc2 = new Zahlungscode();
        	zc2.code = "Mitglliederbeitrag";
        }
        
        if(Konto.count() == 0){
        	Konto k1 = new Konto();
        	k1.isStandard = true;
        	k1.name = "PC";
        }
        
        if(Postleitzahl.count() == 0) {
            Fixtures.loadModels("plz-data.yml");
        }
    }
}
