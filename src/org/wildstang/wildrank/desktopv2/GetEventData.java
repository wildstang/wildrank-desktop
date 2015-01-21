package org.wildstang.wildrank.desktopv2;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GetEventData extends JPanel implements ActionListener {
	JTextField year;
	JTextField team;
	JButton fetch;

	List<String> eventKeys = new ArrayList<>();
	List<JButton> eventButtons = new ArrayList<>();

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

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(fetch)) {
			System.out.println("Downloading events...");
			String json = Utils.getJsonFromUrl("http://www.thebluealliance.com/api/v2/team/frc" + team.getText() + "/" + year.getText() + "/events");
			System.out.println("Events Downloaded!"  + "\n" + json);
			
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
							eventKeys.add(currentEvent.getString("key"));
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
	}
}
