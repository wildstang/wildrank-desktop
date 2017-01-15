package org.wildstang.wildrank.desktopv2;

import java.awt.BorderLayout;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;

/*
 * MakeCSV is a JPanel that shows the progress of creating csv files 
 */
public class MakeCSV extends JPanel {
	JLabel progress = new JLabel("Progress");
	JProgressBar bar = new JProgressBar();
	Database database;

	// constructed called when panel is added to frame
	public MakeCSV() {
		try {
			database = DatabaseManager.getInstance().getDatabase();
		} catch (CouchbaseLiteException | IOException e) {
			e.printStackTrace();
		}
		add(progress, BorderLayout.NORTH);
		add(bar, BorderLayout.SOUTH);
	}

	// runs the csv writer
	public void run() {
		try {
			writeCSV();
		} catch (IOException | CouchbaseLiteException e) {
			e.printStackTrace();
		}
	}

	// updates the panel on the progress
	public void updateProgress(String update, int done, int total) {
		progress.setText(update);
		bar.setValue(done);
		bar.setMaximum(total);
	}

	// writers the data to csv files
	public void writeCSV() throws IOException, CouchbaseLiteException {
		// warning this is sketchy as hell especially with match results
		Query allDocsQuery = database.createAllDocumentsQuery();
		QueryEnumerator result = allDocsQuery.run();
		String type = "";
		BufferedWriter bw = new BufferedWriter(new FileWriter("blank.csv"));
		int counter = 0;
		for (Iterator<QueryRow> it = result; it.hasNext();) {
			QueryRow row = it.next();
			Document doc = row.getDocument();
			updateProgress("", counter, allDocsQuery.getLimit());
			counter++;
			// System.out.println("Document contents: " + doc.getProperties());
			List<String> subKeys = new ArrayList<>();
			if (doc.getProperty("type") != null) {
				// team and match are broken
				if (!doc.getProperty("type").equals("match") && !doc.getProperty("type").equals("team")) {
					Iterator<Entry<String, Object>> props = doc.getProperties().entrySet().iterator();
					Entry<String, Object> prop = props.next();
					if (!(((String) doc.getProperty("type")).equals(type))) {
						subKeys = new ArrayList<>();
						bw.close();
						type = (String) doc.getProperty("type");
						bw = new BufferedWriter(new FileWriter(type + ".csv"));
						while (props.hasNext()) {
							String key = prop.getKey();
							Object value = prop.getValue();
							bw.write(key + ",");
							if (value instanceof Map<?, ?>) {
								Map<String, Object> subMap = (Map<String, Object>) value;
								Iterator<Entry<String, Object>> subProps = subMap.entrySet().iterator();
								Entry<String, Object> subProp = props.next();
								while (subProps.hasNext()) {
									String subKey = subProp.getKey();
									Object subValue = subProp.getValue();
									subProp = subProps.next();
									boolean found = false;
									for (int i = 0; i < subKeys.size(); i++) {
										if (subKeys.get(i).equals(subKey)) {
											found = true;
										}
									}
									if (!found) {
										subKeys.add(subKey);
										bw.write(subKey + ",");
									}
								}
							}
							prop = props.next();
						}
						bw.write("\n");

					} else {
						bw = new BufferedWriter(new FileWriter(type + ".csv"));
						while (props.hasNext()) {
							String key = prop.getKey();
							Object value = prop.getValue();
							bw.write(key + ",");
							if (value instanceof Map<?, ?>) {
								Map<String, Object> subMap = (Map<String, Object>) value;
								Iterator<Entry<String, Object>> subProps = subMap.entrySet().iterator();
								Entry<String, Object> subProp = props.next();
								while (subProps.hasNext()) {
									String subKey = subProp.getKey();
									Object subValue = subProp.getValue();
									subProp = subProps.next();
									boolean found = false;
									for (int i = 0; i < subKeys.size(); i++) {
										if (subKeys.get(i).equals(subKey)) {
											found = true;
										}
									}
									if (!found) {
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
					while (props.hasNext()) {
						String key = prop.getKey();
						Object value = prop.getValue();
						if (value != null) {
							String string = value.toString().replace(",", ";");
							bw.write(string + ",");
							System.out.println("Writing: " + string);
							if (value instanceof Map<?, ?>) {
								System.out.println("Writing Map");
								Map<String, Object> subMap = (Map<String, Object>) value;
								for (int i = 0; i < subKeys.size(); i++) {
									if (subMap.get(subKeys.get(i)) != null) {
										bw.write(subMap.get(subKeys.get(i)).toString() + ",");
									} else {
										bw.write(",");
									}
								}
							}
						} else {
							System.out.println(doc.getProperty("type") + ": " + key + " is null");
						}
						prop = props.next();
					}
					bw.write("\n");
					bw.flush();
				}
			}
		}
		bw.close();
		progress.setText("Done");
	}
}
