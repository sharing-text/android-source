package io.github.zkhan93.sharingtext;

import io.github.zkhan93.sharingtext.util.Constants;
import io.github.zkhan93.sharingtext.util.Utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
	public static String TAG = "io.github.zkhan93.sharingtext.main.MainActivity";
	static Socket s;
	static BufferedReader reader;
	static PrintWriter writer;
	static Context context;
	SharedPreferences spf;

	public static ServerSocket ss;
	// pubic static Socket cs;
	static boolean CLIENT_CONN;
	public static boolean serverup, waiting;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Intent intent = this.getIntent();
		String action = intent.getAction();
		String type = intent.getType();
		FragmentMain fragment = new FragmentMain();
		if (Intent.ACTION_SEND.equals(action) && type != null) {
			if ("text/plain".equals(type)) {
				Bundle bundle = new Bundle();
				bundle.putString(Intent.EXTRA_TEXT,
						intent.getStringExtra(Intent.EXTRA_TEXT));
				fragment.setArguments(bundle);
			}
		}
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.container, fragment, FragmentMain.TAG)
					.commit();
		}
		spf = getSharedPreferences(getString(R.string.pref_filename),
				Context.MODE_PRIVATE);
		context = getApplicationContext();
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	Intent intent;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		case R.id.action_settings:
			intent = new Intent(this, SettingsActivity.class);
			intent.putExtra(Constants.FRAGMENT, Constants.FRAGMENT_SETTING);
			startActivity(intent);
			return true;
		case R.id.action_about:
			intent = new Intent(this, SettingsActivity.class);
			intent.putExtra(Constants.FRAGMENT, Constants.FRAGMENT_ABOUT);
			startActivity(intent);
			return true;
		case R.id.action_server:
			if (serverup) {
				Toast.makeText(getApplicationContext(), "Stopping server",
						Toast.LENGTH_SHORT).show();
				stopServer();
			} else {
				Toast.makeText(getApplicationContext(), "Starting server",
						Toast.LENGTH_SHORT).show();
				startServer();
			}
			return true;
		case R.id.action_share:
			intent = new Intent();
			intent.setAction(Intent.ACTION_SEND);
			intent.setType("text/html|text/plain");
			intent.putExtra(Intent.EXTRA_TEXT, FragmentMain.getText());
			startActivity(Intent.createChooser(intent, "Share Text via"));
			return true;
		case android.R.id.home:
		    onBackPressed();
		    return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem item = menu.findItem(R.id.action_server);
		if (serverup) {
			item.setTitle(getString(R.string.serverstop));
			item.setIcon(getResources().getDrawable(R.drawable.ic_action_stop));
		} else {
			item.setTitle(getString(R.string.serverstart));
			item.setIcon(getResources().getDrawable(R.drawable.ic_action_start));
		}
		return super.onPrepareOptionsMenu(menu);
	}

	public void startServer() {
		int port = spf
				.getInt(getString(R.string.pref_port), Constants.DEF_PORT);
		if (port > 0)
			new ServerStart().execute(port);
		else
			Utility.log(TAG, "port not ok");
	}

	public void stopServer() {
		serverup = false;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				new ServerStop().start();
				Utility.log(TAG, "in stoping server");
			}
		});

	}

	public class ConnectToServer extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				String sip = spf.getString(getString(R.string.pref_ip), null);
				int port = spf.getInt(getString(R.string.pref_sport),
						Constants.DEF_PORT);
				Log.d(TAG, "port i am using is " + port);
				if (sip != null && port > 0) {
					s = new Socket(sip, port);
					writer = new PrintWriter(s.getOutputStream(), true);
					reader = new BufferedReader(new InputStreamReader(
							s.getInputStream()));
					CLIENT_CONN = true;
					Log.d(TAG, "connected to server");
					return true;
				} else {
					Log.d(TAG, "ip or port not set");
					CLIENT_CONN = false;
					return false;
				}
			} catch (Exception e) {
				Log.d(TAG, "socket failed" + e);
				CLIENT_CONN = false;
				return false;
			}
		}

		protected void onPostExecute(Boolean result) {
			if (result) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						new getData().execute();
						Log.d(TAG, "started get DATA");
					}
				});

				FragmentMain.setConnButton(true, true);
				FragmentMain.setSendButton(true);
			} else
				Toast.makeText(context,
						context.getString(R.string.toast_conn_error),
						Toast.LENGTH_LONG).show();
		};
	}

	public void disconnect() {
		try {
			if (s != null && !s.isClosed())
				s.close();
			CLIENT_CONN = false;
			FragmentMain.setSendButton(false);
			if (serverup && !waiting) {
				FragmentMain.setConnButton(false, false);
				new WaitForClient().execute();
			} else
				FragmentMain.setConnButton(false, true);
			Toast.makeText(context,
					context.getString(R.string.toast_disconnected),
					Toast.LENGTH_SHORT).show();

		} catch (Exception e) {
			Log.d(TAG, "cannot close s " + e);
		}

	}

	public void setConnection(View view) {
		if (CLIENT_CONN) {
			new sendData(null).start();
		} else {
			new ConnectToServer().execute();
		}

	}

	public void showInfo(View view) {
		Utility.log(TAG, "in show info");
		Fragment fragment = null;
		fragment = getSupportFragmentManager().findFragmentByTag(
				FragmentInfo.TAG);
		if (fragment == null)
			fragment = new FragmentInfo();
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.container, fragment, FragmentInfo.TAG).addToBackStack(FragmentMain.TAG).commit();
		
		
	}

	public void copyText(View view) {
		String msg = FragmentMain.holder.text.getText().toString().trim();
		if (msg.length() > 0) {
			int sdk = android.os.Build.VERSION.SDK_INT;
			if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
				android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				clipboard.setPrimaryClip(ClipData.newPlainText(null, msg));
			} else {
				ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				ClipData data = ClipData.newPlainText("Text from PC", msg);
				clipboard.setPrimaryClip(data);
			}
			Toast.makeText(getApplicationContext(),
					getString(R.string.toast_textcopied), Toast.LENGTH_SHORT)
					.show();
		} else {
			Toast.makeText(getApplicationContext(),
					getString(R.string.toast_notext), Toast.LENGTH_SHORT)
					.show();
		}
	}

	public void sendText(View view) {
		String msg = FragmentMain.holder.text.getText().toString().trim();
		if (msg.length() > 0) {
			new sendData(msg).start();
			FragmentMain.holder.text.getText().clear();
		}
	}

	public void clearText(View view) {
		FragmentMain.holder.text.getText().clear();
	}

	class sendData extends Thread {
		String msg;

		public sendData(String m) {
			msg = m;
		}

		public void run() {
			try {
				if (writer != null) {
					writer.println(msg);
					Utility.log(TAG, "msg send ");
				}
			} catch (Exception e) {
				CLIENT_CONN = false;
				Log.d(TAG, " sending fail " + e.getMessage());
			} finally {
				if (msg == null) {
					MainActivity.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							disconnect();
						}
					});
				}
			}
		}
	}

	class getData extends AsyncTask<Void, String, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			String str = null;
			try {
				Utility.log(TAG, "waiting for input string");
				while ((str = reader.readLine()) != null && CLIENT_CONN) {
					publishProgress(str);
					Log.d(TAG, "got some msg");
				}
			} catch (Exception e) {
				Log.d(TAG, "getData " + e.getMessage());
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			Utility.log(TAG, " got text" + values[0]);
			if (values[0] != null)
				FragmentMain.holder.text.getText().append(values[0] + "\n");
		}

		@Override
		protected void onPostExecute(Void result) {
			Utility.log(TAG, "get Data termianting onPostExecute");
			CLIENT_CONN = false;
			if (serverup) {
				disconnect();
			} else {
				FragmentMain.setSendButton(false);
				FragmentMain.setConnButton(false, true);
			}
			super.onPostExecute(result);
		}
	}

	public class WaitForClient extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				Utility.log(TAG, "server waiting");
				waiting = true;
				s = ss.accept();
				writer = new PrintWriter(s.getOutputStream(), true);
				reader = new BufferedReader(new InputStreamReader(
						s.getInputStream()));
				CLIENT_CONN = true;
				waiting = false;
			} catch (IOException e) {
				if (s == null || !s.isConnected())
					CLIENT_CONN = false;
				else
					CLIENT_CONN = true;
			}
			return CLIENT_CONN;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			Utility.log(TAG, "client Connected");
			FragmentMain.setSendButton(true);
			FragmentMain.setConnButton(true, true);
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					new getData().execute();
				}
			});
			super.onPostExecute(result);
		}

	}

	public class ServerStart extends AsyncTask<Integer, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Integer... port) {
			try {
				if (ss == null || ss.isClosed()) {
					ss = new ServerSocket(port[0], 1);
					serverup = true;
					Utility.log(TAG, "server started");
				}
			} catch (IOException e) {
				Utility.log(TAG, "server stopped");
				if (ss == null || ss.isClosed())
					serverup = false;
				else
					serverup = true;
				return serverup;
			}
			return serverup;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result && !waiting) {
				FragmentMain.setConnButton(false, false);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						new WaitForClient().execute();
					}
				});

			} else {
				Utility.log(TAG, "error connecting client");
			}
			super.onPostExecute(result);
		}
	}

	public class ServerStop extends Thread {
		@Override
		public void run() {
			try {
				Utility.log(TAG, "server stopping thread ");
				if (s != null && !s.isClosed()) {
					s.close();
				}
				CLIENT_CONN = false;
				Utility.log(TAG, "closed client Socket ");
				if (ss != null && !ss.isClosed()) {
					ss.close();
				}
				Utility.log(TAG, "server socket closing thread");
				serverup = false;
				Utility.log(TAG, "server socket closed");
				MainActivity.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						FragmentMain.setSendButton(false);
						FragmentMain.setConnButton(false, true);

					}
				});
			} catch (Exception e) {
				Utility.log(TAG, "closing serverSocket threw exception");
				if (ss == null || ss.isClosed())
					serverup = false;
			}
		}
	}

}
