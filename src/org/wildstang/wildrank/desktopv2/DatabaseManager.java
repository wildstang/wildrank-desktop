package org.wildstang.wildrank.desktopv2;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.JavaContext;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.View;

/*
 * DatabaseManager contains the database and queries
 */
public class DatabaseManager {
	private static DatabaseManager instance;
	private static File directory;

	private Manager manager;
	private Database database;
	
	public static void setDirectory(File directory) {
		DatabaseManager.directory = directory;
	}

	// used to access the database manager from a static context
	public static DatabaseManager getInstance() throws CouchbaseLiteException, IOException {
		if (directory == null) {
			throw new RuntimeException("DatabaseManager.setDirectory() must be called before an instance of DatabaseManager can be created.");
		}
		if (instance == null) {
			// if there isn't one create a new on
			instance = new DatabaseManager(new JavaContext() {
				@Override
				public File getRootDirectory() {
					return directory;
				}
			});
		}
		// then return the current one
		return instance;
	}
	
	/**
	 * Releases all databases that were opened by this instance. Used to prep the
	 * flash drive before ejecting.
	 */
	public static void disposeInstance() {
		if (instance != null) {
			instance.manager.close();
		}
		instance = null;
	}

	// constructor used to setup the database manager when getInstance() is
	// called for the first time
	private DatabaseManager(JavaContext context) throws CouchbaseLiteException, IOException {
		manager = new Manager(context, Manager.DEFAULT_OPTIONS);
		// gets the database based off its name
		database = manager.getDatabase("wildrank");
		// gets the view based off its name
		View userView = database.getView("user_view");
		userView.setMap(new Mapper() {

			@Override
			public void map(Map<String, Object> document, Emitter emitter) {
				Object docType = document.get("type");
				if (docType != null && docType.toString().equals("user")) {
					emitter.emit(document.get("id"), null);
				}
			}
		}, "7");
	}

	// returns the manager
	public Manager getManager() {
		return manager;
	}

	// returns the database
	public Database getDatabase() {
		return database;
	}

	// returns a query of all the users
	public Query getAllUsers() {
		Query query = database.getView("user_view").createQuery();
		query.setDescending(false);
		return query;
	}

	// saves the state of the database to file
	public void saveDatabase() throws CouchbaseLiteException {
		database.close();
		database.open();
	}

	// prints out the properties of all the documents in the console
	public void printAllDocuments() {
		try {
			// creates a query for all the documents
			Query allDocsQuery = DatabaseManager.getInstance().getDatabase().createAllDocumentsQuery();
			QueryEnumerator result = allDocsQuery.run();
			for (Iterator<QueryRow> it = result; it.hasNext();) {
				QueryRow row = it.next();
				Document doc = row.getDocument();
				// prints out the document's properties
				System.out.println("Document contents: " + doc.getProperties());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
