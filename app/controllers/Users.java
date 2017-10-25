package controllers;

import java.util.UUID;

import models.User;

import org.apache.commons.lang.StringUtils;

import play.i18n.Lang;
import play.libs.Crypto;
import play.libs.Crypto.HashType;
import play.mvc.With;

@CRUD.For(User.class)
@With(Secure.class)
@Check("administrator")
public class Users extends CRUD{
	
	public static void create(String username, String password, Boolean isAdmin){
		
		if(password.length() < 8)
			validation.addError("password", "Passwort muss mindestens 8 Zeichen lang sein");
		
		User newUser = User.createNewUser(username, password, isAdmin);
		
		validation.valid(newUser);
        if (validation.hasErrors()) {
            renderArgs.put("error", play.i18n.Messages.get("crud.hasErrors"));
            render(request.controller.replace(".", "/") + "/blank.html", "User", newUser);
        }
		
		newUser.save();
		
		redirect(request.controller + ".list");
	}
	
	public static void edit(Long id, String username, String password, Boolean isAdmin) throws Exception{
		if(isAdmin == null)
			isAdmin = Boolean.FALSE;
                
		User user = User.findById(id);
		
		user.username = username;
		if(StringUtils.isNotBlank(password)){
			if(password.length() < 8)
				validation.addError("password", "Passwort muss mindestens 8 Zeichen lang sein");
			user.salt = UUID.randomUUID().toString();
			user.passwordHash = Crypto.passwordHash(user.salt + password, HashType.SHA512);
		}
		user.isAdmin = isAdmin;
		
		validation.valid(user);
        if (validation.hasErrors()) {
            renderArgs.put("error", play.i18n.Messages.get("crud.hasErrors"));
            flash.error(validation.errors().toString());
            redirect(request.controller + ".show", user.id);
        }
		user.save();
		
		redirect(request.controller + ".list");
	}
	
	public static void changeLang(String lang){
		Lang.change(lang.toLowerCase());
		redirect("Mitglieder.list");
	}
}
