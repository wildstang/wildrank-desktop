package org.wildstang.wildrank.desktopv2;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class WildRank implements ActionListener
{
	static JFrame frame;
	JPanel panel;
	JButton users;
	
	public static void main(String[] args)
	{
		new WildRank();
	}
	
	public WildRank()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e)
		{
			e.printStackTrace();
		}
		users = new JButton("Manage Users");
		users.addActionListener(this);
		frame = new JFrame("WildRank Desktop v2");
		panel = new GetEventData();
		frame.setPreferredSize(new Dimension(300, 85));
		frame.add(panel, BorderLayout.PAGE_START);
		frame.add(users, BorderLayout.PAGE_END);
		frame.pack();
		//frame.setResizable(false);
		frame.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource().equals(users))
		{
			
		}
	}

}