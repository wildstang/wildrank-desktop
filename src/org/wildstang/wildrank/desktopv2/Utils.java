package org.wildstang.wildrank.desktopv2;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

/*
 * Utils
 * ------
 * A bunch of things that are used throughout the app
 */
public class Utils {
	/*
	 * getJsonFromUrl downloads json from a given url and saves it to the data base
	 */
	public static String getJsonFromUrl(String url) {
		//requests data from tba and tells it which app it is
		HttpClient httpclient = HttpClientBuilder.create().build();
		HttpGet httpget = new HttpGet(url);
		httpget.addHeader("X-TBA-App-Id", "frc111:scouting-system-desktop:v2.0");

		InputStream inputStream = null;
		String result = null;
		try {
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();

			//the data is caught
			inputStream = entity.getContent();
			// json is UTF-8 by default
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
			StringBuilder sb = new StringBuilder();

			//data is read through and made into a string
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			result = sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (inputStream != null)
					inputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//the data string is sent back
		return result;
	}
}
