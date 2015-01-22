package org.wildstang.wildrank.desktopv2;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;

public class ModifyUsers extends JPanel implements ActionListener
{
	JButton save;
	JButton add;
	GridBagConstraints c;
	
	List<UserRow> users = new ArrayList<UserRow>();
	
	public ModifyUsers()
	{
		setLayout(new GridBagLayout());
		save = new JButton("Save");
		save.addActionListener(this);
		add = new JButton("Add Another");
		add.addActionListener(this);
		loadUsers();
		render();
	}
	
	public void render()
	{
		c = new GridBagConstraints();
		for(int i = 0; i < users.size(); i++)
		{
			c.gridy++;
			add(users.get(i), c);
		}
		c.gridy++;
		add(add, c);
		c.gridy++;
		add(save, c);
	}
	
	public void loadUsers()
	{
		users.add(new UserRow("715132", "Liam Fruzyna", true));
		users.add(new UserRow("217002", "Alex Guerra", false));
		users.add(new UserRow("", "", false));
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource().equals(add))
		{
			users.add(new UserRow("", "", false));
			render();
		}
	}

}
