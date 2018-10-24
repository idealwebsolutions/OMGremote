package co.idealwebsolutions.omgremote.util;

import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import co.idealwebsolutions.omgremote.model.ChatObject;
import co.idealwebsolutions.omgremote.model.Entity;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * Helper methods
 */

public final class StringOperations {
	
	private final static char[] BAD_CHARACTERS = {'~', '`', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '-', '_', '+', '=',
			'{', '[', ']', '}', '|', '\\', ';', ':', '"', '\'', '<', '>', '.', '?', '/'};
	
	public static char verifyChars(String text) {
		for(int i = 0; i < text.length(); i++) {
			for(char bad : BAD_CHARACTERS) {
				if(text.charAt(i) == bad)
					return text.charAt(i);
			}
		}
		return 'K';
	}
	
	/**
	 * Translatable string
	 * @param text
	 * @return
	 */
	
	public static boolean ableToTranslate(String text) {
		if(text.length() < 1000) 
			if(text.length() >= 4)
				return true;
		return false;
	}
	
	/**
	 * Using Android's TextWatcher, scans for any strings with invalid chars
	 */
	
	public static TextWatcher Scanner = new TextWatcher() {
    	
    	private boolean stopped;
    	private int st = 0;

		@Override
		public void afterTextChanged(Editable s) {
			if(stopped) {
				s = s.delete(st, s.length());
				stopped = false;
			}
			
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			
			if(s.length() > 0 && s.length() != before && !stopped) {
				String str = s.toString();
				char c = verifyChars(str);
				if(c != 'K') {
					st = str.indexOf(c);
					stopped = true;
				}
			} 
		}
    	
    };
	
	/**
	 * Converts string to array (ex: ["a", "b"])
	 * @param s
	 * @return
	 */
	
	public static String toArray(String s) {
		String[] chopped = null;
		String array = "[";
		if(s.length() <= 0)
			return null;
		if(s.contains(",") && !s.equalsIgnoreCase(",")) {
			chopped = s.split(",");
			int count = 1;
			for(String word : chopped) {
				word = word.replaceAll(" ", "").trim();
				array += "\"" + word + "\"";
				if(count < chopped.length) 
					array += ",";
				count++;
			}
			array += "]";
		} else {
			array += "\"" + s + "\"";
			array += "]";
			return array;
		}
		return array;
	}
	
	/**
	 * Parse back to normal array with commas
	 * @param array
	 * @return
	 */
	
	public static String toStringArray(String array) {
		array = array.substring(1, array.length() - 1).trim();
		array = array.replace("\"", "").trim();
		array = array.replace(",", ", ");
		return array;
	}
	
	/**
	 * Parses out any possible links
	 */
	public static String linkify(String source) {
		if(!source.contains("<a>") && !source.contains("</a>") && source.contains("http")) { // check we do not have broken tags
			String dest = "", link = "";
			String[] words = source.split(" ");
			for(String word : words) {
				if(word.contains("http")) {
					int st = word.indexOf("http") == -1 ? 0 : word.indexOf("http");
					int en = word.length();
					link = word.substring(st, en);
					word = "<a href=\"" + link + "\">" + link + "</a>";
				}
				dest += word + " ";
			}
			return dest;
		}
		return source;
	}
	
	public static String pullLinkFromText(String text) {
		String link = "";
		String[] words = text.split(" ");
		for(String word : words) {
			if(word.startsWith("http")) {
				int st = word.indexOf("http") == -1 ? 0 : word.indexOf("http");
				int en = word.length();
				link = word.substring(st, en);
				break;
			}
		}
		return link;
	}
	
	public static final class LanguageFactory {
		
		public final static String getLanguage(int w) {
			switch(w) {
				case 0:
					return "en|en";
				case 1: // spanish (works)
					return "en|es";
				case 2: // italian (works)
					return "en|it";
				case 3: // german (works)
					return "en|de";
				case 4: // french (works)
					return "en|fr";
				case 5: // dutch (works)
					return "en|nl";
				case 6: // turkish (works)
					return "en|cy";
				case 7: // chinese (works)
					return "en|zh-cn";
				case 8: // russian (works)
					return "en|ru";
				case 9: // indonesian (works)
					return "en|id";
				case 10: // hindu (works)
					return "en|hi";
				case 11: // finnish (works)
					return "en|fi";
				case 12: // korean (works)
					return "en|ko-kr";
			}
			return "";
		}
	}
	
	public static final class JSONFactory {
		
		public static JSONObject buildToJSON(final String tag, final LinkedList<ChatObject> chatObjList) {
			JSONObject jsonObj = null;
			try {
				jsonObj = new JSONObject();
				jsonObj.put(tag, chatObjList);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return jsonObj;
		}
		
		public static String parseToString(final String result, final String jObj, final String jString) {
			JSONObject ob = null;
			try {
				ob = new JSONObject(result);
				return ob.getJSONObject(jObj).getString(jString);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return "?";
		}
		
		public static LinkedList<ChatObject> parseToObject(final String result, final String jsonTag) {
			LinkedList<ChatObject> tempList = new LinkedList<ChatObject>();
			JSONArray jArr = null;
			try {
				jArr = (JSONArray) new JSONObject(result).get(jsonTag); //getInputFile(LOGS_FILE)
				for(int i = 0; i < jArr.length(); i++) {
					String tm = ((JSONObject) jArr.get(i)).getString("timestamp");
					String chatline = ((JSONObject) jArr.get(i)).getString("chatline");
					JSONObject ent = (JSONObject) ((JSONObject) jArr.get(i)).get("entity");
					String name = ent.getString("name");
					boolean alert = ((JSONObject) jArr.get(i)).getBoolean("alert");
					boolean inQMode = ((JSONObject) jArr.get(i)).getBoolean("inQuestionMode");
					tempList.add(new ChatObject(tm, chatline, new Entity(name, false), alert, inQMode));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return tempList;
		}
	}
}
