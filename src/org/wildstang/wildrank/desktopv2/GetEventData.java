package org.wildstang.wildrank.desktopv2;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.JavaContext;
import com.couchbase.lite.Manager;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GetEventData extends JPanel implements ActionListener {
	JTextField year;
	JTextField team;
	JButton fetch;

	List<String> eventKeys = new ArrayList<>();
	List<JButton> eventButtons = new ArrayList<>();

	public GetEventData() {
		setLayout(new GridBagLayout());
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

	public void getDatas(int i) {
		System.out.println(eventKeys.get(i) + " data requested." + "\n" + "Downloading event data...");
		String event = Utils.getJsonFromUrl("http://www.thebluealliance.com/api/v2/event/" + eventKeys.get(i));
		String teams = Utils.getJsonFromUrl("http://www.thebluealliance.com/api/v2/event/" + eventKeys.get(i) + "/teams");
		String matches = Utils.getJsonFromUrl("http://www.thebluealliance.com/api/v2/event/" + eventKeys.get(i) + "/matches");
		System.out.println("Data downloaded! \n" + event + "\n" + teams + "\n" + matches);
		JSONObject eventjson = new JSONObject(event);
		JSONArray teamjson = new JSONArray(teams);
		JSONArray matchjson = new JSONArray(matches);
	}

	public void fetchEvent()
	{
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
					System.out.println("Parsing Events...");
					for (int i = 0; i < teamEvents.length(); i++) {
						JSONObject currentEvent = teamEvents.getJSONObject(i);
						eventKeys.add(i, currentEvent.getString("key"));
						String shortName;
						if (currentEvent.has("short_name") && !currentEvent.isNull("short_name")) {
							shortName = currentEvent.getString("short_name");
						} else {
							shortName = currentEvent.getString("name");
						}
						JButton eventButton = new JButton(shortName);
						eventButton.addActionListener(GetEventData.this);
						add(eventButton);
						eventButtons.add(i, eventButton);
					}
					System.out.println("Events Parsed!");
					GetEventData.this.revalidate();
				}
			});
		} catch (JSONException exception) {
			exception.printStackTrace();
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(fetch)) {
			fetchEvent();
		} else {
			for (int i = 0; i < eventButtons.size(); i++) {
				JButton button = eventButtons.get(i);
				if (e.getSource() == button) {
					createDatabase(eventKeys.get(i));
				}
			}
		}
	}

	public void createDatabase(String eventKey) {
		System.out.println("Creating database with key: " + eventKey);

		String json = Utils.getJsonFromUrl("http://www.thebluealliance.com/api/v2/event/" + eventKey + "/matches");

		try {

			File file = new File("C:\\Users\\Nathan\\WildRank\\");
			file.mkdirs();
			JavaContext context = new JavaContext() {
				@Override
				public File getRootDirectory() {
					return new File("C:\\Users\\Nathan\\WildRank\\");
				}
			};
			Manager manager = new Manager(context, Manager.DEFAULT_OPTIONS);
			Database database = manager.getDatabase("wildrank");

			JSONArray matches = new JSONArray(json);

			for (int i = 0; i < matches.length(); i++) {
				String matchString = matches.get(i).toString();

				Map<String, Object> match = new ObjectMapper().readValue(matchString, HashMap.class);
				System.out.println("Match " + i + ": " + match.toString());
				Map<String, Object> alliances = (Map<String, Object>) match.get("alliances");
				Map<String, Object> redAlliance = (Map<String, Object>) alliances.get("red");
				Object redTeams = redAlliance.get("teams");

				Document document = database.createDocument();
				document.putProperties(match);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Done!");
		removeAll();
		add(new JLabel("Done!"));
		WildRank.frame.pack();
	}
}
