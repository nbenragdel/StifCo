package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;

import android.os.AsyncTask;
import android.util.Log;


public class RestClient{
	public static final int HTTP_OK = 200;
	public static final String SERVER_URL = "http://88.190.41.183/~stifco/core/library";

	public static void doGet(final String url, final OnResultListener onResultListener) {
		new AsyncTask<String, Void, String>() {

			@Override
			protected String doInBackground(String... params) {

				final HttpClient httpClient = new DefaultHttpClient();

				HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 10000);		
				HttpGet httpget = new HttpGet(SERVER_URL + url);
				HttpResponse response;
				InputStream instream = null;

				try {
					response = httpClient.execute(httpget);

					if ( response.getStatusLine().getStatusCode() == 200){
						HttpEntity entity = response.getEntity();				
						instream = entity.getContent();
						return read(instream);
					} else {
						return ""+response.getStatusLine().getStatusCode();
					}
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				return null;
			}

			@Override
			protected void onPostExecute(String result) {	
				super.onPostExecute(result);
				onResultListener.onResult(result);	

			}
		}.execute();
	}	

	public static void doPost(final String url, final List<NameValuePair> form, final OnResultListener onResultListener)
			throws URISyntaxException, HttpException, IOException {
		new AsyncTask<String, Void, String>() {

			@Override
			protected String doInBackground(String... params) {
				final HttpClient httpClient = new DefaultHttpClient();
				HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 10000);
				HttpPost httpPost = new HttpPost(SERVER_URL + url);

				InputStream instream = null;

				try {
					httpPost.setEntity(new UrlEncodedFormEntity(form));
					httpPost.getParams().setBooleanParameter("http.protocol.expect-continue", false);

					HttpResponse response = httpClient.execute(httpPost);
					// Log.d("RESTPOST", response.toString());	
					HttpEntity ent = response.getEntity();					
					instream = ent.getContent();
					return read(instream);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				return null;
			}

			protected void onPostExecute(String result) {
				super.onPostExecute(result);
				onResultListener.onResult(result);
			};
		}.execute();
	}

	private static String read(InputStream in) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader r = new BufferedReader(new InputStreamReader(in), 1000);

		for (String line = r.readLine(); line != null; line = r.readLine()) {
			sb.append(line);
		}

		in.close();

		return sb.toString();
	}
}
