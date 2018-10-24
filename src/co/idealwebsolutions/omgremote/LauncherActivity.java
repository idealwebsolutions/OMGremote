package co.idealwebsolutions.omgremote;

import co.idealwebsolutions.omgremote.feed.OmegleService;
import co.idealwebsolutions.omgremote.feed.OmegleService.Session;
import co.idealwebsolutions.omgremote.manager.SettingsManager;
import co.idealwebsolutions.omgremote.manager.TaskManager;
import co.idealwebsolutions.omgremote.util.StringOperations;

import android.net.ConnectivityManager;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class LauncherActivity extends Activity {
	
	private TextView activityTitle;
	private ImageView settingsOption;
	private Button langOption, textOption, videoOption, spyOption;
	private EditText commonInterestsBar;
	
	private LayoutInflater inflater;
	private ConnectivityManager connectivityManager;
	
	public static final String TAG = "LauncherActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launcher); 
        
        activityTitle = (TextView) findViewById(R.id.app_title);
        settingsOption = (ImageView) findViewById(R.id.settings);
        commonInterestsBar = (EditText) findViewById(R.id.common_interests_search);
        langOption = (Button) findViewById(R.id.lang_option);
        textOption = (Button) findViewById(R.id.text_chat);
        spyOption = (Button) findViewById(R.id.spy_chat);
        videoOption = (Button) findViewById(R.id.video_chat);
        
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        activityTitle.setText(R.string.app_name);
        
        if(TaskManager.getInstance() == null) {
        	TaskManager.newInstance();
        	Log.i(TAG, "Initialized TaskManager");
        }
        
        if(SettingsManager.getInstance() == null) {
        	SettingsManager.newInstance();
        	if(SettingsManager.getInstance() != null && SettingsManager.getInstance().init(this)) {
        		Log.i(TAG, "Initialized SettingsManager");
        	}
        }
        
        if(OmegleService.getInstance() == null) {
        	OmegleService.newInstance();
        	if(TaskManager.getInstance() != null) {
        		OmegleService.getInstance().init();
        		Log.i(TAG, "Initialized OmegleService");
        	}
        }
        
        if(!checkCameraHardware(this)) { // check if user can access camera
        	videoOption.setVisibility(View.GONE);
        	//Log.d(TAG, "Number of cameras available: " + Camera.getNumberOfCameras()); API LVL 9
        }
        
        langOption.setOnClickListener(new OnClickListener() {
        
			@Override
			public void onClick(View v) {
				Intent i = new Intent(LauncherActivity.this, SettingsActivity.class);
				startActivity(i);
			}
        });

        commonInterestsBar.addTextChangedListener(StringOperations.Scanner);
        
        settingsOption.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(LauncherActivity.this, SettingsActivity.class);
				startActivity(i);
			}
        	
        });

        textOption.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String interests = String.valueOf(commonInterestsBar.getText()).trim();
				if(interests.length() > 0) {
					initiate(Session.COMMON_INTERESTS.getType(), interests);
				} else {
					initiate(Session.TEXT.getType(), null);
				}
			}
        	
        });
        
        videoOption.setOnClickListener(new OnClickListener() {
        	
        	private AlertDialog alertDialog;
        	private TextView title, pretext, placer;
        	private EditText questionBar;
        	
			@Override
			public void onClick(View arg0) {
				final View layout = inflater.inflate(R.layout.custom_dialog, null);
				title = (TextView) layout.findViewById(R.id.dialog_title);
				title.setText("Warning: Video mode is experimental");
				pretext = (TextView) layout.findViewById(R.id.pretext);
				pretext.setText("This mode is not available for all devices and is still considered" +
						" in experimental mode. By accepting this disclaimer, you understand that " +
						"this not is not stable and chances of the application crashing is high.");
				placer = (TextView) layout.findViewById(R.id.title);
				placer.setVisibility(View.GONE);
				questionBar = (EditText) layout.findViewById(R.id.questionBar);
				questionBar.setVisibility(View.GONE);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(LauncherActivity.this);
				builder.setView(layout)
				.setCancelable(false)
				.setPositiveButton("I accept", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(LauncherActivity.this, ChatActivity.class);
						intent.putExtra("camMode", (byte)1);
						startActivity(intent);
					}
				})
				.setNegativeButton("I decline", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				alertDialog = builder.create();
				alertDialog.show();
			}
        	
        });
        
        spyOption.setOnClickListener(new OnClickListener() {
        	
        	private boolean flag;
        	private AlertDialog alertDialog;
        	private EditText questionBar;
        	
			@Override
			public void onClick(View v) {
				final View layout = inflater.inflate(R.layout.custom_dialog, null);
				questionBar = (EditText) layout.findViewById(R.id.questionBar);
				questionBar.setSingleLine(true);
				
				if(SettingsManager.getInstance() != null && SettingsManager.getInstance().getAutoQuestion() != null) {
					questionBar.setText(SettingsManager.getInstance().getAutoQuestion());
				}
				
				AlertDialog.Builder builder = new AlertDialog.Builder(LauncherActivity.this);
				builder.setView(layout)
				.setCancelable(false)
				.setPositiveButton("Ask strangers", new DialogInterface.OnClickListener() {
				
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String q = String.valueOf(questionBar.getText());
						if(q.length() >= 10) {
							String tq = q.trim();
							if(tq.length() != 0) {
								initiate(Session.SPYER.getType(), q);
							} else {
								flag = true;
							}
						} else {
							flag = true;
						}
					}
				})
				.setNeutralButton("Discuss instead", new DialogInterface.OnClickListener() {
				
					@Override
					public void onClick(DialogInterface dialog, int which) {
						initiate(Session.SPYEE.getType(), null);
					}
				})
				.setNegativeButton("Neither", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				alertDialog = builder.create();
				alertDialog.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						if(flag) {
							alertDialog.show();
							Toast.makeText(LauncherActivity.this, "Your question is too short, please try again", Toast.LENGTH_SHORT).show();
							flag = false;
						} else {
							return;
						}
					}
				});
				alertDialog.show();
			}
        });
    }
    
    /**
     * Initiates activity
     * @param value
     * @param obj
     */
    private void initiate(final byte value, final Object obj) {
    	if(!TaskManager.getInstance().checkConnection(connectivityManager)) {
    		String cib = String.valueOf(commonInterestsBar.getText());
    		boolean n = cib.trim().length() <= 0 ? true : false;
    		if(commonInterestsBar != null && cib.length() > 0 && value <= Session.COMMON_INTERESTS.getType()) {
    			if(n) {
    				Toast.makeText(LauncherActivity.this, "Your common interests are too short, try again", Toast.LENGTH_SHORT).show();
    				return;
    			}
    			final View layout = inflater.inflate(R.layout.custom_dialog, null);
    			TextView dialogTitle = (TextView) layout.findViewById(R.id.dialog_title);
				ScrollView container = (ScrollView) layout.findViewById(R.id.scroll_container);
				
				dialogTitle.setText("Choose your session");
				container.setVisibility(View.GONE);
    			
    			AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setView(layout)
				.setCancelable(false)
				.setPositiveButton("Common interests", new DialogInterface.OnClickListener() {
				
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(value == Session.COMMON_INTERESTS.getType() && obj != null && OmegleService.getInstance() != null) {
							OmegleService.getInstance().setInterests((String) obj); // We're only expecting a string
						}
						Intent chatIntent = new Intent(LauncherActivity.this, ChatActivity.class);
						chatIntent.putExtra("sessionType", Session.COMMON_INTERESTS.getType());
						startActivity(chatIntent);
					}
				})
				.setNeutralButton("Normal text", new DialogInterface.OnClickListener() {
				
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent chatIntent = new Intent(LauncherActivity.this, ChatActivity.class);
						chatIntent.putExtra("sessionType", Session.TEXT.getType());
						startActivity(chatIntent);
					}
				})
				.setNegativeButton("Neither", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				AlertDialog dialog = builder.create();
				dialog.show();
    		} else {
    			Intent chatIntent = new Intent(this, ChatActivity.class);
    			chatIntent.putExtra("sessionType", value);
    			if(value == Session.SPYER.getType()) {
    				OmegleService.getInstance().setQuestion((String) obj);
    			}
    			startActivity(chatIntent);
    		}
    	} else {
    		Toast.makeText(this, "Please make sure you are connected to the internet", Toast.LENGTH_SHORT).show();
    	}
    }
    
    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }
}
