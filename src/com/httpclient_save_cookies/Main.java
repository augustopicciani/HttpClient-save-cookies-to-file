package com.httpclient_save_cookies;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.os.Build;

/**
 * @author Augusto Picciani <augustopicciani@gmail.com>
 * @version 1.0
 * @since 09/04/2014
 */
public class Main extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a single view
	 */
	public static class PlaceholderFragment extends Fragment {
		Button btnStart;
		TextView status_connection;
		TextView status_saved;
		TextView status_read;
		private String TAG = "debug";
		private DefaultHttpClient httpclient;
		private BasicCookieStore cookieStore;
		private BasicHttpContext localContext;
		private HttpGet httpget;
		private CookieHelper helper;
		private HttpResponse response;
		private String folderName = "cookies";
		private String fileName = "cookies.txt";
		public Handler handler;

		private final int UNSUCCESSFULLY_CONNECTION = 0;
		private final int SUCCESSFULLY_CONNECTION = 1;
		private final int SUCCESSFULLY_READ = 2;
		private final int SUCCESSFULLY_WRITE = 3;

		private String LABEL_CONNECTING = "Connecting.....";
		private String LABEL_SUCCESSFULLY_CONN = "Connection successfully!";
		private String LABEL_UNSUCCESSFULLY_CONN = "Connection unsuccessfully!";
		private String LABEL_SUCCESSFULLY_READ = "Cookies retrieved successfully!"
				+ "\n\n ** For more details please refer to your Log **";
		private String LABEL_SUCCESSFULLY_WRITE = "Cookies saved succesfully on:";
		
		private String url="http://www.google.it";

		public PlaceholderFragment() {
			String path = Environment.getExternalStorageDirectory()
					+ File.separator + folderName;
			helper = new CookieHelper(path, fileName);
			LABEL_SUCCESSFULLY_WRITE = LABEL_SUCCESSFULLY_WRITE + "\n" + path
					+ File.separator + fileName;
			httpclient = new DefaultHttpClient();
			cookieStore = new BasicCookieStore();
			localContext = new BasicHttpContext();
			localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			initializeComp(rootView);
			return rootView;
		}

		public void initializeComp(View view) {
			status_connection = (TextView) view
					.findViewById(R.id.status_connection);
			status_read = (TextView) view.findViewById(R.id.status_read);
			status_saved = (TextView) view.findViewById(R.id.status_save);
			btnStart = (Button) view.findViewById(R.id.btnstart);
			btnStart.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View view) {
					new MakeConnection().execute(url);
				}

			});

			handler = new Handler(Looper.getMainLooper()) {
				@Override
				public void handleMessage(Message inputMessage) {
					switch (inputMessage.what) {
					case UNSUCCESSFULLY_CONNECTION:
						status_connection.setText(LABEL_UNSUCCESSFULLY_CONN);
						break;
					case SUCCESSFULLY_CONNECTION:
						status_connection.setText(LABEL_SUCCESSFULLY_CONN);
						break;
					case SUCCESSFULLY_READ:
						status_read.setText(LABEL_SUCCESSFULLY_READ);
						break;
					case SUCCESSFULLY_WRITE:
						status_saved.setText(LABEL_SUCCESSFULLY_WRITE);
						break;
					}
				}
			};
		}

		/**
		 * 
		 * Make an Http connection and fill cookiestore with cookies from
		 * website
		 * 
		 * @param url
		 */
		public void httpGet(String url) {
			Log.d(TAG, "connecting to: " + url + "....");
			response = null;
			// HttpParams params = httpclient.getParams();
			httpget = new HttpGet(url);

			try {
				response = httpclient.execute(httpget, localContext);
				if (response.getStatusLine().getStatusCode() == 200) {
					handler.sendEmptyMessage(SUCCESSFULLY_CONNECTION);
				} else {
					handler.sendEmptyMessage(UNSUCCESSFULLY_CONNECTION);
				}

			} catch (ClientProtocolException e) {
				handler.sendEmptyMessage(UNSUCCESSFULLY_CONNECTION);
				e.printStackTrace();
			} catch (IOException e) {
				handler.sendEmptyMessage(UNSUCCESSFULLY_CONNECTION);
				e.printStackTrace();
			}

		}

		public void saveCookiesToFile() {

			Log.d(TAG, "controllo cookies.....");
			List<Cookie> cookies = cookieStore.getCookies();

			if (cookies.isEmpty()) {
				Log.d(TAG, "No cookies");
			} else {
				for (Cookie c : cookies) {
					helper.writeCookieTofile(c);
				}

			}
			handler.sendEmptyMessage(SUCCESSFULLY_WRITE);

		}
		
		
		public void getCookieFromfile() {
			// Be sure to clear all cookies before retrieving from file
			cookieStore.clear();
			helper.retrieveCookie(cookieStore);
			handler.sendEmptyMessage(SUCCESSFULLY_READ);
		}

		private class MakeConnection extends AsyncTask<String, Void, Void> {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				status_connection.setText(LABEL_CONNECTING);
				/*
				 * In this example I'm going to delete the file cookies.txt at every connection
				 * because don't need to do other stuff
				 */
				helper.deleteFile();
			}

			@Override
			protected Void doInBackground(String... param) {
				httpGet(param[0]);
				return null;
			}

			@Override
			protected void onPostExecute(Void avoid) {
				super.onPostExecute(avoid);
				saveCookiesToFile();
				getCookieFromfile();
			}

		}
	}

}
