package org.wildstang.wildrank.desktopv2;

//a basic container for a user's id, name, and admin status
public class User {
	String id;
	String name;
	boolean admin;

	//it can be created with an id, name, and admin status
	public User(String id, String name, boolean admin) {
		this.id = id;
		this.name = name;
		this.admin = admin;
	}

	//or as a blank user
	public User() {
	}
}
