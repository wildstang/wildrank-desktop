package org.wildstang.wildrank.desktopv2;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class UserRow extends JPanel
{
	JTextField id;
	JTextField name;
	JCheckBox admin;
	User user;
	
	public UserRow(User user)
	{
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
	
	public UserRow()
	{
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
	
	public User getUser()
	{
		return new User(id.getText(), name.getText(), user.documentId, admin.isSelected());
	}
}
