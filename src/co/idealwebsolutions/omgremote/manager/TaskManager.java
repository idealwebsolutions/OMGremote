package co.idealwebsolutions.omgremote.manager;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import co.idealwebsolutions.omgremote.feed.OmegleService;
import co.idealwebsolutions.omgremote.web.HttpService;
import co.idealwebsolutions.omgremote.web.HttpService.HttpMethod;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class TaskManager {
	
	private static TaskManager tm;
	
	private ExecutorService threadService;
	
	private static final String TAG = "TaskManager", IMAGESHACK_API_URL = "http://www.imageshack.us/upload_api.php", 
			API_KEY = "";
	
	public static TaskManager newInstance() {
		return tm = new TaskManager();
	}
	
	public static TaskManager getInstance() {
		return tm;
	}
	
	public Object pushTaskForResult(final char taskCode, final Object obj) {
		if(threadService == null) {
			threadService = Executors.newCachedThreadPool();
		}
		try {
			switch(taskCode) {
				case 'i': // Image upload
					final String fileUri = (String) obj;
					FutureTask<String> imageTask = (FutureTask<String>) threadService.submit(new Callable<String>() {

						@Override
						public String call() throws Exception {
							if(OmegleService.getInstance().isConnected() && OmegleService.getInstance().isRunning()) {
								return HttpService.getFromAPI(HttpMethod.POST, IMAGESHACK_API_URL, new String[]{fileUri, API_KEY});
							}
							return null;
						}
						
					});
					return imageTask.get();
				}
		} catch(Exception e) {
			Log.e(TAG, "Exception: ", e);
		}
		return new String("Error");
	}
	
	public void pushTask(final char taskCode, final Object obj) {
		if(threadService == null) {
			threadService = Executors.newCachedThreadPool();
		}
		try {
			switch(taskCode) {
				case 'a': // asynchronous message submission (We don't need to wait for a result)
					final String autoMessage = (String) obj;
					
					threadService.submit(new Runnable() {

						@Override
						public void run() {
							if(OmegleService.getInstance().isConnected() && OmegleService.getInstance().isRunning()){
								OmegleService.getInstance().changeTypingStatus(true);
								try {
									Thread.sleep(new Random().nextInt(500));
								} catch(Exception e) {
									// we won't catch this for now
								}
								OmegleService.getInstance().changeTypingStatus(false);
								OmegleService.getInstance().sendMessage(autoMessage);
							}
						}
						
					});
					break;
					
				case 'd': // Disconnect
					threadService.submit(new Runnable() {

						@Override
						public void run() {
							if(OmegleService.getInstance().isConnected() && OmegleService.getInstance().isRunning()) {
								OmegleService.getInstance().killSession(false);
								OmegleService.getInstance().disconnect();
							}
						}
						
					});
					break;
				
				case 't': // Typing event
					threadService.submit(new Runnable() {

						@Override
						public void run() {
							if(OmegleService.getInstance().isConnected() && OmegleService.getInstance().isRunning())
								OmegleService.getInstance().changeTypingStatus(true);
						}
						
					});
					break;
				
				case 'm': // Message
					final String message = (String) obj;
					threadService.submit(new Runnable() {

						@Override
						public void run() {
							if(OmegleService.getInstance().isConnected() && OmegleService.getInstance().isRunning())
								OmegleService.getInstance().sendMessage(message);
						}
						
					});
					break;
					
				case 'n': // Not typing event
					threadService.submit(new Runnable() {

						@Override
						public void run() {
							if(OmegleService.getInstance().isConnected() && OmegleService.getInstance().isRunning())
								OmegleService.getInstance().changeTypingStatus(false);
						}
						
					});
					break;
				
				//case 'r': // read inputfile
			}
		} catch(Exception e) {
			Log.e(TAG, "ExceptionError", e);
		}
	}
	
	public boolean checkConnection(ConnectivityManager connManager) { 
		NetworkInfo activeNetworkInfo = connManager.getActiveNetworkInfo();
		boolean connectFail = false;
		
		if(activeNetworkInfo != null) {
			connectFail = !activeNetworkInfo.isConnectedOrConnecting() ? true : false;
			//System.out.println("net 0: " + (connectFail ? "off" : "on"));
			return connectFail;
		} else {
			NetworkInfo wifiNetwork = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if(wifiNetwork != null) {
				connectFail = !wifiNetwork.isConnectedOrConnecting() ? true : false;
				//System.out.println("net 2: " + (connectFail ? "off" : "on"));
			}
			NetworkInfo mobileNetwork = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if(mobileNetwork != null) {
				connectFail = !mobileNetwork.isConnectedOrConnecting() ? true : false;
				//System.out.println("net 1: " + (connectFail ? "off" : "on"));
			}
			return connectFail;
		} 
	}
}
