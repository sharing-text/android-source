package nu.info.zeeshan.getthetext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import nu.info.zeeshan.getthetext.FragementMain.SetIp;
import nu.info.zeeshan.getthetext.util.Constants;
import nu.info.zeeshan.getthetext.util.Utility;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
	public static String TAG = "nu.info.zeeshan.getthetext.MainActivity";
	// public static EditText text;
	// public static ImageButton cbutton;
	// public static ImageButton sbutton;
	// public static TextView tserver;
	// public static TextView tlocal;
	static Socket s;
	static BufferedReader reader;
	static PrintWriter writer;
	static Context context;
	SharedPreferences spf;
	public static boolean updating;
	public static ServerSocket ss;
	// pubic static Socket cs;
	static boolean CLIENT_CONN;
	public static boolean serverup;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Intent intent = this.getIntent();
		String action = intent.getAction();
		String type = intent.getType();
		FragementMain fragment = new FragementMain();
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
					.replace(R.id.container, fragment).commit();
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent intent = new Intent(this, SettingsActivity.class);
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
			intent.putExtra(Intent.EXTRA_TEXT, FragementMain.getText() );  
			startActivity(Intent.createChooser(intent, "Share Text via"));
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
		Utility.log(TAG, "in stop server1");
		new ServerStop().execute();
		Utility.log(TAG, "in stop server2");
	}

	public class ConnectToServer extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				String sip = spf.getString(getString(R.string.pref_ip), null);
				int port = spf.getInt(getString(R.string.pref_port),
						Constants.DEF_PORT);
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

				FragementMain.setConnButton(true, true);
				FragementMain.setSendButton(true);
			} else
				Toast.makeText(context,
						context.getString(R.string.toast_conn_error),
						Toast.LENGTH_LONG).show();
		};
	}

	public void disconnect() {
		try {
			s.close();
			CLIENT_CONN = false;
			FragementMain.setSendButton(false);
			if (serverup){
				FragementMain.setConnButton(false, false);
				new WaitForClient().execute();
			}
			else
				FragementMain.setConnButton(false, true);
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
			disconnect();
		} else {
			new ConnectToServer().execute();
		}

	}

	@SuppressLint("NewApi")
	public void copyText(View view) {
		String msg = FragementMain.holder.text.getText().toString().trim();
		if (msg.length() > 0) {
			int sdk = android.os.Build.VERSION.SDK_INT;
			if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
				android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				clipboard.setText(msg);
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
		String msg = FragementMain.holder.text.getText().toString().trim();
		if (msg.length() > 0) {
			new sendData(msg).start();
			FragementMain.holder.text.getText().clear();
		}
	}

	public void clearText(View view) {
		FragementMain.holder.text.getText().clear();
	}

	public void updateIP(View view) {
		if (!updating) {
			updating = true;
			new SetIp().execute();
		} else {
			Toast.makeText(getApplicationContext(),
					getString(R.string.updating), Toast.LENGTH_SHORT).show();
		}
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
				disconnect();
				Log.d(TAG, " sending fail " + e.getMessage());
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
				CLIENT_CONN = false;
			} finally {
				Log.d(TAG, "get data terminating");
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			Utility.log(TAG, " got text" + values[0]);
			FragementMain.holder.text.getText().append(values[0] + "\n");
		}

		@Override
		protected void onPostExecute(Void result) {
			Utility.log(TAG, "get Data termianting onPostExecute");
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					disconnect();
					
				}
			});
			super.onPostExecute(result);
		}
	}

	public class WaitForClient extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				Utility.log(TAG, "server waiting");
				s = ss.accept();
				writer = new PrintWriter(s.getOutputStream(), true);
				reader = new BufferedReader(new InputStreamReader(
						s.getInputStream()));
				Utility.log(TAG, "client Connected");
				CLIENT_CONN = true;
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
			FragementMain.setSendButton(true);
			FragementMain.setConnButton(true, true);
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

			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				FragementMain.setConnButton(false, false);
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

	public static class ServerStop extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			try {
				Utility.log(TAG, "server socket closing thread");
				ss.close();
				Utility.log(TAG, "server socket closed");
				serverup = false;
				s.close();
				CLIENT_CONN = false;
				Utility.log(TAG, "closed serverSocket and client done");
			} catch (Exception e) {
				Utility.log(TAG, "closing serverSocket threw exception");
				if (ss == null || ss.isClosed())
					serverup = false;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			FragementMain.setSendButton(false);
			FragementMain.setConnButton(false, true);
			super.onPostExecute(result);
		}
	}

}
