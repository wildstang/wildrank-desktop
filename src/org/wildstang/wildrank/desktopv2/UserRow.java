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
	
	public UserRow(String id, String name, Boolean admin)
	{
		this.id = new JTextField(id, 6);
		this.name = new JTextField(name, 15);
		this.admin = new JCheckBox("admin?", admin);
		add(new JLabel("ID:"));
		add(this.id);
		add(new JLabel("Name:"));
		add(this.name);
		add(this.admin);
	}
}
