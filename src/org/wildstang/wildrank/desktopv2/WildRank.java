package org.wildstang.wildrank.desktopv2;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class WildRank
{
	JFrame frame;
	JPanel panel;
	
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
		frame = new JFrame("WildRank Desktop v2");
		panel = new GetEventData();
		frame.setPreferredSize(new Dimension(500, 250));
		frame.add(panel);
		frame.pack();
		frame.setResizable(false);
		frame.setVisible(true);
	}

}
