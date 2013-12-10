package com.mensaunibe.util.tasks;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mensaunibe.app.controller.Controller;
import com.mensaunibe.app.model.DataHandler;
import com.mensaunibe.app.model.Mensa;
import com.mensaunibe.app.model.MensaList;
import com.mensaunibe.app.model.User;
import com.mensaunibe.util.ServicePrefsManager;
import com.mensaunibe.util.ServiceWebRequest;
import com.mensaunibe.util.database.DatabaseManager;

public class TaskCreateModel extends AsyncTask<Void, Integer, MensaList> {
	
	// for logging and debugging purposes
	private static final String TAG = TaskCreateModel.class.getSimpleName();
	
	@SuppressWarnings("unused")
	private Controller mController;
	private DataHandler mDataHandler;
	
	private ArrayList<TaskListener> mListeners;
	private ServiceWebRequest mWebService;
	private ServicePrefsManager mPersistenceService;
	private DatabaseManager mDBManager;

	
	public TaskCreateModel(Controller controller, Fragment fragment) {
		this.mController = controller;
		this.mDataHandler = Controller.getDataHandler();
		this.mWebService = mDataHandler.getWebService();
		this.mPersistenceService = mDataHandler.getPersistenceManager();
		this.mDBManager = mDataHandler.getDatabaseManager();
		this.mListeners = new ArrayList<TaskListener>();
	}
	
    public void addListener(TaskListener listener) {
    	Log.i(TAG, "addListener(" + listener + ")");
    	if (listener != null) {
    		mListeners.add(listener);
    	} else {
    		Log.e(TAG, "Listener was null!");
    	}
	}
    
    public void removeListener(TaskListener listener) {
    	Log.i(TAG, "removeListener(" + listener + ")");
    	mListeners.remove(listener);
    }
    
    public void removeListeners() {
    	Log.i(TAG, "removeListeners()");
    	mListeners.removeAll(mListeners);
    }

    protected void notifyOnTaskComplete(Object result) {
    	Log.i(TAG, "notifyOnTaskComplete(" + result + ")");
    	for (TaskListener mListener : mListeners) {
    		mListener.onTaskComplete(result);
    	}
    }

    protected void notifyOnProgressUpdate(int percent) {
        for (TaskListener mListener : mListeners) {
            mListener.onProgressUpdate(percent);
        }
    }
	
	@Override
    protected MensaList doInBackground(Void... params) {
		Log.i(TAG, "Starting background task to create the model");
		MensaList model = null;
		JsonObject jsonObj = null;
		final Gson gson = new Gson();
		
		if (mWebService.getLastUpdate() != (Integer) mPersistenceService.getData("integer", "lastupdate")) {
			Log.i(TAG, "Update check negative, trying to load JSON from API");
			jsonObj = mWebService.getJSON("http://api.031.be/mensaunibe/v1/?type=mensafull", 5000);
		}

		if (jsonObj != null) {
			model = createFromJSON(jsonObj, gson);
		} else {
			model = createFromSharedPrefs(model, gson);
		}
		
		setFavorites(model);
		
		// if the model still is null, a lot went wrong, now we try to load the data from the slower
		// (than shared prefs, which sit in the device RAM) database
		if (model == null) {
			Log.e(TAG, "Model still null after loading from prefs, trying the database!");	
			model = mDBManager.load();
		}
		
		//show fake progress
		for (int i = 0; i <= 100; i++) {
        	try {
            	Thread.sleep(10);
            	publishProgress(i);
        	} catch (InterruptedException e) {
            	return null;
        	}
    	}
		
    	return model;
    }

	private MensaList createFromSharedPrefs(MensaList model, final Gson gson) {
		JsonObject jsonObj;
		Log.e(TAG, "JSONObject from webservice was null, trying to load from shared prefs");
		String jsonStr = (String) mPersistenceService.getData("string", "model");
		if (jsonStr != null) {
			jsonObj = new JsonParser().parse(jsonStr).getAsJsonObject();
			model = gson.fromJson(jsonObj, MensaList.class);
		}
		return model;
	}

	private MensaList createFromJSON(JsonObject jsonObj, final Gson gson) {
		MensaList model;
		Log.i(TAG, "JSONObject from webservice successfully loaded");
		model = gson.fromJson(jsonObj, MensaList.class);
		
		//persist the json in shared prefs
		mPersistenceService.setData("string", "model", jsonObj.toString());
		
		// get the lastupate timestamp and persist it too
		mPersistenceService.setData("integer", "lastupdate", jsonObj.get("lastupdatetimestamp").getAsInt());
		return model;
	}

    private void setFavorites(MensaList model) {
    	List<Mensa> mensalist = model.getMensas();
    	for(Mensa mensa : mensalist){
    		mensa.setFavorite(mDBManager.isFavorite(mensa));
    	}
	}

	@Override
    protected void onPostExecute(MensaList model) {
        notifyOnTaskComplete(model);
        removeListeners();
    }

    @Override
    protected void onProgressUpdate(Integer... percent) {
        notifyOnProgressUpdate(percent[0]);
    }
}