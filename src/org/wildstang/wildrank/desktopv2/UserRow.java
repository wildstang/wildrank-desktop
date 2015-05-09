package org.wildstang.wildrank.desktopv2;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/*
 * UserRow is a basic container for the User Manager 
 * It contains fields for the id name and admin of a user as well as a user
 */
public class UserRow extends JPanel {
	JTextField id;
	JTextField name;
	JCheckBox admin;
	User user;

	//constructor used when reading from file
	public UserRow(User user) {
		this.user = user;
		id = new JTextField(user.id, 6);
		name = new JTextField(user.name, 15);
		admin = new JCheckBox("admin?", user.admin);
		add(new JLabel("ID:"));
		add(id);
		add(new JLabel("Name:"));
		add(name);
		add(admin);
	}

	//constructor used for when a new user is created
	public UserRow() {
		user = new User();
		id = new JTextField("", 6);
		name = new JTextField("", 15);
		admin = new JCheckBox("admin?");
		add(new JLabel("ID:"));
		add(id);
		add(new JLabel("Name:"));
		add(name);
		add(admin);
	}

	//returns a user that is created from the data in the fields
	public User getUser() {
		return new User(id.getText(), name.getText(), admin.isSelected());
	}
}
