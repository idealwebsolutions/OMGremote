package co.idealwebsolutions.omgremote.feed;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import net.htmlparser.jericho.Source;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import co.idealwebsolutions.omgremote.manager.SettingsManager;
import co.idealwebsolutions.omgremote.model.ChatObject;
import co.idealwebsolutions.omgremote.model.Entity;
import co.idealwebsolutions.omgremote.util.StringOperations;
import co.idealwebsolutions.omgremote.web.HttpService;
import co.idealwebsolutions.omgremote.web.HttpService.HttpMethod;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

/**
 * The backbone service for this application
 */

public class OmegleService {
	
	private static OmegleService os;
	
	private OmegleChatSession chatSession;
	
	private volatile boolean running, isConnected;
	private String sid, serverUrl, serverOptions, curInterests, curQuestion, curMessage;
	private boolean typing;
	private byte curSessionType;
	
	private final static char AND = '&';
	private final static int[] SERVER_NUM = {1,2,3,4,5,7};
	private final static String[] INIT_PARAMS = {"rcs=1&spid=", "topics=", "ask=", "wantsspy="};
	private final static String TAG = "OmegleService";
	
	public enum Session {
		TEXT(Byte.valueOf("0")), COMMON_INTERESTS(Byte.valueOf("1")), 
		SPYER(Byte.valueOf("2")), SPYEE(Byte.valueOf("3")), WATCHER(Byte.valueOf("4"));
		
		private Byte type;
		
		Session(byte type) {
			this.type = type;
		}
		
		public Byte getType() {
			return type;
		}
		
		public Session getSession(Byte type) {
			switch(type) {
			case 0:
				return TEXT;
			case 1:
				return COMMON_INTERESTS;
			}
			return TEXT;
		}
	}
	
	public static OmegleService newInstance() {
		return os = new OmegleService();
	}
	
	public static OmegleService getInstance() {
		return os;
	}
	
	public void init() {
		this.serverUrl = "http://front" + SERVER_NUM[new Random().nextInt(SERVER_NUM.length)] + ".omegle.com/";
		this.serverOptions = "";
		this.sid = "";
		this.curInterests = "";
		this.curQuestion = "";
		this.curMessage = "";
	}
	
	public void resetServer() {
		this.serverUrl = "http://front" + SERVER_NUM[new Random().nextInt(SERVER_NUM.length)] + ".omegle.com/";
	}
	
	public synchronized boolean isRunning() {
		return running;
	}
	
	public synchronized void setRunning(boolean running) {
		this.running = running;
	}
	
	public String getServerUrl() {
		return serverUrl;
	}
	
	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}
	
	public String getId() {
		return sid;
	}
	
	public void setId(String sid) {
		this.sid = sid;
	}
	
	public boolean isConnected() {
		return isConnected;
	}
	
	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}
	
	public boolean isTyping() {
		return typing;
	}
	
	public void setTyping(boolean typing) {
		this.typing = typing;
	}
	
	public String getCurrentMessage() {
		return curMessage;
	}
	
	public void setMessage(String curMessage) {
		this.curMessage = curMessage;
	}
	
	public String getInterests() {
		return curInterests;
	}
	
	public void setInterests(String curInterests) {
		this.curInterests = StringOperations.toArray(curInterests);
	}
	
	public String getQuestion() {
		return curQuestion;
	}
	
	public void setQuestion(String question) {
		this.curQuestion = question;
	}
	
	public Session getCurrentSessionType() {
		return Session.values()[curSessionType];
	}
	
	public void setSessionType(Byte curSessionType) {
		this.curSessionType = curSessionType;
	}
	
	public OmegleChatSession getChatSession() {
		return chatSession;
	}
	
	public void startSession(final Session sessionType, final Fragment fragment, final boolean tempSession) throws UnsupportedEncodingException {
		chatSession = new OmegleChatSession(fragment);
		serverOptions = INIT_PARAMS[0];
		
		if(Session.COMMON_INTERESTS == sessionType && curInterests != null) {
			serverOptions += AND + INIT_PARAMS[1] + URLEncoder.encode(curInterests, "UTF-8");
		}
		if(Session.SPYER == sessionType && curQuestion != null) {
			serverOptions += AND + INIT_PARAMS[2] + URLEncoder.encode(curQuestion, "UTF-8");
		}
		if(Session.SPYEE == sessionType) {
			serverOptions += AND + INIT_PARAMS[3] + "1";
		}
		if(LoggerService.getInstance() == null) {
			LoggerService.newInstance();
			LoggerService.getInstance().init(fragment.getActivity());
		}
		chatSession.execute(serverOptions, String.valueOf(tempSession));
	}
	
	public void killSession(boolean revert) {
		if(!chatSession.isCancelled()) {
			chatSession.cancel(true);
		}
		if(isConnected()) {
			setConnected(false);
		}
		if(isRunning())
			setRunning(false);
		if(revert)
			serverOptions = "";
	}
	
	public void changeTypingStatus(boolean typing) {
		if(typing) 
			getPostResult(new String[]{"id"}, new String[]{sid}, "typing");
		else 
			getPostResult(new String[]{"id"}, new String[]{sid}, "stoppedtyping");
	}
	
	public final boolean disconnect() {
		String response = getPostResult(new String[]{"id"}, new String[]{sid}, "disconnect");
		setConnected(false);
		setRunning(false);
    return response != null;
	}
	
	/**
	 * Resets the entire sequence
	 */
	
	public final void reset() {
		disconnect();
		init();
	}
	
	public final String[] getServerStatus() {
		String[] status = parsePreload(getResult("status"));
		if(status != null)
			return status;
		return new String[]{"0"};
	}
	
	public final boolean sendMessage(final String message) {
		String result = "";
		//System.out.println("Message: " + message);
		if(message != null && message.length() > 0) {
			result = getPostResult(new String[]{"id", "msg"}, new String[]{sid, message}, "send");
			if(result != null) 
				return true;
		}
		return false;
	}
	
	private final String getResult(String typeUrl) { 
		String result = "";
		try {
			Source source = new Source(HttpService.getFromHttp(HttpService.HttpMethod.GET, serverUrl + typeUrl, null));
			//System.out.println("url: " + serverUrl + typeUrl);
			result = String.valueOf(source.getTextExtractor());
			//System.out.println("Result: " + result);
		} catch(Exception e) {
			Log.d(TAG, "IgnoredException: ", e);
		}
		return result = result != null ? result.replaceAll("\"", "") : "E";
	}
	
	private String getPostResult(String[] keys, String[] values, String typeUrl) {
		try {
			String eventSubmission = "";
			for(int i = 0; i < keys.length; i++) {
				eventSubmission += URLEncoder.encode(keys[i], "UTF-8") + "=" + URLEncoder.encode(values[i], "UTF-8");
				if(keys.length > 1) {
					eventSubmission += "&";
				}
			}
			Source source = new Source(HttpService.getFromHttp(HttpService.HttpMethod.POST, serverUrl + typeUrl, eventSubmission));
			String result = String.valueOf(source.getTextExtractor());
			return result;
		} catch(Exception e) {
			Log.d(TAG, "IgnoredException: ", e);
		}
		return "[]";
	}
	
	private String[] parsePreload(String result) {
		List<String> pre = new ArrayList<String>();
		if(!result.equals("null") && result.length() > 0) {
			try {
				JSONObject jObj = new JSONObject(result);
				for(int i = 0; i < jObj.length(); i++) {
					if(jObj.get("count") != null) {
						pre.add(String.valueOf(jObj.getInt("count")));
					}
				}
				String[] results = new String[pre.size()];
				int j = 0;
				for(String item : pre) {
					results[j++] = item;
				}
				return results;
			} catch (JSONException e) {
				Log.e(TAG, "JSONError: ", e);
			}
		}
		return new String[]{"0"};
	}
	
	public class OmegleChatSession extends AsyncTask<String, String, String> {
		
		private String curId, curTopic, curStranger, curStrangerTyping, curStrangerDisconnected;
		private ChatObject copiedChatObject;
		private boolean stopped, failed;
		private Timer timer;
		
		private OmegleEventListener eventListener;
		private Fragment fragment;
		
		public OmegleChatSession(Fragment fragment) {
			this.fragment = fragment;
			this.curId = "";
			this.curStranger = "Stranger";
			this.curTopic = "";
			this.curStranger = "";
			this.curStrangerTyping = "";
			this.curStrangerDisconnected = "";
			
			eventListener = (OmegleEventListener) fragment;
		}
		
		@Override
		protected void onPreExecute() {
			Toast.makeText(fragment.getActivity(), "Searching for a new conversation...", Toast.LENGTH_SHORT).show();
		}
		
		@Override
		protected String doInBackground(String... params) {
			if(OmegleService.getInstance() != null) {
				resetServer();
				startNew(params[0]);
				//obtainAddress();
				while(running) {
					String result = "", lastEle = "";
					result = getPostResult(new String[]{"id"}, new String[]{curId}, "events");
					if(stopped) {
						resetServer();
						startNew(INIT_PARAMS[0]);
						stopped = false;
						failed = true;
					}
					if(result != null && result.length() > 0 && !isCancelled() && !stopped) {
						//System.out.println("Received: " + result);
						try {
							if(!result.equals("null") && result.length() > 0) {
								JSONArray jArr = new JSONArray(result);
								for(int i = 0; i < jArr.length(); i++) {
									JSONArray subArr = (JSONArray) jArr.get(i);
									for(int j = 0; j < subArr.length(); j++) {
										String element = subArr.getString(j);
										
										if(element.equalsIgnoreCase("waiting")) {
											publishProgress("w", "Looking for someone you can chat with. Hang on.");
											if(getCurrentSessionType() == Session.COMMON_INTERESTS) {
												publishProgress("w", "It may take a little while to find someone with common interests. If no one is found, you will be connected to a completely random stranger instead.");
												timer = new Timer();
												timer.schedule(new TimerTask() {

													@Override
													public void run() { 
														if(!isConnected()) {
															publishProgress("rs");
														}
													}
													
												}, 12000);
											}
										}
										if(element.equalsIgnoreCase("connected")) {
											setConnected(true);
											publishProgress("c", "You're now chatting with a random stranger. Say hi!");
											if(failed && getCurrentSessionType() == Session.COMMON_INTERESTS) {
												publishProgress("c", "Omegle couldn't find anyone who shares interests with you, so this stranger is completely random. Try adding more interests!");
											}
										}
										if(element.equalsIgnoreCase("strangerDisconnected")) {
											if(getCurrentSessionType() == Session.SPYER) {
												publishProgress("d", " has disconnected.", getStrangerDisconnected());
											} else {
												publishProgress("d", "Your conversational partner has disconnected.", "");
												failed = false;
												setConnected(false);
												setRunning(false);
											}
										}
										if(element.equalsIgnoreCase("stoppedTyping")) {
											if(isConnected()) {
												publishProgress("s", "", "");
											}
										}
										if(element.equalsIgnoreCase("typing")) {
											if(isConnected()) {
												publishProgress("t", " is typing...", "Stranger");
											}
										}
										if(lastEle.equalsIgnoreCase("recaptchaRequired")) {
											Log.i(TAG, "recaptchaRequired caught: " + element);
											publishProgress("re", "Recaptcha found: Please restart the application/change ip address temporarily. This will be fixed in v2");
											setConnected(false);
											setRunning(false);
										}
										if(lastEle.equalsIgnoreCase("error")) {
											publishProgress("e", "Error: " + element);
										}
										if(lastEle.equalsIgnoreCase("gotMessage")) { // Fix this ASAP
											String oldElement = element;
											if(isConnected() && !element.equalsIgnoreCase("null") && !element.equalsIgnoreCase("spyDisconnected") 
													&& !element.equalsIgnoreCase("spyTyping") && !element.equalsIgnoreCase("spyMessage")) {
												if(SettingsManager.getInstance() != null && SettingsManager.getInstance().getTranslateStatus()) {
													if(StringOperations.ableToTranslate(element)) {
														String jString = getTranslatedResult(element);
														if(jString != null && !jString.equalsIgnoreCase("null") || jString != null && !jString.equalsIgnoreCase("?")) {
															element = StringOperations.JSONFactory.parseToString(jString, "responseData", "translatedText");
														}
													}
												}
												publishProgress("r", element, "Stranger: ");
											}
											setMessage(oldElement);
										}
										if(lastEle.equalsIgnoreCase("spyDisconnected")) {
											if(isConnected()) {
												publishProgress("d", " has disconnected.", element);
											}
										}
										if(lastEle.equalsIgnoreCase("spyTyping")) {
											if(isConnected()) {
												publishProgress("t", " is typing...", element);
											}
										}
										if(lastEle.equalsIgnoreCase("spyMessage")) {
											if(isConnected()) {
												setStranger(element);
											}
										}
										if(lastEle.equalsIgnoreCase("Stranger 1")) {
											if(isConnected() && !element.equalsIgnoreCase("null") && !element.equalsIgnoreCase("spyDisconnected") 
													&& !element.equalsIgnoreCase("spyTyping") && !element.equalsIgnoreCase("spyMessage")) {
												publishProgress("r", element, "Stranger 1: ");
											}
										} 
										if(lastEle.equalsIgnoreCase("Stranger 2")) {
											if(isConnected() && !element.equalsIgnoreCase("null") && !element.equalsIgnoreCase("spyDisconnected") 
													&& !element.equalsIgnoreCase("spyTyping") && !element.equalsIgnoreCase("spyMessage")) {
												publishProgress("r", element, "Stranger 2: ");
											}
										}
										if(lastEle.equalsIgnoreCase("question")) {
											if(isConnected())
												publishProgress("q", "Discuss: " + element);
										}
										if(lastEle.equalsIgnoreCase("commonLikes")) {
											publishProgress("l", "You and the stranger both like " + StringOperations.toStringArray(element));
										}
										lastEle = element;
									}
								}
							} else {
								try {
									Thread.sleep(100);
								} catch(Exception e) {}
							}
						} catch (JSONException e) {
							Log.e(TAG, "JSONException: ", e);
						}
					} else {
						cancel(false); // kill after 12 secs (common interests)
						Log.i(TAG, "Shutting down session thread");
						timer.purge();
					}
				}
			}
			return null;
		}
		
		@Override
		protected void onProgressUpdate(String...update) {
			if(update != null) {
				if(update[0].equalsIgnoreCase("c") || update[0].equalsIgnoreCase("l")) {
					eventListener.onConnected(update[1]);
				}
				if(update[0].equalsIgnoreCase("d")) { 
					eventListener.onStrangerDisconnected(update[1], new Entity(update[2], false));
					if(Session.SPYER == getCurrentSessionType()) {
						eventListener.onOfferSettingsChange("qs", getQuestion());
					}
					if(Session.COMMON_INTERESTS == getCurrentSessionType()) {
						eventListener.onOfferSettingsChange("is", StringOperations.toStringArray(getInterests()));
					}
				}
				if(update[0].equalsIgnoreCase("e")) {
					eventListener.onErrorMessage(update[1]);
				}
				if(update[0].equalsIgnoreCase("r")) {
					eventListener.onMessageReceived(update[1], new Entity(update[2], false));
				}
				if(update[0].equalsIgnoreCase("s")) {
					eventListener.onStrangerStopped(update[1], null);
				}
				if(update[0].equalsIgnoreCase("t")) { 
					eventListener.onStrangerTyping(update[1], new Entity(update[2], true));
				}
				if(update[0].equalsIgnoreCase("q")) {
					eventListener.onQuestionReceived(update[1]);
				}
				if(update[0].equalsIgnoreCase("w")) {
					eventListener.onConnecting(update[1]);
				}
				if(update[0].equalsIgnoreCase("re")) { 
					eventListener.onCaptchaCaught(update[1]);
				}
				if(update[0].equalsIgnoreCase("rs")) { 
					eventListener.onNoResponseFound(update[0]);
					stopped = true;
					timer.cancel();
					Toast.makeText(fragment.getActivity(), "Searching for a new conversation...", Toast.LENGTH_SHORT).show();
				}
			}
		}
		
		private void startNew(String param) {
			curId = getResult("start?" + param);
			if(curId != null) {
				if(curId.equalsIgnoreCase("404")) {
					setRunning(false);
					Toast.makeText(fragment.getActivity(), "404: Could not connect to Omegle", Toast.LENGTH_LONG).show();
					fragment.getActivity().finish();
				} else {
					setId(curId);
					setRunning(true);
				}
			}
		}
		
		private String getTranslatedResult(String element) {
			Source source;
			String url = "http://mymemory.translated.net/api/get?";
			try {
				url += "q=" + URLEncoder.encode(element, "UTF-8") + AND + "mt=1";
				url += AND + "langpair=" + SettingsManager.getInstance().getTranslateTo();
				source = new Source(HttpService.getFromHttp(HttpMethod.GET, url, null));
				return String.valueOf(source.getTextExtractor());
			} catch (IOException e) {
				Log.i(TAG, "Error obtaining translation", e);
			}
			return "?";
		}
		
		public String getCurrentTopic() {
			return curTopic;
		}
		
		public void setTopic(String curTopic) {
			this.curTopic = curTopic;
		}
		
		public String getCurrentStranger() {
			return curStranger;
		}
		
		public void setStranger(String curStranger) {
			this.curStranger = curStranger;
		}
		
		public String getStrangerTyping() {
			return curStrangerTyping;
		}
		
		public void setStrangerTyping(String curStrangerTyping) {
			this.curStrangerTyping = curStrangerTyping;
		}
		
		public String getStrangerDisconnected() {
			return curStrangerDisconnected;
		}
		
		public void setStrangerDisconnected(String curStrangerDisconnected) {
			this.curStrangerDisconnected = curStrangerDisconnected;
		}
		
		public ChatObject getCopiedObject() {
			return copiedChatObject;
		}
		
		public void copyChatObject(ChatObject copiedChatObject) {
			this.copiedChatObject = copiedChatObject;
		}
	}
	
	public interface OmegleEventListener {	
		public void onConnecting(final String event); 
		public void onConnected(final String event);
		public void onQuestionReceived(final String event);
		public void onNoResponseFound(final String event);
		public void onErrorMessage(final String event);
		public void onCaptchaCaught(final String event);
		public void onOfferSettingsChange(final String event, final String param);
		public void onStrangerDisconnected(final String event, final Entity who);
		public void onStrangerTyping(final String event, final Entity who);
		public void onStrangerStopped(final String event, final Entity who);
		public void onMessageReceived(final String event, final Entity who);
		
		
	}
}
