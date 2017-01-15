package org.wildstang.wildrank.desktopv2.users;

public class User {
	private String id;
	private String name;
	private boolean admin;
	private String documentId;

	// it can be created with an id, name, and admin status
	public User(String id, String name, boolean admin) {
		this.id = id;
		this.name = name;
		this.admin = admin;
	}

	// or as a blank user
	public User() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public String getDocumentId() {
		return documentId;
	}

	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}
}
