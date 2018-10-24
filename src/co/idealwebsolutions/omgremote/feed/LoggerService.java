package co.idealwebsolutions.omgremote.feed;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

/**
 * Logs all our convos (not in use for v1)
 */

public class LoggerService {
	
	private static LoggerService loggerService;
	
	private Context context;
	private String logDates;
	
	private static final String LOGS_FILE = "logs.json";
	private static final String TAG = "LoggerService";
	
	public static LoggerService newInstance() {
		return loggerService = new LoggerService();
	}
	
	public static LoggerService getInstance() {
		return loggerService;
	}
	
	public void init(Context context) {
		this.context = context;
	}
	
	public void writeOut(final String json) {
		boolean[] rwVars = verifyExternalAvail();
		FileWriter fr = null;
		if(rwVars[1]) { // means we're writable
			try {
				File f = new File(context.getExternalFilesDir(null), LOGS_FILE); 
				if(f != null) {
					fr = new FileWriter(f, true);
					fr.append(json);
					fr.flush();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					fr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			Log.e(TAG, "Could not write to directory");
		}
	}
	
	public String getInputFile(final String filename) {
		String temp = "";
		boolean[] rwVars = verifyExternalAvail();
		
		if(rwVars[0]) { // means we're readable
			File f = new File(context.getExternalFilesDir("Omegle Remote") + LOGS_FILE);
			if(!f.exists())
				return temp;
			try {
				String line = "";
				FileReader reader = new FileReader(f);
				BufferedReader bf = new BufferedReader(reader);
				while((line = bf.readLine()) != null) {
					temp += line;
				}
				bf.close();
				reader.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return temp;
	}
	
	public void addTag(final String tag) {
		if(logDates != null) {
			if(logDates.length() <= 0) {
				logDates = tag;
			} else {
				logDates += "," + tag;
			}
		}
	}
	
	public void commitChanges() {
		
	}
	
	private boolean[] verifyExternalAvail() {
		String state = Environment.getExternalStorageState();

		if(Environment.MEDIA_MOUNTED.equals(state)) 
			return new boolean[]{true, true};
		else if(Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
			return new boolean[]{true, false};
		else 
		    return new boolean[]{false, false};
	}
}
