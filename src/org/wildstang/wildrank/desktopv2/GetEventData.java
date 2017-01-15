package org.wildstang.wildrank.desktopv2;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.UnsavedRevision;
import com.fasterxml.jackson.databind.ObjectMapper;

/*
 * GetEventData is a JPanel that is run when fetching the data for an event to set up the app
 * this includes matches and teams
 */
public class GetEventData extends JPanel implements ActionListener {
	JTextField year;
	JTextField team;
	JButton fetch;
	JList events;
	Boolean eventFetched = false;

	List<String> eventKeys = new ArrayList<>();

	// constructor that is called when adding it to its JFrame
	public GetEventData() {
		// the panel contains 2 textfields for year and team as well as a button
		// to fetch the data
		// and jlabels to label all the things
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

	// run to get all the events the team is attending in a certain year
	public class EventFetcherThread extends Thread {

		@Override
		public void run() {
			System.out.println("Downloading events...");
			// gets the json (as a string) from the blue alliance for the
			// entered team's events in the entered year
			String json = Utils.getJsonFromUrl("http://www.thebluealliance.com/api/v2/team/frc" + team.getText() + "/"
					+ year.getText() + "/events");
			System.out.println("Events Downloaded!" + "\n" + json);

			// creates a json array based off the string gotten from tba
			JSONArray teamEvents = new JSONArray(json);
			try {
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						System.out.println("Clearing Panel.");
						// clears the panel
						removeAll();
						List<String> eventStrings = new ArrayList<>();
						System.out.println("Parsing Events...");
						// parses through all the events from the json and adds
						// the events shortName to an arraylist
						for (int i = 0; i < teamEvents.length(); i++) {
							JSONObject currentEvent = teamEvents.getJSONObject(i);
							eventKeys.add(i, currentEvent.getString("key"));
							String shortName;
							if (currentEvent.has("short_name") && !currentEvent.isNull("short_name")) {
								shortName = currentEvent.getString("short_name");
							} else {
								shortName = currentEvent.getString("name");
							}
							eventStrings.add(shortName);
						}
						events = new JList(eventStrings.toArray());
						events.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						events.setLayoutOrientation(JList.VERTICAL_WRAP);
						events.setVisibleRowCount(-1);
						// adds the event shortNames to a new scroll-able panel
						JScrollPane listScroller = new JScrollPane(events);
						listScroller.setPreferredSize(new Dimension(280, 35));
						listScroller.setViewportView(events);
						System.out.println("Events Parsed!");
						// changes the fetch button to download
						fetch.setText("Download");
						add(listScroller);
						add(fetch);
						// redraws the window
						GetEventData.this.revalidate();
					}
				});
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// defines what happens when buttons are pressed
		if (e.getSource().equals(fetch)) {
			// when the "Fetch Events" or "Download" button is pressed
			if (eventFetched) {
				// if data has already been fetched get the event data
				Thread t = new DatabaseCreatorThread(eventKeys.get(events.getSelectedIndex()));
				t.start();
			} else {
				// if data hasn't been fetched, fetch it
				eventFetched = true;
				Thread t = new EventFetcherThread();
				t.start();
			}
		}
	}

	// used to get the data for the chosen event
	public class DatabaseCreatorThread extends Thread {
		String eventKey = "";

		public DatabaseCreatorThread(String eventKey) {
			this.eventKey = eventKey;
		}

		@Override
		public void run() {
			System.out.println("Creating database with key: " + eventKey);

			// gets the match and team json from the blue alliance as a string
			String matches = Utils
					.getJsonFromUrl("http://www.thebluealliance.com/api/v2/event/" + eventKey + "/matches");
			String teams = Utils.getJsonFromUrl("http://www.thebluealliance.com/api/v2/event/" + eventKey + "/teams");

			try {
				// gets the database
				Database database = DatabaseManager.getInstance().getDatabase();

				// creates json arrays based off the strings from tba
				JSONArray matchesj = new JSONArray(matches);
				JSONArray teamsj = new JSONArray(teams);

				// parses through all the matches
				for (int i = 0; i < matchesj.length(); i++) {
					String matchString = matchesj.get(i).toString();
					// creates a map of all the data from the match
					Map<String, Object> match = new ObjectMapper().readValue(matchString, HashMap.class);
					match.put("type", "match");
					System.out.println("Match " + i + ": " + match.toString());
					// filter non-qualifying matches
					if (!match.get("comp_level").equals("qm")) {
						System.out.println("Non-qual match!");
						continue;
					}
					// gets the match key and creates the document name based
					// off it
					String matchKey = (String) match.get("key");
					System.out.println("match key:" + matchKey);
					String documentName = "match:" + matchKey;

					// finds the document or creates it if it doesn't exist
					Document document = database.getExistingDocument(documentName);
					if (document != null) {
						System.out.println("Match document exists... clearing");
					} else {
						document = database.getDocument(documentName);
						System.out.println("Match document doesn't exist... creating new document");
					}

					// saves the data to the document as a revision
					UnsavedRevision revision = document.createRevision();
					revision.setProperties(match);
					revision.save();
				}

				// parses through all the teams
				for (int i = 0; i < teamsj.length(); i++) {
					String teamString = teamsj.get(i).toString();
					// creates a map of all the data for the team
					Map<String, Object> team = new ObjectMapper().readValue(teamString, HashMap.class);
					team.put("type", "team");
					// gets the team key and creates the document name based off
					// it
					String teamKey = (String) team.get("key");
					System.out.println("team key:" + teamKey);
					String documentName = "team:" + teamKey;

					// finds the document or creates it if it doesn't exist
					Document document = database.getExistingDocument(documentName);
					if (document != null) {
						System.out.println("Team document exists... clearing");
					} else {
						document = database.getDocument(documentName);
						System.out.println("Team document doesn't exist... creating new document");
					}

					// saves the data to the document as a revision
					UnsavedRevision revision = document.createRevision();
					revision.setProperties(team);
					revision.save();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				// prints all the documents in the console
				DatabaseManager.getInstance().printAllDocuments();
			} catch (CouchbaseLiteException | IOException e) {
				e.printStackTrace();
			}

			// says it's done
			System.out.println("Done!");
			removeAll();
			add(new JLabel("Done!"));
			GetEventData.this.revalidate();
		}
	}
}
