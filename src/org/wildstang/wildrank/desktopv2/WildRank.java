package org.wildstang.wildrank.desktopv2;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
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

	JPanel panel;
	JButton users;
	JButton fixNotes;
	JButton csv;
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
		frame = new JFrame("WildRank Desktop v2");
		panel = new GetEventData();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(300, 150));
		JPanel top = new JPanel();
		top.add(fixNotes, BorderLayout.WEST);
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
			try
			{
				writeCSV();
			}
			catch (IOException | CouchbaseLiteException e1)
			{
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

	public void writeCSV() throws IOException, CouchbaseLiteException
	{
		//warning this is sketchy as hell especially with match results
		Database database = DatabaseManager.getInstance().getDatabase();
		Query allDocsQuery = database.createAllDocumentsQuery();
		QueryEnumerator result = allDocsQuery.run();
		String type = "";
		BufferedWriter bw = new BufferedWriter(new FileWriter("blank.csv"));
		for (Iterator<QueryRow> it = result; it.hasNext();)
		{
			QueryRow row = it.next();
			Document doc = row.getDocument();
			// System.out.println("Document contents: " + doc.getProperties());
			List<String> subKeys = new ArrayList<>();
			if (doc.getProperty("type") != null)
			{
				//team and match are broken
				if (!doc.getProperty("type").equals("match") && !doc.getProperty("type").equals("team"))
				{
					Iterator<Entry<String, Object>> props = doc.getProperties().entrySet().iterator();
					Entry<String, Object> prop = props.next();
					if (!(((String) doc.getProperty("type")).equals(type)))
					{
						subKeys = new ArrayList<>();
						bw.close();
						type = (String) doc.getProperty("type");
						bw = new BufferedWriter(new FileWriter(type + ".csv"));
						while (props.hasNext())
						{
							String key = prop.getKey();
							Object value = prop.getValue();
							bw.write(key + ",");
							if(value instanceof Map<?,?>)
							{
								Map<String, Object> subMap = (Map<String, Object>)value;
								Iterator<Entry<String, Object>> subProps = subMap.entrySet().iterator();
								Entry<String, Object> subProp = props.next();
								while (subProps.hasNext())
								{
									String subKey = subProp.getKey();
									Object subValue = subProp.getValue();
									subProp = subProps.next();
									boolean found = false;
									for(int i = 0; i < subKeys.size(); i++)
									{
										if(subKeys.get(i).equals(subKey))
										{
											found = true;
										}
									}
									if(!found)
									{
										subKeys.add(subKey);
										bw.write(subKey + ",");
									}
								}
							}
							prop = props.next();
						}
						bw.write("\n");

					}
					else
					{
						bw = new BufferedWriter(new FileWriter(type + ".csv"));
						while (props.hasNext())
						{
							String key = prop.getKey();
							Object value = prop.getValue();
							bw.write(key + ",");
							if(value instanceof Map<?,?>)
							{
								Map<String, Object> subMap = (Map<String, Object>)value;
								Iterator<Entry<String, Object>> subProps = subMap.entrySet().iterator();
								Entry<String, Object> subProp = props.next();
								while (subProps.hasNext())
								{
									String subKey = subProp.getKey();
									Object subValue = subProp.getValue();
									subProp = subProps.next();
									boolean found = false;
									for(int i = 0; i < subKeys.size(); i++)
									{
										if(subKeys.get(i).equals(subKey))
										{
											found = true;
										}
									}
									if(!found)
									{
										subKeys.add(subKey);
										bw.write(subKey + ",");
									}
								}
							}
							prop = props.next();
						}
					}

					props = doc.getProperties().entrySet().iterator();
					prop = props.next();
					while (props.hasNext())
					{
						String key = prop.getKey();
						Object value = prop.getValue();
						if (value != null)
						{
							String string = value.toString().replace(",", ";");
							bw.write(string + ",");
							System.out.println("Writing: " + string);
							if(value instanceof Map<?,?>)
							{
								System.out.println("Writing Map");
								Map<String, Object> subMap = (Map<String, Object>)value;
								for(int i = 0; i < subKeys.size(); i++)
								{
									if(subMap.get(subKeys.get(i)) != null)
									{
										bw.write(subMap.get(subKeys.get(i)).toString() + ",");
									}
									else
									{
										bw.write(",");
									}
								}
							}
							prop = props.next();
						}
						else
						{
							System.out.println(doc.getProperty("type") + ": " + key + " is null");
						}
					}
					bw.write("\n");
					bw.flush();
				}
			}
		}
		bw.close();
	}
}
