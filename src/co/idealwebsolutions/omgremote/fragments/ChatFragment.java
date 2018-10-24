package co.idealwebsolutions.omgremote.fragments;

import java.util.Calendar;
import java.util.LinkedList;

import co.idealwebsolutions.omgremote.ChatActivity;
import co.idealwebsolutions.omgremote.R;
import co.idealwebsolutions.omgremote.feed.LoggerService;
import co.idealwebsolutions.omgremote.feed.OmegleService;
import co.idealwebsolutions.omgremote.feed.OmegleService.OmegleEventListener;
import co.idealwebsolutions.omgremote.feed.OmegleService.Session;
import co.idealwebsolutions.omgremote.manager.SettingsManager;
import co.idealwebsolutions.omgremote.manager.TaskManager;
import co.idealwebsolutions.omgremote.model.ChatObject;
import co.idealwebsolutions.omgremote.model.Entity;
import co.idealwebsolutions.omgremote.util.StringOperations;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ChatFragment extends Fragment implements OmegleEventListener {
	
	private static ChatFragment chatFragment;
	
	private RelativeLayout rl;
	private ListView chatList;
	private ChatAdapter chatAdapter;
	
	private CallbackListener cbListener;
	private boolean runningCheck;
	
	private final static String TAG = "ChatFragment";
	
	public static ChatFragment newInstance() {
		return chatFragment = new ChatFragment();
	}
	
	public static ChatFragment getInstance() {
		return chatFragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setRetainInstance(true);
        cbListener = (CallbackListener) activity;
        
        if(OmegleService.getInstance() != null && activity.getIntent() != null) {
			OmegleService.getInstance().setSessionType(activity.getIntent().getByteExtra("sessionType", Session.TEXT.getType()));
			prepareSession();
		}
        
        chatAdapter = new ChatAdapter(activity);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) { 
		View view = inflater.inflate(R.layout.chat_list, container, false); 
		
		rl = (RelativeLayout) view.findViewById(R.id.progress_state);
		chatList = (ListView) view.findViewById(R.id.chat_view);
		chatList.setAdapter(chatAdapter);
		
		refreshLayout();
		return view;
	}
	
	/**
	 * Connect event
	 */
	
	@Override
	public void onConnecting(final String event) {
		if(rl.isShown()) {
			rl.setVisibility(View.GONE);
		}
		chatAdapter.getChatLineList().add(chatAdapter.getCount(), new ChatObject(getCurrentTimeArray(), event, null, true, false));
		chatAdapter.notifyDataSetChanged();
		chatList.post(new Runnable() {
			@Override
			public void run() {
				chatList.setSelection(chatList.getCount() - 1);
			}
		});
		cbListener.setLeaveButton("Leave", false);
	}
	
	/**
	 * Connected Event
	 */
	
	@Override
	public void onConnected(final String event) {
		if(rl.isShown()) {
			rl.setVisibility(View.GONE);
		}
		checkForLast();
		chatAdapter.getChatLineList().add(chatAdapter.getCount(), new ChatObject(getCurrentTimeArray(), event, null, false, false));
		chatAdapter.notifyDataSetChanged();
		chatList.post(new Runnable() {
			@Override
			public void run() {
				chatList.requestFocusFromTouch();
				chatList.setSelection(chatList.getCount() - 1);
			}
		});
		cbListener.setPictureOption(true);
		cbListener.setLeaveButton("Leave", true);
		cbListener.setTextBar(true);
		cbListener.setSendButton("Send", true);
		if(OmegleService.getInstance().getCurrentSessionType() != Session.SPYEE) {
			sendPreMessage();
		}
	}
	
	/**
	 * Stranger disconnected event
	 */

	@Override
	public void onStrangerDisconnected(final String event, final Entity who) {
		checkForLast();
		chatAdapter.getChatLineList().add(chatAdapter.getCount(), new ChatObject(getCurrentTimeArray(), event, who, false, false));
		chatAdapter.getChatLineList().add(chatAdapter.getCount(), new ChatObject("SV", "Share this conversation", null, true, false));
		chatAdapter.notifyDataSetChanged();
		chatList.post(new Runnable() {
			@Override
			public void run() {
				chatList.setSelection(chatList.getCount() - 1);
			}
		});
		OmegleService.getInstance().killSession(false);
		cbListener.setLeaveButton("New", true);
		cbListener.setPictureOption(false);
		checkForReconnect();
	}
	
	/**
	 * Stranger has typed event
	 */

	@Override
	public void onStrangerTyping(final String event, final Entity who) {
		checkForLast();
		chatAdapter.getChatLineList().add(chatAdapter.getCount(), new ChatObject(getCurrentTimeArray(), event, who, true, false));
		chatAdapter.notifyDataSetChanged();
		chatList.post(new Runnable() {
			@Override
			public void run() {
				chatList.setSelection(chatList.getCount() - 1);
			}
		});
	}
	
	/**
	 * Stranger no longer typing event
	 */
	
	@Override
	public void onStrangerStopped(String event, Entity who) {
		checkForLast();
	}
	
	/**
	 * Received message event
	 */
	
	@Override
	public void onMessageReceived(final String event, final Entity who) {
		checkForLast();
		chatAdapter.getChatLineList().add(new ChatObject(getCurrentTimeArray(), event, who, false, false));
		chatAdapter.notifyDataSetChanged();
		chatList.post(new Runnable() {
			@Override
			public void run() {
				chatList.setSelection(chatList.getCount() - 1);
			}
		});
	}
	
	/**
	 * Question received event
	 */
	
	@Override
	public void onQuestionReceived(final String event) {
		checkForLast();
		chatAdapter.getChatLineList().add(new ChatObject(getCurrentTimeArray(), event, null, false, true));
		chatAdapter.notifyDataSetChanged();
		chatList.post(new Runnable() {
			@Override
			public void run() {
				chatList.setSelection(chatList.getCount() - 1);
			}
		});
		if(OmegleService.getInstance().getCurrentSessionType() == Session.SPYEE) {
			sendPreMessage();
		}
	}
	
	/**
	 * We received an unexpected error
	 */
	
	@Override
	public void onErrorMessage(final String event) {
		Toast.makeText(getActivity(), event, Toast.LENGTH_SHORT).show();
		Log.e(TAG, "Error occurred: " + event);
		getActivity().finishActivity(1);
	}
	
	@Override
	public void onOfferSettingsChange(final String event, final String param) {
		chatAdapter.getChatLineList().add(new ChatObject(param, event, null, true, false));
		chatAdapter.notifyDataSetChanged();
		chatList.post(new Runnable() {
			@Override
			public void run() {
				chatList.setSelection(chatList.getCount() - 1);
			}
		});
	}
	
	/**
	 * No response found event
	 */
	
	@Override
	public void onNoResponseFound(final String event) {
		if(!isCurrentlyConnected() && event.equalsIgnoreCase("reset")) {
			TaskManager.getInstance().pushTask('d', null);
			Log.i(TAG, "Nothing found, trying different");
			resetSession();
			rl.setVisibility(View.VISIBLE);
		}
	}
	
	/**
	 * TODO: Implement captcha detection.
	 */
	
	@Override
	public void onCaptchaCaught(String event) {
		Toast.makeText(getActivity(), event, Toast.LENGTH_SHORT).show();
  }
	
	/**
	 * Chat adapter
	 * @return
	 */
	
	public ChatAdapter getChatAdapter() {
		return chatAdapter;
	}
	
	/**
	 * Returns listview
	 * @return
	 */
	
	public ListView getChatlist() {
		return chatList;
	}
	
	/**
	 * Returns callback listener
	 * @return
	 */
	
	public CallbackListener getListener() {
		return cbListener;
	}
	
	/**
	 * Checks current list for last item to remove alerts
	 */
	
	public final void checkForLast() {
		if(chatAdapter.getChatLineList().size() > 0 && !runningCheck) {
			runningCheck = true;
			for(int i = 0; i < chatAdapter.getCount(); i++) {
				if(chatAdapter.getChatLineList().get(i) != null && chatAdapter.getChatLineList().get(i).isAlert()) {
					chatAdapter.getChatLineList().remove(i);
					chatAdapter.notifyDataSetChanged();
				}
			}
			runningCheck = false;
		}
	}
	
	/**
	 * Calls for any last minute preparations to the UI, then executing the service thread
	 */
	
	public final void prepareSession() {
		try {
			if(rl != null) {
				rl.setVisibility(View.VISIBLE);
			}
			OmegleService.getInstance().startSession(Session.values()[OmegleService.getInstance().getCurrentSessionType().getType()], this, false);
		} catch(Exception e) {
			Log.e(TAG, "Exception caught on prepare: ", e);
		}
	}
	
	/**
	 * Same as last, passes a temp session variable
	 * @param sessionType
	 */
	
	public final void prepareSession(final Session sessionType) {
		try {
			if(rl != null) {
				rl.setVisibility(View.VISIBLE);
			}
			OmegleService.getInstance().startSession(Session.values()[sessionType.getType()], this, true);
		} catch(Exception e) {
			Log.e(TAG, "Exception caught on prepare: ", e);
		}
	} 
	
	/**
	 * TODO: Remove redundancy
	 */
	
	private final void sendPreMessage() {
		if(OmegleService.getInstance() != null && Session.SPYER == OmegleService.getInstance().getCurrentSessionType() && SettingsManager.getInstance() != null) {
			if(SettingsManager.getInstance().getAutoQuestion().length() > 0) {
				TaskManager.getInstance().pushTask('a', SettingsManager.getInstance().getAutoQuestion());
				chatAdapter.getChatLineList().add(new ChatObject(ChatFragment.getInstance().getCurrentTimeArray(), SettingsManager.getInstance().getAutoQuestion(), new Entity(ChatActivity.YOU_TAG, false), false, false));
				chatAdapter.notifyDataSetChanged();
				chatList.post(new Runnable() {
					@Override
					public void run() {
						chatList.setSelection(chatList.getCount() - 1);
					}
				});
			}
		}
		if(OmegleService.getInstance() != null && OmegleService.getInstance().getCurrentSessionType() != Session.SPYER && SettingsManager.getInstance() != null) { //OmegleService.getInstance().getCurrentSessionType() == Session.TEXT
			if(SettingsManager.getInstance().getAutoResponse() != null && SettingsManager.getInstance().getAutoResponse().length() > 0) {
				TaskManager.getInstance().pushTask('a', SettingsManager.getInstance().getAutoResponse());
				chatAdapter.getChatLineList().add(new ChatObject(ChatFragment.getInstance().getCurrentTimeArray(), SettingsManager.getInstance().getAutoResponse(), new Entity(ChatActivity.YOU_TAG, false), false, false));
				chatAdapter.notifyDataSetChanged();
				chatList.post(new Runnable() {
					@Override
					public void run() {
						chatList.setSelection(chatList.getCount() - 1);
					}
				});
			}
		}
	}
	
	/**
	 * Checks for reconnect available
	 */
	
	public void checkForReconnect() {
		if(SettingsManager.getInstance() != null && SettingsManager.getInstance().getPrefs() != null) {
			if(SettingsManager.getInstance().isOnAutoReconnect()) { //OmegleService.getInstance().getCurrentSessionType() == Session.TEXT &&
				cbListener.setLeaveButton("New", false);
				resetSession();
				prepareSession();
			}
		}
	}
	
	/**
	 * Check chat instance is still alive
	 * @return bool
	 */
	
	public boolean isCurrentlyConnected() {
		return OmegleService.getInstance() != null && OmegleService.getInstance().isConnected();
	}
	
	public void refreshLayout() {
		if(chatAdapter != null && chatAdapter.getCount() != 0) {
			chatAdapter.getChatLineList().add(new ChatObject("", "", null, false, false));
			chatAdapter.getChatLineList().removeLast();
			chatAdapter.notifyDataSetChanged(); // trigger listview update
		}
	}
	
	public void resetSession() {
		chatAdapter.getChatLineList().clear();
		chatAdapter.notifyDataSetChanged();
	}
	
	public int[] getCurrentTimeArray() {
		Calendar now = Calendar.getInstance();
		if(now != null)
			return new int[]{now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), now.get(Calendar.SECOND)};
		return new int[]{0,0,0};
	}
	
	public String determineDate() {
		String date = "";
		Calendar now = Calendar.getInstance();
		if(now != null) {
			date = now.get(Calendar.YEAR) + "_" + now.get(Calendar.MONTH) + "_" + now.get(Calendar.DAY_OF_MONTH);
		}
		return date;
	}
	
	public static class ChatAdapter extends BaseAdapter {
		
		private LinkedList<ChatObject> chatLineList;
		
		private LayoutInflater inflater;
		
		private Context ctx;
		
		public ChatAdapter(Context ctx) {
			this.ctx = ctx;
			this.inflater = LayoutInflater.from(ctx);
			this.chatLineList = new LinkedList<ChatObject>();
		}

		@Override
		public int getCount() {
			return chatLineList.size();
		}

		@Override
		public Object getItem(int position) {
			return chatLineList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			
			if(convertView == null) {
				convertView = inflater.inflate(R.layout.chat_line_item, null);
				holder = new ViewHolder();
				holder.layout = (LinearLayout) convertView.findViewById(R.id.chatline_layout);
				holder.chatline = (TextView) convertView.findViewById(R.id.chat_line);
				holder.timestamp = (TextView) convertView.findViewById(R.id.timestamp);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			String chatline = "";
			//if(chatLineList.get(position) != null) {
				chatline = chatLineList.get(position).getChatline();
			//}
			String timestamp = "";
			//if(chatLineList.get(position) != null) {
				timestamp = chatLineList.get(position).getTimestamp();
			//}
			SpannableStringBuilder sb = null;
			
			Spanned sp = Html.fromHtml(StringOperations.linkify(chatline));
			
			if(!isThemeDay()) {
				holder.layout.setBackgroundColor(Color.BLACK);
				holder.chatline.setBackgroundColor(Color.BLACK);
				holder.chatline.setTextColor(Color.WHITE);
				holder.timestamp.setBackgroundColor(Color.BLACK);
				holder.timestamp.setTextColor(Color.WHITE);
			} else {
				holder.layout.setBackgroundColor(Color.WHITE);
				holder.chatline.setBackgroundColor(Color.WHITE);
				holder.chatline.setTextColor(Color.BLACK);
				holder.timestamp.setBackgroundColor(Color.WHITE);
				holder.timestamp.setTextColor(Color.BLACK);
			}
			
			if(chatline.startsWith("You: ")) {
				sb = new SpannableStringBuilder(sp);
				ForegroundColorSpan fcs = new ForegroundColorSpan(isThemeDay() ? Color.BLUE : Color.CYAN);
				sb.setSpan(fcs, 0, 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
			} else if(chatline.startsWith("Stranger") && !chatLineList.get(position).isAlert()) {
				sb = new SpannableStringBuilder(sp);
				ForegroundColorSpan fcs = new ForegroundColorSpan(Color.RED);
				if(chatline.startsWith("Stranger "))
					sb.setSpan(fcs, 0, 11, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
				else
					sb.setSpan(fcs, 0, 9, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
			} else {
				sb = new SpannableStringBuilder(sp);
				ForegroundColorSpan fcs = new ForegroundColorSpan(isThemeDay() ? Color.DKGRAY : Color.GRAY);
				sb.setSpan(fcs, 0, sp.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
			}
			
			if(chatLineList.get(position).isInQuestionMode()) {
				holder.layout.setBackgroundColor(Color.rgb(142, 251, 240));
				holder.timestamp.setVisibility(View.GONE);
				holder.chatline.setTextColor(Color.WHITE);
			} else {
				holder.timestamp.setVisibility(View.VISIBLE);
				holder.timestamp.setText(timestamp);
			}
			
			if(chatLineList.get(position).getTimestamp().equalsIgnoreCase("SV") && chatLineList.get(position).isAlert()) {
				holder.timestamp.setVisibility(View.GONE);
				holder.chatline.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						packageList();
					}
					
				});
			}
			if(chatLineList.get(position).getChatline().equalsIgnoreCase("is") && chatLineList.get(position).isAlert()) {
				final String modeParam = chatLineList.get(position).getTimestamp();
				holder.timestamp.setVisibility(View.GONE);
				holder.chatline.setText("Change my interests");
				holder.chatline.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						ChatFragment.getInstance().getListener().resetMode("is", modeParam);
					}
					
				});
			} else if(chatLineList.get(position).getChatline().equalsIgnoreCase("qs") && chatLineList.get(position).isAlert()) {
				final String modeParam = chatLineList.get(position).getTimestamp();
				holder.timestamp.setVisibility(View.GONE);
				holder.chatline.setText("Change my question");
				holder.chatline.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						ChatFragment.getInstance().getListener().resetMode("qs", modeParam);
					}
					
				});
			} else {
				holder.chatline.setText(sb);
				
				final int pos = position;
				holder.chatline.setOnLongClickListener(new OnLongClickListener() {

					@Override
					public boolean onLongClick(View v) {
						if(OmegleService.getInstance() != null && OmegleService.getInstance().getChatSession() != null) {
							if(!chatLineList.get(pos).isAlert()) {
								OmegleService.getInstance().getChatSession().copyChatObject(chatLineList.get(pos));
								Toast.makeText(ctx, "Saved to internal clipboard", Toast.LENGTH_SHORT).show();
							}
						}
						return false;
					}
					
				});
			}
			
			holder.chatline.setLinkTextColor(Color.RED);
			holder.chatline.setMovementMethod(LinkMovementMethod.getInstance());
			
			return convertView;
		}
		
		public LinkedList<ChatObject> getChatLineList() {
			return chatLineList;
		}
		
		/**
		 * Redundant call but it's helps ease of boilerplate code
		 * @return
		 */
		
		private boolean isThemeDay() {
			if(SettingsManager.getInstance() != null && SettingsManager.getInstance().getPrefs() != null) {
				if(!SettingsManager.getInstance().isThemeDay()) 
					return false;
			}
			return true;
		}
		
		/**
		 * Packages current list and converts it to Strings
		 */
		
		private void packageList() {
			if(LoggerService.getInstance() != null && TaskManager.getInstance() != null) {
				LinkedList<String> fList = new LinkedList<String>();
				for(ChatObject co : chatLineList) {
					fList.add(co.toString());
				}
				ChatFragment.getInstance().getListener().requestShare(fList);
			}
		}
		
		static class ViewHolder {
			LinearLayout layout;
			TextView chatline;
			TextView timestamp;
		}
	}
	
	public interface CallbackListener {
		public void setLeaveButton(final String name, final boolean enabled);
		public void setSendButton(final String name, final boolean enabled);
		public void setTextBar(final boolean enabled);
		public void setPictureOption(final boolean enable);
		
		public void resetMode(final String mode, final String modeParam);
		public void requestShare(final LinkedList<String> chatList);
	}
}
