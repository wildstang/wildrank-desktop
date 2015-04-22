package org.wildstang.wildrank.desktopv2;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.UnsavedRevision;

public class WildRank implements ActionListener {
	static JFrame frame;
	static JFrame userFrame;
	static JFrame csvFrame;

	JPanel panel;
	JButton users;
	JButton fixNotes;
	JButton csv;
	JButton addPictures;
	
	static File directory;

	public static void main(String[] args) {
		new WildRank();
	}

	public WildRank() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		users = new JButton("Manage Users");
		users.addActionListener(this);
		fixNotes = new JButton("Fix Notes");
		fixNotes.addActionListener(this);
		csv = new JButton("Write CSV");
		csv.addActionListener(this);
		addPictures = new JButton("Add Pictures");
		addPictures.addActionListener(this);
		frame = new JFrame("WildRank Desktop v2");
		panel = new GetEventData();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setPreferredSize(new Dimension(300, 150));
		JPanel top = new JPanel();
		top.add(fixNotes, BorderLayout.WEST);
		top.add(addPictures, BorderLayout.CENTER);
		top.add(csv, BorderLayout.EAST);
		frame.add(top, BorderLayout.PAGE_START);
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
		}
		else {
			directory = null;
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(users)) {
			userFrame = new JFrame("WildRank Desktop v2: User Manager");
			userFrame.setPreferredSize(new Dimension(400, 700));
			userFrame.setLocation(5, 5);
			JScrollPane scroll = new JScrollPane(new ModifyUsers());
			userFrame.add(scroll);
			userFrame.pack();
			userFrame.setVisible(true);
		}
		else if (e.getSource().equals(fixNotes)) {
			fixNotes();
		}
		else if(e.getSource().equals(csv))
		{
			csvFrame = new JFrame("WildRank Desktop v2: CSV Writer");
			csvFrame.setPreferredSize(new Dimension(300, 150));
			csvFrame.setLocation(5, 5);
			MakeCSV csvPanel;
			csvFrame.add(csvPanel = new MakeCSV());
			csvFrame.pack();
			csvFrame.setVisible(true);
			csvPanel.run();
		}
		else if(e.getSource().equals(addPictures))
		{
			loadPictures();
		}
	}
	
	public void loadPictures()
	{	
		JFileChooser chooser = new JFileChooser();
		File startFile = new File(System.getProperty("user.home"));
		chooser.setCurrentDirectory(chooser.getFileSystemView().getParentDirectory(startFile));
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle("Select the picture location");
		File picDir;
		if (chooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION)
		{
			JFrame loading = new JFrame("WildRank Desktop v2: Picture Loader");
			JPanel p = new JPanel();
			JLabel label = new JLabel("Loading...");
			p.add(label);
			loading.setLocation(5, 5);
			loading.setPreferredSize(new Dimension(150, 100));
			loading.add(p);
			loading.pack();
			loading.setVisible(true);
			
			picDir = chooser.getSelectedFile();
			String[] pics = picDir.list(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					return name.matches("[0-9]+\\.jpg");
				}
			});
			Database database;
			try {
				database = DatabaseManager.getInstance().getDatabase();
				for(int i = 0; i < pics.length; i++)
				{
					String infoString = "Adding: " + pics[i] + " " + Integer.toString(i+1) + "/" + Integer.toString(pics.length);
					label.setText(infoString);
					p.revalidate();
					System.out.print(infoString);
					String docName = "images:frc" + pics[i].replace(".jpg", "");
					System.out.println(" to " + docName);
					Document doc = database.getExistingDocument(docName);
					if(doc == null)
					{
						doc = database.getDocument(docName);
					}
					UnsavedRevision rev = doc.createRevision();
					BufferedInputStream stream = new BufferedInputStream(new FileInputStream(new File(picDir + File.separator + pics[i])));
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

	private void fixNotes() {
		try {
			Database database = DatabaseManager.getInstance().getDatabase();

			Query allDocsQuery = database.createAllDocumentsQuery();
			QueryEnumerator result = allDocsQuery.run();
			BufferedWriter bw = new BufferedWriter(new FileWriter("doc.txt"));

			database.beginTransaction();
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
			database.endTransaction(true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
