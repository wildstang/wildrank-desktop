package org.wildstang.wildrank.desktopv2;

public class User
{
	String id;
	String name;
	String documentId;
	boolean admin;
	
	public User(String id, String name, String documentId, boolean admin)
	{
		this.id = id;
		this.name = name;
		this.documentId = documentId;
		this.admin = admin;
	}
	
	public User(){}
}
