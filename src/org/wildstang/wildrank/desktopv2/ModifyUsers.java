package org.wildstang.wildrank.desktopv2;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
import com.couchbase.lite.QueryOptions;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ModifyUsers extends JPanel implements ActionListener
{
	JButton save;
	JButton add;
	GridBagConstraints c;
	
	List<UserRow> users = new ArrayList<UserRow>();
	
	public ModifyUsers()
	{
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
	
	public void render()
	{
		c = new GridBagConstraints();
		for(int i = 0; i < users.size(); i++)
		{
			c.gridy++;
			add(users.get(i), c);
		}
		c.gridy++;
		add(add, c);
		c.gridy++;
		add(save, c);
	}
	
	public void loadUsers() throws IOException, CouchbaseLiteException
	{
		File file = WildRank.file;
		file.mkdirs();
		JavaContext context = new JavaContext() {
			@Override
			public File getRootDirectory() {
				return WildRank.file;
			}
		};
		Manager manager = new Manager(context, Manager.DEFAULT_OPTIONS);
		Database database = manager.getDatabase("wildrank");
		System.out.println(database.getAllDocs(new QueryOptions()).size() + ", " + database.getDocumentCount());
		for(int i = 0; i < database.getDocumentCount(); i++)
		{
			Document document = (Document) database.getAllDocs(new QueryOptions()).get(i);
			if(document != null)
			{
				Map<String, Object> user = document.getProperties();
				JSONObject userj = new JSONObject(user.get(HashMap.class));
				users.add(new UserRow(userj.getString("id"), userj.getString("name"), userj.getBoolean("admin")));
			}
			else
			{
				System.out.println("Document is null");
			}
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource().equals(add))
		{
			users.add(new UserRow("", "", false));
			render();
			WildRank.userFrame.pack();
		}
		else if(e.getSource().equals(save))
		{
			createDatabase();
		}
	}

	public void createDatabase() {
		System.out.println("Creating database of users");


		try {

			File file = WildRank.file;
			file.mkdirs();
			JavaContext context = new JavaContext() {
				@Override
				public File getRootDirectory() {
					return WildRank.file;
				}
			};
			Manager manager = new Manager(context, Manager.DEFAULT_OPTIONS);
			Database database = manager.getDatabase("wildrank");

			for (int i = 0; i < users.size(); i++) {
				String id = users.get(i).id.getText();
				String name = users.get(i).name.getText();
				Boolean admin = users.get(i).admin.isSelected();
				
				JSONObject userj = new JSONObject();
				userj.put("id", id);
				userj.put("name", name);
				userj.put("admin", admin);
				Map<String, Object> user = new ObjectMapper().readValue(userj.toString(), HashMap.class);
				System.out.println("User " + i + ": " + user.toString());

				Document document = database.createDocument();
				document.putProperties(user);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
