package co.idealwebsolutions.omgremote;

import java.util.ArrayList;

import co.idealwebsolutions.omgremote.model.GenericItem;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ScriptActivity extends Activity {
	
	private ScriptAdapter scriptAdapter;
	//private LayoutInflater inflater;
	private ListView scriptList;
	private TextView activityTitle;
	
	//private static final String TAG = "ScriptActivity";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.generic_layout);
		
		//inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		
		activityTitle = (TextView) findViewById(R.id.app_title);
		scriptList = (ListView) findViewById(R.id.l_view);
		scriptAdapter = new ScriptAdapter(this);
		scriptList.setAdapter(scriptAdapter);
		activityTitle.setText("Scripts");
		
		scriptList.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				
			}
			
		});
		
		constructList();
	}
	
	private void constructList() {
		scriptAdapter.getScriptList().add(new GenericItem(1, "Songbird", "Allows you to choose an artist/song to send on connect (Note: Sending messages will be disabled until finished)"));
	}
	
	private static class ScriptAdapter extends BaseAdapter {
		
		private ArrayList<GenericItem> scriptList;
		
		private LayoutInflater layoutInflater;
		
		public ScriptAdapter(Context ctx) {
			this.layoutInflater = LayoutInflater.from(ctx);
			this.scriptList = new ArrayList<GenericItem>();
		}

		@Override
		public int getCount() {
			return scriptList.size();
		}

		@Override
		public Object getItem(int position) {
			return scriptList.get(position);
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
				//holder.log = (TextView) convertView.findViewById(R.id.generic_layout);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.log.setText(scriptList.get(position).getTitle());
			
			return convertView;
		}
		
		public ArrayList<GenericItem> getScriptList() {
			return scriptList;
		}
		
		private static class ViewHolder {
			TextView log;
		}
		
	}

}
