package org.wildstang.wildrank.desktopv2;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.JavaContext;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.UnsavedRevision;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GetEventData extends JPanel implements ActionListener {
	JTextField year;
	JTextField team;
	JButton fetch;
	JList events;
	Boolean eventFetched = false;

	List<String> eventKeys = new ArrayList<>();

	public GetEventData() {
		year = new JTextField(4);
		team = new JTextField(4);
		fetch = new JButton("Fetch Events");
		fetch.addActionListener(this);
		add(new JLabel("Year: "));
		add(year);
		add(new JLabel("Team: "));
		add(team);
		add(fetch);
	}

	public void fetchEvent() {
		System.out.println("Downloading events...");
		String json = Utils.getJsonFromUrl("http://www.thebluealliance.com/api/v2/team/frc" + team.getText() + "/" + year.getText() + "/events");
		System.out.println("Events Downloaded!" + "\n" + json);

		JSONArray teamEvents = new JSONArray(json);
		try {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					System.out.println("Clearing Panel.");
					removeAll();
					List<String> eventStrings = new ArrayList<>();
					System.out.println("Parsing Events...");
					for (int i = 0; i < teamEvents.length(); i++) {
						JSONObject currentEvent = teamEvents.getJSONObject(i);
						eventKeys.add(i, currentEvent.getString("key"));
						String shortName;
						if (currentEvent.has("short_name") && !currentEvent.isNull("short_name")) {
							shortName = currentEvent.getString("short_name");
						}
						else {
							shortName = currentEvent.getString("name");
						}
						eventStrings.add(shortName);
					}
					events = new JList(eventStrings.toArray());
					events.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					events.setLayoutOrientation(JList.VERTICAL_WRAP);
					events.setVisibleRowCount(-1);
					JScrollPane listScroller = new JScrollPane(events);
					listScroller.setPreferredSize(new Dimension(280, 35));
					listScroller.setViewportView(events);
					System.out.println("Events Parsed!");
					fetch.setText("Download");
					add(listScroller);
					add(fetch);
					GetEventData.this.revalidate();
				}
			});
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(fetch)) {
			if (eventFetched) {
				createDatabase(eventKeys.get(events.getSelectedIndex()));
			}
			else {
				eventFetched = true;
				fetchEvent();
			}
		}
	}

	public void createDatabase(String eventKey) {
		System.out.println("Creating database with key: " + eventKey);

		String matches = Utils.getJsonFromUrl("http://www.thebluealliance.com/api/v2/event/" + eventKey + "/matches");
		String teams = Utils.getJsonFromUrl("http://www.thebluealliance.com/api/v2/event/" + eventKey + "/teams");

		try {
			Database database = DatabaseManager.getInstance().getDatabase();

			JSONArray matchesj = new JSONArray(matches);
			JSONArray teamsj = new JSONArray(teams);

			for (int i = 0; i < matchesj.length(); i++) {
				String matchString = matchesj.get(i).toString();

				Map<String, Object> match = new ObjectMapper().readValue(matchString, HashMap.class);
				match.put("type", "match");
				System.out.println("Match " + i + ": " + match.toString());
				// filter non-qualifying matches
				if (!match.get("comp_level").equals("qm")) {
					System.out.println("Non-qual match!");
					continue;
				}

				Document document = database.getDocument("match:" + matchString);
				UnsavedRevision revision = document.createRevision();
		        revision.setProperties(match);
		        revision.save();
			}

			for (int i = 0; i < teamsj.length(); i++) {
				String teamString = teamsj.get(i).toString();

				Map<String, Object> team = new ObjectMapper().readValue(teamString, HashMap.class);
				team.put("type", "team");
				System.out.println("Team " + i + ": " + team.toString());

				Document document = database.getDocument("team" + teamString);
				UnsavedRevision revision = document.createRevision();
		        revision.setProperties(team);
		        revision.save();
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		try {
			Query allDocsQuery = DatabaseManager.getInstance().getDatabase().createAllDocumentsQuery();
			QueryEnumerator result = allDocsQuery.run();
			for (Iterator<QueryRow> it = result; it.hasNext();) {
				QueryRow row = it.next();
				Document doc = row.getDocument();
				System.out.println("Document contents: " + doc.getProperties());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Done!");
		removeAll();
		add(new JLabel("Done!"));
		WildRank.frame.pack();
		GetEventData.this.revalidate();
	}
}
