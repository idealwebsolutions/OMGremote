package co.idealwebsolutions.omgremote;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;

import co.idealwebsolutions.omgremote.feed.OmegleService;
import co.idealwebsolutions.omgremote.feed.OmegleService.Session;

import co.idealwebsolutions.omgremote.fragments.ChatFragment;
import co.idealwebsolutions.omgremote.fragments.ChatFragment.CallbackListener;
import co.idealwebsolutions.omgremote.fragments.VideoChatFragment;
import co.idealwebsolutions.omgremote.manager.SettingsManager;
import co.idealwebsolutions.omgremote.manager.TaskManager;
import co.idealwebsolutions.omgremote.model.ChatObject;
import co.idealwebsolutions.omgremote.model.Entity;
import co.idealwebsolutions.omgremote.util.StringOperations;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public final class ChatActivity extends FragmentActivity implements CallbackListener {
	
	private LinearLayout mainLayout;
	private TextView activityTitle;
	private ImageView settingsOption, picOption;
	private Button leaveButton, sendButton;
	private EditText textBar;
	private Fragment fragment;
	private LayoutInflater inflater;
	//private Intent serviceIntent;
	
	private String picUrl;
	private boolean debug, flag, activatedFlag; // fix
	private byte camMode;
	
	private final static String TAG = "ChatActivity", DISCONNECT_MSG = "You have disconnected.";
	public final static String YOU_TAG = "You: "; 

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat);
		
		camMode = getIntent().getByteExtra("camMode", (byte) 0);
		
		activityTitle = (TextView) findViewById(R.id.app_title);
		settingsOption = (ImageView) findViewById(R.id.settings);
		picOption = (ImageView) findViewById(R.id.gallery);
		mainLayout = (LinearLayout) findViewById(R.id.main_layout);
		textBar = (EditText) findViewById(R.id.text_area);
		leaveButton = (Button) findViewById(R.id.leave_button);
		sendButton = (Button) findViewById(R.id.send_button);
		
		inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		
		if(camMode == 1) {
			//setContentView(R.layout.video_chat_ex);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			activityTitle.setText("Video Chat");
			
			picOption.setVisibility(View.GONE);
			picOption.setEnabled(false);
			textBar.setVisibility(View.GONE);
			textBar.setEnabled(false);
			sendButton.setVisibility(View.GONE);
			sendButton.setEnabled(false);
			
			refreshLayout();
			
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction ft = null;
	        fragment = fm.findFragmentById(R.id.chat_frame);
	        
	        if(fragment == null) {
	        	VideoChatFragment.newInstance();
	        	ft = fm.beginTransaction();
	        	ft.add(R.id.chat_frame, VideoChatFragment.getInstance(), "t");
	        	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
	        	ft.commit();
	        }
	        
		} else {			
			activityTitle.setText("Chat");
			//serviceIntent = new Intent(this, NotificationService.class);
			
			debug = false;
			flag = false;
			
			picOption.setVisibility(View.VISIBLE);
			picOption.setEnabled(false);
			textBar.setEnabled(false);
			leaveButton.setEnabled(false);
			sendButton.setEnabled(false);
			
			refreshLayout();
			
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction ft = null;
	        fragment = fm.findFragmentById(R.id.chat_frame);
	        
	        if(fragment == null) {
	        	ChatFragment.newInstance();
	        	ft = fm.beginTransaction();
	        	ft.add(R.id.chat_frame, ChatFragment.getInstance(), "t");
	        	ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
	        	ft.commit();
	        } 
	        
	        settingsOption.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent i = new Intent(ChatActivity.this, SettingsActivity.class);
					startActivity(i);
				}
	        	
	        });
	        
	        picOption.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent();
					intent.setAction(Intent.ACTION_GET_CONTENT);
					intent.setType("image/*");
					startActivityForResult(intent, 1);
				}
	        	
	        });
			
			textBar.addTextChangedListener(new TextWatcher() {

				@Override
				public void afterTextChanged(Editable s) {
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before,
						int count) {
					if(ChatFragment.getInstance() != null && 
							ChatFragment.getInstance().isCurrentlyConnected() && !OmegleService.getInstance().isTyping()) {
						TaskManager.getInstance().pushTask('t', null);
						OmegleService.getInstance().setTyping(true);
						if(debug)
							Log.d(TAG, "Is typing");
					}
				}
				
			});
			
			textBar.setOnFocusChangeListener(new OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if(hasFocus) {
						ChatFragment.getInstance().getChatlist().setSelection(ChatFragment.getInstance().getChatlist().getCount() - 1);
					}
					
				}
				
			});
			
			textBar.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					ChatFragment.getInstance().getChatlist().post(new Runnable() {
						@Override
						public void run() {
							ChatFragment.getInstance().getChatlist().requestFocusFromTouch();
							ChatFragment.getInstance().getChatlist().setSelection(ChatFragment.getInstance().getChatlist().getCount() - 1);
						}
					});
				}
				
			});
			
			textBar.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					if(OmegleService.getInstance() != null && OmegleService.getInstance().getChatSession() != null) {
						if(OmegleService.getInstance().getChatSession().getCopiedObject() != null) {
							textBar.setText(OmegleService.getInstance().getChatSession().getCopiedObject().toString());
						}
					}
					return false;
				}
				
			});
			
			leaveButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					final View layout = inflater.inflate(R.layout.custom_dialog, null);
	    			TextView dialogTitle = (TextView) layout.findViewById(R.id.dialog_title);
					ScrollView container = (ScrollView) layout.findViewById(R.id.scroll_container);
					
					dialogTitle.setText("Do you wish to end this conversation?");
					container.setVisibility(View.GONE);
					
					AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
						builder.setView(layout)
						.setCancelable(false)
						.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if(ChatFragment.getInstance() != null && ChatFragment.getInstance().isCurrentlyConnected()) {
									TaskManager.getInstance().pushTask('d', null);
									ChatFragment.getInstance().checkForLast();
									ChatFragment.getInstance().getChatAdapter().getChatLineList().add(ChatFragment.getInstance().getChatAdapter().getCount(), new ChatObject(ChatFragment.getInstance().getCurrentTimeArray(), DISCONNECT_MSG, null, false, false));
									ChatFragment.getInstance().getChatAdapter().getChatLineList().add(ChatFragment.getInstance().getChatAdapter().getCount(), new ChatObject("SV", "Share this conversation", null, true, false));
									ChatFragment.getInstance().getChatAdapter().notifyDataSetChanged();
									ChatFragment.getInstance().getChatlist().post(new Runnable() {
										@Override
										public void run() {
											ChatFragment.getInstance().getChatlist().setSelection(ChatFragment.getInstance().getChatAdapter().getCount() - 1);
										}
									});
									if(OmegleService.getInstance() != null) { 
										if(OmegleService.getInstance().getCurrentSessionType() == Session.COMMON_INTERESTS) {
											ChatFragment.getInstance().onOfferSettingsChange("is", 
												StringOperations.toStringArray(OmegleService.getInstance().getInterests()));
										}
										if(OmegleService.getInstance().getCurrentSessionType() == Session.SPYER) {
											ChatFragment.getInstance().onOfferSettingsChange("qs", OmegleService.getInstance().getQuestion());
										}
									}
									leaveButton.setText("New");
									picOption.setEnabled(false);
									ChatFragment.getInstance().checkForReconnect();
								}
							}
						})
						.setNegativeButton("No", new DialogInterface.OnClickListener() {
						
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
					});
					if(ChatFragment.getInstance().isCurrentlyConnected()) {
						AlertDialog dialog = builder.create();
						dialog.show();
					} else {
						leaveButton.setEnabled(false);
						ChatFragment.getInstance().resetSession();
						ChatFragment.getInstance().prepareSession();
					}
				}
			});
			
			sendButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(ChatFragment.getInstance() != null && ChatFragment.getInstance().isCurrentlyConnected() && textBar.getText().length() > 0) {
						if(OmegleService.getInstance().isTyping()) {
							TaskManager.getInstance().pushTask('n', null);
						}
						if(debug)
							Log.d(TAG, "Stopped typing");
						String message = String.valueOf(textBar.getText());
						textBar.setText("");
						TaskManager.getInstance().pushTask('m', message);
						OmegleService.getInstance().setTyping(false);
						ChatFragment.getInstance().checkForLast();
						ChatFragment.getInstance().getChatAdapter().getChatLineList().add(ChatFragment.getInstance().getChatAdapter().getCount(), new ChatObject(ChatFragment.getInstance().getCurrentTimeArray(), message, new Entity(YOU_TAG, false), false, false));
						ChatFragment.getInstance().getChatAdapter().notifyDataSetChanged();
						ChatFragment.getInstance().getChatlist().post(new Runnable() {
							@Override
							public void run() {
								ChatFragment.getInstance().getChatlist().setSelection(ChatFragment.getInstance().getChatlist().getCount() - 1);
							}
						});
					}
				}	
			});
		}
	}
	
	/**
	 * User is leaving activity
	 */
	
	@Override
	public void onBackPressed() { 
		if(!textBar.isInEditMode() && camMode == 0) {
			TaskManager.getInstance().pushTask('d', null);
			if(debug)
				Log.i(TAG, "Disconnecting");
			OmegleService.getInstance().killSession(false);
		}
		super.onBackPressed();
	}
	
	@Override
	public void onWindowFocusChanged(final boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		refreshLayout();
		
		if(ChatFragment.getInstance() != null)
			ChatFragment.getInstance().refreshLayout();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    
	    final View layout = inflater.inflate(R.layout.custom_dialog, null);
		final TextView dialogTitle = (TextView) layout.findViewById(R.id.dialog_title);
		final TextView preText = (TextView) layout.findViewById(R.id.pretext);
		final TextView title = (TextView) layout.findViewById(R.id.title);
		final EditText textBar = (EditText) layout.findViewById(R.id.questionBar);
		final ImageView imgV = (ImageView) layout.findViewById(R.id.given_pic);
		
	    switch(resultCode) {
	    case RESULT_CANCELED:
	    	break;
	    case RESULT_OK:
	    	if(data == null) {
				return;
			}
	    	Uri chosenImageUri = data.getData();
	        
	        Bitmap pic = null;
	        try {
				pic = Media.getBitmap(getContentResolver(), chosenImageUri);
			} catch (FileNotFoundException e) {
				Log.e(TAG, "Photo not found", e);
				return;
			} catch (IOException e) {
				Log.e(TAG, "IOException: ", e);
				return;
			}
	        
	        dialogTitle.setText("Do you wish to upload this picture?");
	        preText.setVisibility(View.GONE);
	        title.setVisibility(View.GONE);
	        imgV.setVisibility(View.VISIBLE);
	        
	        imgV.setImageBitmap(pic);
	        textBar.setHint("Your picture url will appear here once complete");
	       
	        String[] proj = { MediaStore.Images.Media.DATA };
	        Cursor cursor = managedQuery(chosenImageUri, proj, null, null, null);
	        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	        cursor.moveToFirst();
	        final String fileUri = cursor.getString(column_index);
	        
	        AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setView(layout)
			.setCancelable(true)
			.setPositiveButton("Upload", new Dialog.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(activatedFlag) {
						Toast.makeText(ChatActivity.this, "Your picture was already uploaded, To send a new one, click the attachment icon", Toast.LENGTH_SHORT).show();
						activatedFlag = false;
						return;
					}
					if(TaskManager.getInstance() != null) {
						Toast.makeText(ChatActivity.this, "Uploading your picture...", Toast.LENGTH_SHORT).show();
						Toast.makeText(ChatActivity.this, "Once complete, it will appear in the textbar above", Toast.LENGTH_SHORT).show();
						picUrl = (String) TaskManager.getInstance().pushTaskForResult('i', fileUri);
						if(picUrl != null) {
							textBar.setText(picUrl);
							textBar.setSelection(0, textBar.getText().length());
							flag = true;
							activatedFlag = true;
						}
					}
				}
				
			})
			.setNegativeButton("Cancel", new Dialog.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
				
			});
			final AlertDialog picDialog = builder.create();
			picDialog.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					if(flag) {
						picDialog.show();
						Toast.makeText(ChatActivity.this, "Picture successfully uploaded", Toast.LENGTH_SHORT).show();
						Toast.makeText(ChatActivity.this, "Use anchors to copy and paste text", Toast.LENGTH_SHORT).show();
						flag = false;
					} else {
						return;
					}
				}
			});
			picDialog.show();
			break;
	    }
	}
	
	@Override
	public void setLeaveButton(final String name, final boolean enabled) {
		leaveButton.setText(name);
		leaveButton.setEnabled(enabled);
	}
	
	@Override
	public void setSendButton(final String name, final boolean enabled) {
		sendButton.setText(name);
		sendButton.setEnabled(enabled);
	}

	@Override
	public void setTextBar(final boolean enabled) {
		textBar.setEnabled(enabled);
	}
	
	@Override
	public void setPictureOption(final boolean enable) {
		picOption.setEnabled(enable ? true : false);
	}
	
	@Override
	public void resetMode(final String mode, final String modeParams) {
		final View layout = inflater.inflate(R.layout.custom_dialog, null);
		final TextView dialogTitle = (TextView) layout.findViewById(R.id.dialog_title);
		final TextView preText = (TextView) layout.findViewById(R.id.pretext);
		final TextView title = (TextView) layout.findViewById(R.id.title);
		final EditText textBar = (EditText) layout.findViewById(R.id.questionBar);
		
		dialogTitle.setText("Change your " + (mode.equalsIgnoreCase("is") ? "common interests" : "question"));
		if(mode.equalsIgnoreCase("is")) {
			preText.setText("Note this will only be used for the current session");
			title.setText("Enter your interests here: ");
			textBar.setHint("Enter your new interests here");
			textBar.addTextChangedListener(StringOperations.Scanner);
			textBar.setText(modeParams);
		} else {
			textBar.setText(modeParams);
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(layout)
		.setCancelable(true)
		.setPositiveButton("Save", new Dialog.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(OmegleService.getInstance() != null && textBar.getText().length() > 0) {
					if(mode.equalsIgnoreCase("is")) {
						OmegleService.getInstance().setInterests(String.valueOf(textBar.getText()));
						flag = true;
					} else {
						if(textBar.getText().length() >= 10) {
							OmegleService.getInstance().setQuestion(String.valueOf(textBar.getText()));
							flag = true;
						} else {
							Toast.makeText(ChatActivity.this, "Your question is too short. It must be at least 10 characters or greater",
									Toast.LENGTH_SHORT).show();
						}
					}
				} else {
					Toast.makeText(ChatActivity.this, "Your " + 
							(mode.equalsIgnoreCase("is") ? "common interests" : "question" + " is too short, please try again"), Toast.LENGTH_SHORT).show();
				}
			}
			
		})
		.setNegativeButton("Cancel", new Dialog.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
			
		});
		final AlertDialog resetDialog = builder.create();
		resetDialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				if(flag) {
					resetDialog.show();
					Toast.makeText(ChatActivity.this, "Your new " + 
							(mode.equalsIgnoreCase("is") ? "common interests" : "question" + " has been saved"), Toast.LENGTH_SHORT).show();
					flag = false;
				} else {
					return;
				}
			}
		});
		resetDialog.show();
	}
	
	@Override
	public void requestShare(final LinkedList<String> chatList) {
		Toast.makeText(this, "Use the anchors to select text", Toast.LENGTH_SHORT).show();
		
		final View layout = inflater.inflate(R.layout.custom_dialog, null);
		final TextView dialogTitle = (TextView) layout.findViewById(R.id.dialog_title);
		final ScrollView container = (ScrollView) layout.findViewById(R.id.scroll_container);
		final TextView preText = (TextView) layout.findViewById(R.id.pretext);
		final TextView title = (TextView) layout.findViewById(R.id.title);
		final EditText textBar = (EditText) layout.findViewById(R.id.questionBar);
		String chat = "";
		
		for(String line : chatList) {
			chat += line + "<br/>";
		}
		
		dialogTitle.setText("Copy and paste your conversation anywhere");
		container.setBackgroundColor(Color.WHITE);
		preText.setVisibility(View.GONE);
		title.setVisibility(View.GONE);
		textBar.setText(Html.fromHtml(chat));
		textBar.setHint("Select your conversation text");
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(layout)
		.setCancelable(true)
		.setPositiveButton("OK", new Dialog.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
			
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	private void refreshLayout() {
		if(SettingsManager.getInstance() != null) {
			if(SettingsManager.getInstance().getPrefs() != null) {
				if(!SettingsManager.getInstance().isThemeDay()) {
					mainLayout.setBackgroundColor(Color.BLACK);
					textBar.setTextColor(Color.WHITE);
					textBar.setBackgroundColor(Color.BLACK);
					leaveButton.setTextColor(Color.WHITE);
					leaveButton.setBackgroundResource(R.drawable.chat_dark_button_layout);
					sendButton.setTextColor(Color.WHITE);
					sendButton.setBackgroundResource(R.drawable.chat_dark_button_layout);
				} else {
					mainLayout.setBackgroundColor(Color.WHITE);
					textBar.setTextColor(Color.BLACK);
					textBar.setBackgroundColor(Color.WHITE);
					leaveButton.setTextColor(Color.BLACK);
					leaveButton.setBackgroundResource(R.drawable.chat_button_layout);
					sendButton.setTextColor(Color.BLACK);
					sendButton.setBackgroundResource(R.drawable.chat_button_layout);
				}
			} else {
				Log.e(TAG, "Prefs were not loaded");
			}
		}
	}
}
