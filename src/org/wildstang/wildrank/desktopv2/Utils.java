package org.wildstang.wildrank.desktopv2;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
		OkHttpClient client = new OkHttpClient();
		
		Request request = new Request.Builder()
				.url(url)
				.addHeader("X-TBA-App-Id", "frc111:scouting-system-desktop:v2.0")
				.build();
		try {
			Response response = client.newCall(request).execute();
			return response.body().string();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
