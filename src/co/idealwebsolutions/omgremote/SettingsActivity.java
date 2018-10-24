package co.idealwebsolutions.omgremote;

import java.util.ArrayList;

import co.idealwebsolutions.omgremote.manager.SettingsManager;
import co.idealwebsolutions.omgremote.model.GenericItem;
import co.idealwebsolutions.omgremote.util.StringOperations;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {
	
	private SettingsAdapter settingsAdapter;
	private LayoutInflater inflater;
	private ListView settings;
	private TextView activityTitle;
	private SharedPreferences.Editor editor;
	
	private boolean dayTheme, autoReconnect, translatable;
	private String qType, rType, lang;
	
	private static final String TAG = "SettingsActivity";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.generic_layout);
		
		inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		
		activityTitle = (TextView) findViewById(R.id.app_title);
		settings = (ListView) findViewById(R.id.l_view);
		settingsAdapter = new SettingsAdapter(this);
		settings.setAdapter(settingsAdapter);
		activityTitle.setText("Settings");
		
		settingsAdapter.getSettingsList().add(new GenericItem(1, "Day/Night Theme", "This will invert the color theme for better visibility"));
		settingsAdapter.getSettingsList().add(new GenericItem(2, "Auto Reconnnect", "Allows you to reconnect automagically on disconnect"));
		settingsAdapter.getSettingsList().add(new GenericItem(3, "Auto Response", "Set a response to respond automagically on connect"));
		settingsAdapter.getSettingsList().add(new GenericItem(4, "Auto Question", "Set a question to ask automatically on spy mode"));
		settingsAdapter.getSettingsList().add(new GenericItem(5, "Preferred Language", "Choose a supported language to translate messages to"));
		
		if(SettingsManager.getInstance() != null && SettingsManager.getInstance().getPrefs() != null) {
			editor = SettingsManager.getInstance().getPrefs().edit();
			dayTheme = SettingsManager.getInstance().getPrefs().getBoolean(SettingsManager.THEME_TAG, true);
			autoReconnect = SettingsManager.getInstance().getPrefs().getBoolean(SettingsManager.RE_TAG, true);
			rType = SettingsManager.getInstance().getPrefs().getString(SettingsManager.R_TAG, "");
			qType = SettingsManager.getInstance().getPrefs().getString(SettingsManager.Q_TAG, "");
			setAndGetTranslatable(SettingsManager.getInstance().getPrefs().getBoolean(SettingsManager.TRANS_TAG, false));
			setAndGetLang(SettingsManager.getInstance().getPrefs().getString(SettingsManager.LANG_TAG, ""));
		} else {
			Log.e(TAG, "Could not open/access settings");
			Toast.makeText(this, "Could not open/access your settings file", Toast.LENGTH_SHORT).show();
			finishActivity(0);
		}
		
		settings.setOnItemClickListener(new OnItemClickListener() {
			
			private ScrollView container;
			private TextView dialogTitle, preText, title;
			private EditText textBar;
			private AlertDialog dialogT;
			
			private boolean flag;

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				final View layout = inflater.inflate(R.layout.custom_dialog, null);
				AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);;
				
				dialogTitle = (TextView) layout.findViewById(R.id.dialog_title);
				container = (ScrollView) layout.findViewById(R.id.scroll_container);
				preText = (TextView) layout.findViewById(R.id.pretext);
				title = (TextView) layout.findViewById(R.id.title);
				textBar = (EditText) layout.findViewById(R.id.questionBar);
				
				int code = settingsAdapter.getSettingsList().get(arg2).getCode();
				if(code <= 2) {
					dialogTitle.setText(code == 1 ? "Which theme do you wish to use?" : "Do you wish to enable auto reconnect?");
					container.setVisibility(View.GONE);
					
					String optionOne = code == 1 ? ("Day theme" + (isDayTheme() ? " - Current" : "")) : ("Enable" + (isOnAutoReconnect() ? " - Currently Enabled" : ""));
					String optionTwo = code == 1 ? ("Night theme" + (!isDayTheme() ? " - Current" : "")) : ("Disable" + (!isOnAutoReconnect() ? " - Currently Disabled" : ""));
					
					final int c = code;
					
					builder.setView(layout)
					.setPositiveButton(optionOne, new Dialog.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							if(c == 1) 
								editor.putBoolean(SettingsManager.THEME_TAG, setAndGetTheme(true));
							else
								editor.putBoolean(SettingsManager.RE_TAG, setAndGetAutoReconnect(true));
							editor.commit();
							Toast.makeText(SettingsActivity.this, "Successfully saved", Toast.LENGTH_SHORT).show();
						}
						
					})
					.setNeutralButton(optionTwo, new Dialog.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							if(c == 1)
								editor.putBoolean(SettingsManager.THEME_TAG, setAndGetTheme(false));
							else
								editor.putBoolean(SettingsManager.RE_TAG, setAndGetAutoReconnect(false));
							editor.commit();
							Toast.makeText(SettingsActivity.this, "Successfully saved", Toast.LENGTH_SHORT).show();
						}
						
					});
				} else if(code == 5) {
					dialogTitle.setText("Which language do you wish the chat text to be translated to?");
					container.setVisibility(View.GONE);
					
					builder.setView(layout)
					.setItems(new String[]{"English", "Spanish", "Italian", "German", "French", "Dutch",
							"Turkish", "Chinese (traditional)", "Russian", "Indonesian", "Hindu",
							"Finnish", "Korean"}, new Dialog.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							switch(which) {
							case 0:
								editor.putBoolean(SettingsManager.TRANS_TAG, setAndGetTranslatable(false));
								editor.putString(SettingsManager.LANG_TAG, setAndGetLang(StringOperations.LanguageFactory.getLanguage(which)));
								break;
							case 1: // spanish
								editor.putBoolean(SettingsManager.TRANS_TAG, setAndGetTranslatable(true));
								editor.putString(SettingsManager.LANG_TAG, setAndGetLang(StringOperations.LanguageFactory.getLanguage(which)));
								break;
							case 2: // italian
								editor.putBoolean(SettingsManager.TRANS_TAG, setAndGetTranslatable(true));
								editor.putString(SettingsManager.LANG_TAG, setAndGetLang(StringOperations.LanguageFactory.getLanguage(which)));
								break;
							case 3: // german
								editor.putBoolean(SettingsManager.TRANS_TAG, setAndGetTranslatable(true));
								editor.putString(SettingsManager.LANG_TAG, setAndGetLang(StringOperations.LanguageFactory.getLanguage(which)));
								break;
							case 4: // french
								editor.putBoolean(SettingsManager.TRANS_TAG, setAndGetTranslatable(true));
								editor.putString(SettingsManager.LANG_TAG, setAndGetLang(StringOperations.LanguageFactory.getLanguage(which)));
								break;
							case 5: // danish
								editor.putBoolean(SettingsManager.TRANS_TAG, setAndGetTranslatable(true));
								editor.putString(SettingsManager.LANG_TAG, setAndGetLang(StringOperations.LanguageFactory.getLanguage(which)));
								break;
							case 6: // greek
								editor.putBoolean(SettingsManager.TRANS_TAG, setAndGetTranslatable(true));
								editor.putString(SettingsManager.LANG_TAG, setAndGetLang(StringOperations.LanguageFactory.getLanguage(which)));
								break;
							case 7: // chinese
								editor.putBoolean(SettingsManager.TRANS_TAG, setAndGetTranslatable(true));
								editor.putString(SettingsManager.LANG_TAG, setAndGetLang(StringOperations.LanguageFactory.getLanguage(which)));
								break;
							case 8: // russian
								editor.putBoolean(SettingsManager.TRANS_TAG, setAndGetTranslatable(true));
								editor.putString(SettingsManager.LANG_TAG, setAndGetLang(StringOperations.LanguageFactory.getLanguage(which)));
								break;
							case 9: // hindi
								editor.putBoolean(SettingsManager.TRANS_TAG, setAndGetTranslatable(true));
								editor.putString(SettingsManager.LANG_TAG, setAndGetLang(StringOperations.LanguageFactory.getLanguage(which)));
								break;
							case 10: // japanese
								editor.putBoolean(SettingsManager.TRANS_TAG, setAndGetTranslatable(true));
								editor.putString(SettingsManager.LANG_TAG, setAndGetLang(StringOperations.LanguageFactory.getLanguage(which)));
								break;
							case 11: // finnish
								editor.putBoolean(SettingsManager.TRANS_TAG, setAndGetTranslatable(true));
								editor.putString(SettingsManager.LANG_TAG, setAndGetLang(StringOperations.LanguageFactory.getLanguage(which)));
								break;
							case 12: // korean
								editor.putBoolean(SettingsManager.TRANS_TAG, setAndGetTranslatable(true));
								editor.putString(SettingsManager.LANG_TAG, setAndGetLang(StringOperations.LanguageFactory.getLanguage(which)));
								break;
							}
							editor.commit();
							Toast.makeText(SettingsActivity.this, "Successfully saved", Toast.LENGTH_SHORT).show();
						}
					});
				} else {
					final String type = (code == 3 ? "response" : "question");
					dialogTitle.setText("Set an auto " + type + " of your choice: ");
					preText.setText("This message will be sent if set as soon as soon as you're connected. Only up to 30 characters allowed.");
					title.setVisibility(View.GONE);
					textBar.setHint("Enter your " + type + " here");
					textBar.setText(code == 3 ? rType : qType);
					textBar.setSingleLine(true);
					if(code == 4)
						textBar.addTextChangedListener(StringOperations.Scanner);
					
					final int c = code;
					
					builder.setView(layout)
					.setPositiveButton("Save", new Dialog.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							String s = String.valueOf(textBar.getText());
							if(s.trim().length() <= 0) {
								Toast.makeText(SettingsActivity.this, "Your " + type + " is too short", Toast.LENGTH_SHORT).show();
								flag = true;
							}
							if(c == 3) 
								rType = s;
							else 
								qType = s;
							editor.putString(c == 3 ? SettingsManager.R_TAG : SettingsManager.Q_TAG, String.valueOf(textBar.getText()));
							editor.commit();
							Toast.makeText(SettingsActivity.this, "Successfully saved", Toast.LENGTH_SHORT).show();
						}
						
					})
					.setNeutralButton("Cancel", new Dialog.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
						
					});
				}
				dialogT = builder.create();
				dialogT.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						if(flag) {
							dialogT.show();
							flag = false;
						} else {
							return;
						}
					}
				});
				dialogT.show();
			}
		});
	}
	
	public boolean isDayTheme() {
		return dayTheme;
	}
	
	public boolean setAndGetTheme(boolean dayTheme) {
		this.dayTheme = dayTheme;
		return dayTheme;
	}
	
	public boolean isOnAutoReconnect() {
		return autoReconnect;
	}
	
	public boolean setAndGetAutoReconnect(boolean autoReconnect) {
		this.autoReconnect = autoReconnect;
		return autoReconnect;
	}
	
	public boolean isTranslatable() {
		return translatable;
	}

	public boolean setAndGetTranslatable(boolean translatable) {
		this.translatable = translatable;
		return translatable;
	}

	public String getLang() {
		return lang;
	}

	public String setAndGetLang(String lang) {
		this.lang = lang;
		return lang;
	}

	private static class SettingsAdapter extends BaseAdapter {
		
		private ArrayList<GenericItem> settingsList;
		
		private LayoutInflater layoutInflater;
		
		public SettingsAdapter(Context ctx) {
			this.layoutInflater = LayoutInflater.from(ctx);
			this.settingsList = new ArrayList<GenericItem>();
		}

		@Override
		public int getCount() {
			return settingsList.size();
		}

		@Override
		public Object getItem(int arg0) {
			return settingsList.get(arg0);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			
			if(convertView == null) {
				convertView = layoutInflater.inflate(R.layout.generic_item, null); 
				holder = new ViewHolder();
				holder.settingsTitle = (TextView) convertView.findViewById(R.id.settings_item_title);
				holder.settingsDescription = (TextView) convertView.findViewById(R.id.settings_item_desc);
				convertView.setTag(holder);
 			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.settingsTitle.setText(settingsList.get(position).getTitle());
			holder.settingsDescription.setText(settingsList.get(position).getDescription());
			
			return convertView;
		}
		
		public ArrayList<GenericItem> getSettingsList() {
			return settingsList;
		}
		
		static class ViewHolder {
			TextView settingsTitle, settingsDescription;
		}
		
	}

}
