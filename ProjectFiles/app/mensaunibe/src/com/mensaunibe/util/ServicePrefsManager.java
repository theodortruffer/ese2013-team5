package com.mensaunibe.util;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.mensaunibe.app.controller.Controller;

public class ServicePrefsManager {
	
	private static final String TAG = ServicePrefsManager.class.getSimpleName();
	
	private static Controller mController;

	private SharedPreferences mSettings;
	private Editor mSettingsEditor;
	
	public ServicePrefsManager(Controller controller) {
		mController = controller;
		
		// set up the shared prefs, just use the default ones
		mSettings = PreferenceManager.getDefaultSharedPreferences(mController);
		mSettingsEditor = mSettings.edit();
	}
	
	public Object getData(String type, String key) {
		Log.i(TAG, "getData(" + type + ", " + key + ")");
		
		Object setting = null;
		
		if (type.equals("string")) {
			setting = mSettings.getString(key, null);
		} else if (type.equals("boolean")) {
			setting = mSettings.getBoolean(key, false);
		} else if (type.equals("integer")) {
			setting = mSettings.getInt(key, 0);
		} else if (type.equals("float")) {
			setting = mSettings.getFloat(key, 0);
		} else {
			Log.e(TAG, "getData(): Type could not be resolved");
		}
		
		return setting;
	}
	
	/**
	 * Writes data to the shared prefs
	 * @param type the type of data to be saved
	 * @param key the key for the setting/data
	 * @param value the actual data to persist
	 */
	public void setData(String type, String key, Object value) {
		Log.i(TAG, "setData(" + type + ", " + key + ", value)");
		if (type == "string") {
			mSettingsEditor.putString(key, (String) value);
		} else if (type.equals("boolean")) {
			mSettingsEditor.putBoolean(key, (Boolean) value);
		} else if (type.equals("integer")) {
			Log.i(TAG, "setData(" + type + ", " + key + ", " + value + ")");
			mSettingsEditor.putInt(key, (Integer) value);
		} else if (type.equals("float")) {
			mSettingsEditor.putFloat(key, (Float) value);
		} else {
			Log.e(TAG, "setData(): Type could not be resolved");
		}
		// write persistent data
		mSettingsEditor.commit();
	}
}
