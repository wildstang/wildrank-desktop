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

public class WildRank implements ActionListener {
	static JFrame frame;
	static JFrame userFrame;

	JPanel panel;
	JButton users;
	static File directory;

	public static void main(String[] args) {
		new WildRank();
	}

	public WildRank() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		users = new JButton("Manage Users");
		users.addActionListener(this);
		frame = new JFrame("WildRank Desktop v2");
		panel = new GetEventData();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(300, 135));
		frame.add(panel, BorderLayout.CENTER);
		frame.add(users, BorderLayout.PAGE_END);
		frame.pack();
		// frame.setResizable(false);
		frame.setVisible(true);
		JFileChooser chooser = new JFileChooser();
		File startFile = new File(System.getProperty("user.home"));
		chooser.setCurrentDirectory(chooser.getFileSystemView().getParentDirectory(startFile));
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle("Select the flash drive location");
		if (chooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
			directory = chooser.getSelectedFile();
		} else {
			directory = null;
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(users)) {
			userFrame = new JFrame("WildRank Desktop v2: User Manager");
			userFrame.setPreferredSize(new Dimension(400, 500));
			userFrame.setLocation(frame.getX(), frame.getY() + 100);
			userFrame.add(new ModifyUsers());
			userFrame.pack();
			userFrame.setVisible(true);
		}
	}

}
