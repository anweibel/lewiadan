package models;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Table;

import controllers.CRUD.Hidden;

import play.data.validation.MaxSize;
import play.db.jpa.Model;
import play.libs.Crypto;
import play.libs.Crypto.HashType;

@Entity
@Table(name = "users")
public class User extends Model{
	
	@MaxSize(64)
	public String username;
	
	@Hidden
	public String passwordHash;
	
	@MaxSize(64)
	@Hidden
	public String salt;
	
	public Boolean isAdmin;
	
	public String toString(){
		return username + (isAdmin?" (Administrator)":"");
	}
	
	public boolean authenticate(String password){
		
		String input = salt + password;
		String hash = Crypto.passwordHash(input, HashType.SHA512);
		
		return hash.equals(passwordHash);
	}
	
	public static User createNewUser(String username, String password,
			Boolean isAdmin) {
		if(isAdmin == null)
			isAdmin = Boolean.FALSE;
		
		User newUser = new User();
		
		newUser.username = username;
		newUser.salt = UUID.randomUUID().toString();
		newUser.passwordHash = Crypto.passwordHash(newUser.salt + password, HashType.SHA512);
		newUser.isAdmin = isAdmin;
		
		return newUser;
	}
}
