package controllers;

import models.User;

public class Security extends Secure.Security{
	
    static boolean authenticate(String username, String password) {
        User user = User.find("username", username).first();
        return user != null && user.authenticate(password);
    }
    
    static boolean check(String profile) {
        User user = User.find("username", connected()).first();
        if ("administrator".equals(profile)) {
            return user != null && user.isAdmin;
        } else if ("loggedin".equals(profile)) {
        	return user != null;
        } else {
            return false;
        }
    }
}
