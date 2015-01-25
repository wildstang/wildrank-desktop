package org.wildstang.wildrank.desktopv2;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.json.JSONArray;
import org.json.JSONObject;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.JavaContext;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryOptions;
import com.couchbase.lite.QueryRow;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ModifyUsers extends JPanel implements ActionListener {
	JButton save;
	JButton add;
	GridBagConstraints c;

	List<UserRow> users = new ArrayList<UserRow>();

	public ModifyUsers() {
		setLayout(new GridBagLayout());
		save = new JButton("Save");
		save.addActionListener(this);
		add = new JButton("Add Another");
		add.addActionListener(this);
		try {
			loadUsers();
		} catch (IOException | CouchbaseLiteException e) {
			e.printStackTrace();
		}
		render();
	}

	public void render() {
		c = new GridBagConstraints();
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
		Database database = DatabaseManager.getInstance().getDatabase();
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
				users.add(new UserRow(new User((String) user.get("id"), (String) user.get("name"), document.getId(), (Boolean) user.get("admin"))));
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
	}

	public void save() {
		System.out.println("Creating database of users");

		try {

			Database database = DatabaseManager.getInstance().getDatabase();

			for (int i = 0; i < users.size(); i++) {
				User user = users.get(i).getUser();

				Document document;
				if (user.documentId != null) {
					document = database.getExistingDocument(user.documentId);
				} else {
					document = database.createDocument();
				}

				Map<String, Object> usermap = new HashMap<>();
				usermap.put("id", user.id);
				usermap.put("name", user.name);
				usermap.put("admin", user.admin);
				usermap.put("type", "user");
				System.out.println("User " + i + ": " + usermap.toString());

				document.putProperties(usermap);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
