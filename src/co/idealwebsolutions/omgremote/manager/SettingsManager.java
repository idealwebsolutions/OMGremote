package co.idealwebsolutions.omgremote.manager;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {
	
	private static SettingsManager settingsManager;
	
	private SharedPreferences sharedPrefs;
	
	private static final String PREFS_FILE = "pf.omg";
	public static final String THEME_TAG = "theme", RE_TAG = "autoReconnect", R_TAG = "autoResponse", Q_TAG = "autoQuestion", 
			TRANS_TAG = "translate", LANG_TAG = "lang";
	
	public static SettingsManager newInstance() {
		return settingsManager = new SettingsManager();
	}
	
	public static SettingsManager getInstance() {
		return settingsManager;
	}
	
	public boolean init(Activity activity) {
		if(sharedPrefs == null) {
			sharedPrefs = activity.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
			return true;
		}
		return false;
	}
	
	public SharedPreferences getPrefs() {
		return sharedPrefs;
	}
	
	public boolean isThemeDay() {
		if(sharedPrefs != null)
			return sharedPrefs.getBoolean(THEME_TAG, true);
		return false;
	}
	
	public boolean isOnAutoReconnect() {
		if(sharedPrefs != null)
			return sharedPrefs.getBoolean(RE_TAG, true);
		return false;
	}
	
	public String getAutoResponse() {
		if(sharedPrefs != null)
			return sharedPrefs.getString(R_TAG, "");
		return "";
	}
	
	public String getAutoQuestion() {
		if(sharedPrefs != null)
			return sharedPrefs.getString(Q_TAG, "");
		return "";
	}
	
	public String getTranslateTo() {
		if(sharedPrefs != null)
			return sharedPrefs.getString(LANG_TAG, "en|it");
		return "en";
	}
	
	public boolean getTranslateStatus() {
		if(sharedPrefs != null)
			return sharedPrefs.getBoolean(TRANS_TAG, false);
		return false;
	}
}
