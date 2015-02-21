package org.wildstang.wildrank.desktopv2;

public class User {
	String id;
	String name;
	boolean admin;

	public User(String id, String name, boolean admin) {
		this.id = id;
		this.name = name;
		this.admin = admin;
	}

	public User() {
	}
}
