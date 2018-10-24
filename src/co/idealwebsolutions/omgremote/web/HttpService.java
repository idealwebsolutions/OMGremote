package co.idealwebsolutions.omgremote.web;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import net.htmlparser.jericho.Source;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import co.idealwebsolutions.omgremote.util.StringOperations;

import android.util.Log;

public class HttpService {
	
	private static final String TAG = "HttpService";
	
	/**
	 * Defines http methods
	 */
	
	public static enum HttpMethod {
		GET, POST;
	}
	
	/**
	 * Uses HttpClient for retrieve data
	 * @param method
	 * @param url
	 * @param params
	 * @return
	 */
	
	public static String getFromAPI(HttpMethod method, final String url, final String[] params) {
		HttpResponse response = null;
		HttpEntity entity = null;
		try {
			if(params == null || params.length < 2)
				return null;
			MultipartEntity reqEntity = new MultipartEntity();
			reqEntity.addPart("fileupload", new FileBody(new File(params[0])));
			reqEntity.addPart("key", new StringBody(params[1]));
			
			HttpClient http = new DefaultHttpClient();
			if(HttpMethod.POST == method && params != null) {
				HttpPost post = new HttpPost(new URI(url));
				post.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
				post.setEntity(reqEntity);
				response = http.execute(post);
				if(response.getStatusLine().getStatusCode() != 404) {
					entity = response.getEntity();
				} 
				Source source = new Source(entity.getContent());
				entity.consumeContent();
				return StringOperations.pullLinkFromText(String.valueOf(source.getTextExtractor()));
			}
		} catch(Exception e) {
			Log.i(TAG, "HTTP Error: ", e);
		} 
		return null;
	}
	
	/**
	 * Opens a new url for use
	 * @param method
	 * @return
	 * @throws Exception 
	 */
	
	public static InputStream getFromHttp(HttpMethod method, final String url, final String params) {
		HttpURLConnection http;
		try {
			URL u = new URL(url);
			http = (HttpURLConnection) u.openConnection();
			http.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
			http.setConnectTimeout(10000);
			http.setReadTimeout(10000);
			if(method == HttpMethod.POST && params != null) {
				http.setRequestMethod("POST");
				http.setDoOutput(true);
				http.getOutputStream().write(params.getBytes());
			} else {
				http.setRequestMethod("GET");
			}
			return http.getInputStream();
		} catch (IOException e) {
			Log.d(TAG, "HTTP Error: ", e);
		}
		return null;
	}
}
