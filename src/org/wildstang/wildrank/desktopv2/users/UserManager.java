package org.wildstang.wildrank.desktopv2.users;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class UserManager {

	private JFrame frame;
	private UserManagerPanel manager;

	public void show() {
		if (frame == null) {
			frame = new JFrame("WildRank Desktop v2: User Manager");
			frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			frame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					if (manager.isSafeToClose()) {
						frame.dispose();
					} else {
						int result = JOptionPane.showOptionDialog(frame,
								"You haven't saved to the database since your most recent edit. Do you still want to close?",
								"Close without saving", JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION, null,
								null, null);
						if (result == JOptionPane.YES_OPTION) {
							frame.dispose();
						}
					}
				}

				@Override
				public void windowClosed(WindowEvent e) {
					frame = null;
				}
			});

			manager = new UserManagerPanel();

			frame.add(manager);
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		}

		frame.toFront();
	}

}
