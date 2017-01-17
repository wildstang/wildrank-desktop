package org.wildstang.wildrank.desktopv2;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.wildstang.wildrank.desktopv2.users.UserManager;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.TransactionalTask;
import com.couchbase.lite.UnsavedRevision;

public class WildRank implements ActionListener {
	private static JFrame frame;
	private static JFrame csvFrame;
	
	private UserManager userManager;

	private JPanel panel;
	private JButton users;
	private JButton csv;
	private JButton addPictures;
	private JButton eject;

	public static void main(String[] args) {
		new WildRank();
	}

	public WildRank() {
		try {
			// makes the windows look native to whatever os you are on
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		// Immediately prompt for database location
		File directory = null;
		while (directory == null) {
			JFileChooser chooser = new JFileChooser();
			File startFile = new File(System.getProperty("user.home"));
			chooser.setCurrentDirectory(chooser.getFileSystemView().getParentDirectory(startFile));
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setDialogTitle("Select the flash drive location");
			if (chooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
				directory = chooser.getSelectedFile();
			} else {
				JOptionPane.showMessageDialog(null, "You must select a directory.", "Error!", JOptionPane.ERROR_MESSAGE);
			}
		}
		DatabaseManager.setDirectory(directory);

		// button to add or edit users
		users = new JButton("Manage Users");
		users.addActionListener(this);

		// button to save data into csv
		csv = new JButton("Write CSV");
		csv.addActionListener(this);

		// button to attach pictures to team documents
		addPictures = new JButton("Add Pictures");
		addPictures.addActionListener(this);
		// its disabled because it kills the app
		addPictures.setEnabled(false);
		
		eject = new JButton("Close database to eject");
		eject.addActionListener(this);

		// basic window setup
		frame = new JFrame("WildRank Desktop v2");
		panel = new GetEventData();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// lines up everything in the window
		JPanel top = new JPanel();
		top.setLayout(new GridLayout(0, 1));
		top.add(users);
		top.add(addPictures);
		top.add(csv);
		top.add(eject);
		
		frame.add(panel, BorderLayout.PAGE_START);
		frame.add(top, BorderLayout.CENTER);

		// final setup of window
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// defines what happens when buttons are pressed
		if (e.getSource().equals(users)) {
			if (userManager == null) {
				userManager = new UserManager();
			}
			userManager.show();
		} else if (e.getSource().equals(csv)) {
			// when the "Write CSV" button is pressed
			// a new window is created and contains the MakeCSV panel
			csvFrame = new JFrame("WildRank Desktop v2: CSV Writer");
			csvFrame.setPreferredSize(new Dimension(300, 150));
			csvFrame.setLocation(5, 5);
			MakeCSV csvPanel;
			csvFrame.add(csvPanel = new MakeCSV());
			csvFrame.pack();
			csvFrame.setVisible(true);
			csvPanel.run();
		} else if (e.getSource().equals(addPictures)) {
			// when the "Add Pictures" button is pressed
			// the pictures are attached to respective documents
			loadPictures();
		} else if (e.getSource().equals(eject)) {
			try {
				DatabaseManager.disposeInstance();
				JOptionPane.showMessageDialog(null, "Databse closed successfully.", "Success!", JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception exception) {
				exception.printStackTrace();
				JOptionPane.showMessageDialog(null, "Database did not close properly.", "Error!", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	// called when the "Add Pictures button is pressed
	public void loadPictures() {
		// user is prompted to define where the pictures are located
		JFileChooser chooser = new JFileChooser();
		File startFile = new File(System.getProperty("user.home"));
		chooser.setCurrentDirectory(chooser.getFileSystemView().getParentDirectory(startFile));
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle("Select the picture location");
		File picDir;
		// if a directory is chosen
		if (chooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
			// a new frame is created that shows status
			JFrame loading = new JFrame("WildRank Desktop v2: Picture Loader");
			JPanel p = new JPanel();
			JLabel label = new JLabel("Loading...");
			p.add(label);
			loading.setLocation(5, 5);
			loading.setPreferredSize(new Dimension(150, 100));
			loading.add(p);
			loading.pack();
			loading.setVisible(true);

			// sorts through all pictures and only stores the ones that only
			// have numbers in their names
			picDir = chooser.getSelectedFile();
			String[] pics = picDir.list(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					return name.matches("[0-9]+\\.jpg");
				}
			});

			Database database;
			try {
				// the database is accessed
				database = DatabaseManager.getInstance().getDatabase();
				for (int i = 0; i < pics.length; i++) {
					// the team number is extracted from the name
					String infoString = "Adding: " + pics[i] + " " + Integer.toString(i + 1) + "/"
							+ Integer.toString(pics.length);
					label.setText(infoString);
					p.revalidate();
					System.out.print(infoString);
					// a document name is created with the team number
					String docName = "images:frc" + pics[i].replace(".jpg", "");
					System.out.println(" to " + docName);
					// a document is created with the name
					Document doc = database.getExistingDocument(docName);
					if (doc == null) {
						doc = database.getDocument(docName);
					}
					// a revision is created which has the image attached to it
					UnsavedRevision rev = doc.createRevision();
					BufferedInputStream stream = new BufferedInputStream(
							new FileInputStream(new File(picDir + File.separator + pics[i])));
					rev.setAttachment("pic", "image/jpeg", stream);
					rev.save();
				}
				System.out.println("Loading pictures completed!");
				label.setText("Done!");
				p.revalidate();
			} catch (CouchbaseLiteException | IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	// used when we had duplicate notes being made shouldn't be needed anymore
	private void fixNotes() {
		try {
			Database database = DatabaseManager.getInstance().getDatabase();

			Query allDocsQuery = database.createAllDocumentsQuery();
			QueryEnumerator result = allDocsQuery.run();
			BufferedWriter bw = new BufferedWriter(new FileWriter("doc.txt"));

			database.runInTransaction(new TransactionalTask() {

				@Override
				public boolean run() {
					try {
						for (Iterator<QueryRow> it = result; it.hasNext();) {
							QueryRow row = it.next();
							Document doc = row.getDocument();
							String type = (String) doc.getProperty("type");
							if (type != null) {
								if (type.equals("notes")) {
									// We need to fix this note
									List<String> notes = (List<String>) doc.getProperty("notes");
									List<String> newNotes = new ArrayList<>();
									for (String note : notes) {
										if (!newNotes.contains(note)) {
											newNotes.add(note);
										}
									}

									Map<String, Object> newProps = new HashMap<String, Object>(doc.getProperties());
									newProps.remove("notes");
									newProps.put("notes", newNotes);

									UnsavedRevision revision = doc.createRevision();
									revision.setProperties(newProps);
									revision.save();
								}
							}
						}
					} catch (CouchbaseLiteException e) {
						e.printStackTrace();
						return false;
					}

					return true;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
