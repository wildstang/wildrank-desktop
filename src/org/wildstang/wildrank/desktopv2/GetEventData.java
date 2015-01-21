package org.wildstang.wildrank.desktopv2;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.json.JSONArray;
import org.json.JSONObject;

public class GetEventData extends JPanel implements ActionListener
{
	JTextField year;
	JTextField team;
	JButton fetch;
	
	public GetEventData()
	{
		year = new JTextField(4);
		team = new JTextField(4);
		fetch = new JButton("Fetch Events");
		add(year);
		add(team);
		add(fetch);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource().equals(fetch))
		{
			String json = Utils.getJsonFromUrl("http://www.thebluealliance.com/api/v2/team/frc" + team.getText() + "/" + year.getText() + "/events");
			JSONArray teamEvents = new JSONArray(json);
			int x = 0;
			int y = 0;
			removeAll();
			for (int i = 0; i < teamEvents.length(); i++) {
				JSONObject currentEvent = teamEvents.getJSONObject(i);
				eventKeys.add(currentEvent.getString("key"));
				String shortName = currentEvent.getString("short_name");
				eventButtons.add(new JButton(shortName));
				eventButtons.get(i).addActionListener(EventSelector.this);
				c.gridx = x;
				c.gridy = y;
				panel.add(eventButtons.get(i), c);
				x++;
				if (x == 3) {
					y++;
					x = 0;
				}
			}
		}
	}
}
