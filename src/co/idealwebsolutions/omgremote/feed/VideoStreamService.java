package co.idealwebsolutions.omgremote.feed;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class VideoStreamService extends SurfaceView implements SurfaceHolder.Callback {
	
	private SurfaceHolder holder;
	private Camera camera;

	public VideoStreamService(Context context, Camera camera) {
		super(context);
		this.camera = camera;
		this.holder = getHolder();
		
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			camera.setPreviewDisplay(holder);
			camera.startPreview();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		if(holder.getSurface() == null) {
			return;
		}
		try {
			camera.stopPreview();
			
			camera.setPreviewDisplay(holder);
			camera.startPreview();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
