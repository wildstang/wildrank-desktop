package org.wildstang.wildrank.desktopv2;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class WildRank implements ActionListener
{
	static JFrame frame;
	JPanel panel;
	JButton users;
	static File file;
	
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
		JFileChooser chooser = new JFileChooser();
		File startFile = new File(System.getProperty("user.home"));
		chooser.setCurrentDirectory(chooser.getFileSystemView().getParentDirectory(startFile));
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle("Select the Local location");
		if (chooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
			file = chooser.getSelectedFile();
		} else {
			file = null;
		}
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource().equals(users))
		{
			
		}
	}

}