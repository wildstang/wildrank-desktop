package org.wildstang.wildrank.desktopv2;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.UnsavedRevision;

public class ModifyUsers extends JPanel implements ActionListener {
	JButton save;
	JButton add;
	JButton read;
	File file;
	GridBagConstraints c;

	List<UserRow> users = new ArrayList<UserRow>();

	public ModifyUsers() {
		setLayout(new GridBagLayout());
		save = new JButton("Save");
		save.addActionListener(this);
		add = new JButton("Add Another");
		add.addActionListener(this);
		read = new JButton("Read from CSV");
		read.addActionListener(this);
		try {
			loadUsers();
		} catch (IOException | CouchbaseLiteException e) {
			e.printStackTrace();
		}
		render();
	}

	public void render() {
		removeAll();
		c = new GridBagConstraints();
		add(read, c);
		c.gridy++;
		for (int i = 0; i < users.size(); i++) {
			c.gridy++;
			add(users.get(i), c);
		}
		c.gridy++;
		add(add, c);
		c.gridy++;
		add(save, c);
	}

	public void loadUsers() throws IOException, CouchbaseLiteException {
		Query query = DatabaseManager.getInstance().getAllUsers();
		QueryEnumerator enumerator = query.run();
		List<QueryRow> rows = new ArrayList<>();
		for (Iterator<QueryRow> it = enumerator; it.hasNext();) {
			rows.add(it.next());
		}

		for (int i = 0; i < rows.size(); i++) {
			Document document = rows.get(i).getDocument();
			if (document != null) {
				Map<String, Object> user = document.getProperties();
				users.add(new UserRow(new User((String) user.get("id"), (String) user.get("name"), (Boolean) user.get("admin"))));
			} else {
				System.out.println("Document is null");
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(add)) {
			users.add(new UserRow());
			render();
			WildRank.userFrame.pack();
		} else if (e.getSource().equals(save)) {
			save();
		}
		else if(e.getSource().equals(read))
		{
			JFileChooser chooser = new JFileChooser();
			File startFile = new File(System.getProperty("user.home"));
			chooser.setCurrentDirectory(chooser.getFileSystemView().getParentDirectory(startFile));
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setDialogTitle("Select the Local location");
			FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files", "csv");
			chooser.setFileFilter(filter);
			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				file = chooser.getSelectedFile();
				try
				{
					readFromCSV();
				}
				catch (IOException e1)
				{
					e1.printStackTrace();
				}
			} else {
				file = null;
			}
			render();
		}
	}

	public void save() {
		System.out.println("Creating database of users");

		try {

			Database database = DatabaseManager.getInstance().getDatabase();

			for (int i = 0; i < users.size(); i++) {
				User user = users.get(i).getUser();
				
				Map<String, Object> usermap = new HashMap<>();
				usermap.put("id", user.id);
				usermap.put("name", user.name);
				usermap.put("admin", user.admin);
				usermap.put("type", "user");
				System.out.println("User " + i + ": " + usermap.toString());

				Document document = database.getDocument("user:" + user.id);
				UnsavedRevision revision = document.createRevision();
		        revision.setProperties(usermap);
		        revision.save();
			}
			
			Query allDocsQuery = database.createAllDocumentsQuery();
	        QueryEnumerator result = allDocsQuery.run();
	        for (Iterator<QueryRow> it = result; it.hasNext(); ) {
	            QueryRow row = it.next();
	            Document doc = row.getDocument();
	            System.out.println("Document contents: " + doc.getProperties());
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public void readFromCSV() throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		List<String[]> rawUsers = new ArrayList<>(); 
		while((line = br.readLine()) != null)
		{
			rawUsers.add(line.split(","));
		}
		br.close();
		users.clear();
		for(int i = 0; i < rawUsers.size(); i++)
		{
			String id = rawUsers.get(i)[0].replace("\"", "");
			String name = rawUsers.get(i)[1].replace("\"", "");
			Boolean admin = Boolean.parseBoolean(rawUsers.get(i)[2].replace("\"", ""));
			users.add(new UserRow(new User(id, name, admin)));
		}
	}

}
