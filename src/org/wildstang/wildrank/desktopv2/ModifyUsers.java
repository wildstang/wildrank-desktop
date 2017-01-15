package org.wildstang.wildrank.desktopv2;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
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

/*
 * ModifyUsers is a window that is used to add or edit users
 */
public class ModifyUsers extends JPanel implements ActionListener {
	JButton save;
	JButton add;
	JButton read;
	File file;
	GridBagConstraints c;

	List<UserRow> users = new ArrayList<UserRow>();

	//sets up the window
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

	//redraws the window
	public void render() {
		removeAll();
		c = new GridBagConstraints();
		add(read, c);
		c.gridy++;
		c.gridx = 0;
		System.out.println("Users: " + users.size());
		for (int i = 0; i < users.size(); i++) {
			c.gridy++;
			add(users.get(i), c);
			System.out.println(users.get(i).name.getText() + " (" + c.gridx + ", " + c.gridy + ")");
		}
		c.gridx = 0;
		c.gridy++;
		add(add, c);
		c.gridy++;
		add(save, c);
	}

	//loads users from documents and populates the window
	public void loadUsers() throws IOException, CouchbaseLiteException {
		//creates a query looking for documents for users
		Query query = DatabaseManager.getInstance().getAllUsers();
		QueryEnumerator enumerator = query.run();
		//creates an arraylist from the iterator returned from the query
		List<QueryRow> rows = new ArrayList<>();
		for (Iterator<QueryRow> it = enumerator; it.hasNext();) {
			rows.add(it.next());
		}

		//parses through all the documents from the query
		for (int i = 0; i < rows.size(); i++) {
			Document document = rows.get(i).getDocument();
			if (document != null) {
				Map<String, Object> user = document.getProperties();
				//creates a userrow based off the data from the document
				users.add(new UserRow(new User((String) user.get("id"), (String) user.get("name"), (Boolean) user.get("admin"))));
			} else {
				System.out.println("Document is null");
			}
		}
	}

	//responds to button presses
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(add)) {
			//if the "Add Another" button is pressed
			//create a new blank userrow
			users.add(new UserRow());
			render();
			WildRank.userFrame.pack();
		} else if (e.getSource().equals(save)) {
			//if the "Save" button is pressed
			//save the users
			save();
		} else if (e.getSource().equals(read)) {
			//if the "Read from CSV" button is pressed
			
			//prompt for location of CSV file
			JFileChooser chooser = new JFileChooser();
			File startFile = new File(System.getProperty("user.home"));
			chooser.setCurrentDirectory(chooser.getFileSystemView().getParentDirectory(startFile));
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setDialogTitle("Select the Local location");
			FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files", "csv");
			chooser.setFileFilter(filter);
			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				file = chooser.getSelectedFile();
				try {
					//when it is found read the users from the file
					readFromCSV();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			} else {
				file = null;
			}
			render();
		}
	}

	//takes the users from the fields in the userrows and puts them in documents
	public void save() {
		System.out.println("Creating database of users");

		try {
			//gets the database
			Database database = DatabaseManager.getInstance().getDatabase();

			//parses through all the users
			for (int i = 0; i < users.size(); i++) {
				User user = users.get(i).getUser();

				//creates a map of the users data
				Map<String, Object> usermap = new HashMap<>();
				usermap.put("id", user.id);
				usermap.put("name", user.name);
				usermap.put("admin", user.admin);
				usermap.put("type", "user");
				System.out.println("User " + i + ": " + usermap.toString());

				//creates a document for the user and adds the map to it
				Document document = database.getDocument("user:" + user.id);
				UnsavedRevision revision = document.createRevision();
				revision.setProperties(usermap);
				revision.save();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	//creates users and populates the window based on data from a csv file
	public void readFromCSV() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		List<String[]> rawUsers = new ArrayList<>();
		//creates a series of strings from the file (each line should be a user)
		while ((line = br.readLine()) != null) {
			rawUsers.add(line.split(","));
		}
		br.close();
		users.clear();
		//parses through strings
		for (int i = 0; i < rawUsers.size(); i++) {
			String id = rawUsers.get(i)[0].replace("\"", "");
			String name = rawUsers.get(i)[1].replace("\"", "");
			Boolean admin = Boolean.parseBoolean(rawUsers.get(i)[2].replace("\"", ""));
			//creates a new userrow for each found user
			users.add(new UserRow(new User(id, name, admin)));
		}
	}

}
