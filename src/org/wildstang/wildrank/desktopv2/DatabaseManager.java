package org.wildstang.wildrank.desktopv2;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.JavaContext;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Query;
import com.couchbase.lite.View;

public class DatabaseManager {
	private static DatabaseManager instance;

	private Manager manager;
	private Database database;

	public static DatabaseManager getInstance() throws CouchbaseLiteException, IOException {
		if (instance == null) {
			instance = new DatabaseManager(new JavaContext() {
				@Override
				public File getRootDirectory() {
					return WildRank.directory;
				}
			});
		}
		return instance;
	}

	private DatabaseManager(JavaContext context) throws CouchbaseLiteException, IOException {
		manager = new Manager(context, Manager.DEFAULT_OPTIONS);
		database = manager.getDatabase("wildrank");
		View userView = database.getView("user_view");
		userView.setMap(new Mapper() {

			@Override
			public void map(Map<String, Object> document, Emitter emitter) {
				Object docType = document.get("type");
				if (docType != null && docType.toString().equals("user")) {
					emitter.emit(document.get("user_id"), null);
				}
			}
		}, "7");
	}

	public Manager getManager() {
		return manager;
	}

	public Database getDatabase() {
		return database;
	}

	public Query getAllUsers() {
		Query query = database.getView("user_view").createQuery();
		query.setDescending(false);
		return query;
	}
	
	public void saveDatabase() {
		database.close();
		database.open();
	}
}
