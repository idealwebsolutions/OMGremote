package co.idealwebsolutions.omgremote.fragments;

import co.idealwebsolutions.omgremote.R;
import co.idealwebsolutions.omgremote.feed.VideoStreamService;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class VideoChatFragment extends Fragment {
	
	private static VideoChatFragment vcFragment;
	private Camera camera;
	
	private VideoStreamService vss;
	
	private final static String TAG = "VideoChatFragment";
	
	public static VideoChatFragment newInstance() {
		return vcFragment = new VideoChatFragment();
	}
	
	public static VideoChatFragment getInstance() {
		return vcFragment;
	}
	
	public static Camera getCameraInstance() {
		Camera cam = null;
		try {
			cam = Camera.open();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return cam;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setRetainInstance(true);
        camera = getCameraInstance();
        vss = new VideoStreamService(activity, camera);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) { 
		VideoStreamService vss = new VideoStreamService(this.getActivity(), camera);
		View view = inflater.inflate(R.layout.video_chat_ex, container, false); 
		
		FrameLayout frame = (FrameLayout) view.findViewById(R.id.camera_preview);
		frame.addView(vss);
		return view;
	}
	
}
