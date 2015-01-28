package nu.info.zeeshan.getthetext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

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
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
	public static String TAG = "nu.info.zeeshan.getthetext.MainActivity";
	// public static EditText text;
	// public static ImageButton cbutton;
	// public static ImageButton sbutton;
	// public static TextView tserver;
	// public static TextView tlocal;
	static Socket s;
	static BufferedReader br;
	static PrintWriter output;
	static ArrayList<PrintWriter> clients_PWs;
	static ArrayList<Socket> cSockets;
	static int CLIENT_COUNT;
	static boolean CONN_SERVER;
	static Context context;
	SharedPreferences spf;
	public static boolean updating;
	public static ServerSocket ss;
	// public static Socket cs;
	public static boolean CONN_CLIENT;
	public static boolean serverup;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Intent intent = this.getIntent();
		String action = intent.getAction();
		String type = intent.getType();
		FragementMain fragment=new FragementMain();
		if (Intent.ACTION_SEND.equals(action) && type != null) {
			if ("text/plain".equals(type)) {
						Bundle bundle=new Bundle();
						bundle.putString(Intent.EXTRA_TEXT, intent.getStringExtra(Intent.EXTRA_TEXT));
						fragment.setArguments(bundle);
				}
		}
		if (clients_PWs == null)
			clients_PWs = new ArrayList<PrintWriter>();
		if (cSockets == null)
			cSockets = new ArrayList<Socket>();
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.container, fragment).commit();
		}
		spf = getSharedPreferences(getString(R.string.pref_filename),
				Context.MODE_PRIVATE);
		context = getApplicationContext();
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		

		
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
		case R.id.action_info:
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

	@Override
	public void onStart() {
		super.onStart();

	}

	public void startServer() {
		int port = spf
				.getInt(getString(R.string.pref_port), Constants.DEF_PORT);
		if (port != 0)
			new ServerStart().execute(port);
		else
			Utility.log(TAG, "port not ok");
	}

	public void stopServer() {
		Utility.log(TAG, "in stop server1");
		new ServerStop().start();
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
					output = new PrintWriter(s.getOutputStream(), true);
					CONN_SERVER = true;
					Log.d(TAG, "connected to server");
					return true;
				} else {
					Log.d(TAG, "ip or port not set");
					CONN_SERVER = false;
					return false;
				}
			} catch (Exception e) {
				Log.d(TAG, "socket failed" + e);
				return false;
			}
		}

		protected void onPostExecute(Boolean result) {
			if (result) {
				clients_PWs.clear();
				clients_PWs.add(output);
				getData gt = new getData(0);
				gt.execute(s);
				FragementMain.holder.cbutton.setImageDrawable(context
						.getResources().getDrawable(R.drawable.ic_connected_b));// ("Disconnect");
				FragementMain.holder.sbutton.setEnabled(true);
				FragementMain.holder.sbutton.setImageDrawable(context
						.getResources().getDrawable(R.drawable.ic_send_b));
			} else
				Toast.makeText(context,
						context.getString(R.string.toast_conn_error),
						Toast.LENGTH_LONG).show();
		};
	}

	public static void disconnect() {
		try {
			s.close();
			CONN_SERVER = false;
			FragementMain.holder.sbutton.setEnabled(false);
			FragementMain.holder.sbutton.setImageDrawable(context
					.getResources().getDrawable(
							R.drawable.ic_action_send_disable));
			FragementMain.holder.cbutton.setImageDrawable(context
					.getResources().getDrawable(R.drawable.ic_disconnected_b));// ("Connect");
			Toast.makeText(context,
					context.getString(R.string.toast_disconnected),
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Log.d(TAG, "cannot close s " + e);
		}

	}

	public void setConnection(View view) {
		if (CONN_SERVER) {
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

	static class sendData extends Thread {
		String msg;

		public sendData(String m) {
			msg = m;
		}

		public void run() {
			for (PrintWriter pw : clients_PWs)
				pw.println(msg);
			Utility.log(TAG, "msg send to all " + clients_PWs.size());
		}
	}

	static class getData extends AsyncTask<Socket, String, Void> {
		int index;

		public getData(int i) {
			index = i;
		}

		@Override
		protected Void doInBackground(Socket... params) {
			BufferedReader lbr;
			String str = null;
			try {
				lbr = new BufferedReader(new InputStreamReader(
						params[0].getInputStream()));
				Utility.log(TAG, "waiting for input string");
				while ((str = lbr.readLine()) != null) {
					publishProgress(str);
				}
				Log.d(TAG, "getData finished properly");
			} catch (Exception e) {
				Log.d(TAG, "getData ended" + e);
				try {
					clients_PWs.remove(index);
					cSockets.remove(index);
					if (params[0] != null && !params[0].isClosed())
						params[0].close();
				} catch (Exception ee) {

				}
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
			Utility.log(TAG, "client closed " + clients_PWs.size()
					+ " remaining");
			super.onPostExecute(result);
		}
	}

	public static class ServerStart extends AsyncTask<Integer, Socket, Boolean> {

		@Override
		protected Boolean doInBackground(Integer... port) {
			try {
				ss = new ServerSocket(port[0]);
				serverup = true;
				Utility.log(TAG, "server started");
				while (true) {
					Utility.log(TAG, "server waiting");
					s = ss.accept();
					output = new PrintWriter(s.getOutputStream(), true);
					cSockets.add(s);
					clients_PWs.add(output);
					CLIENT_COUNT++;
					publishProgress(s);
				}
			} catch (IOException e) {
				Utility.log(TAG, "server stopped");
				serverup = false;
				return false;
			}
		}

		@Override
		protected void onProgressUpdate(Socket... values) {
			new getData(CLIENT_COUNT).execute(values[0]);
			Utility.log(TAG, "client set");
			Toast.makeText(context, "client connected (" + CLIENT_COUNT + ")",
					Toast.LENGTH_SHORT).show();
			if (!FragementMain.holder.sbutton.isEnabled()) {
				FragementMain.holder.sbutton.setEnabled(true);
				FragementMain.holder.sbutton.setImageDrawable(context
						.getResources().getDrawable(R.drawable.ic_send_b));
			}
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				FragementMain.holder.sbutton.setEnabled(true);
				FragementMain.holder.sbutton.setImageDrawable(context
						.getResources().getDrawable(R.drawable.ic_send_b));
			} else {
				Utility.log(TAG, "error connecting client");
			}
			super.onPostExecute(result);
		}
	}

	public static class ServerStop extends Thread {

		public void run() {
			Utility.log(TAG, "socket closing thread0");
			try {
				Utility.log(TAG, "socket closing thread" + ss);
				ss.close();
				Utility.log(TAG, "socket closed");
				serverup = false;
				for (Socket ts : cSockets) {
					try {
						ts.close();
					} catch (Exception e) {
						Utility.log(TAG, "a client cannot be closed");
					}
				}
				cSockets.clear();
				clients_PWs.clear();

				Utility.log(TAG, "closing serverSocket and client done");
			} catch (Exception e) {
				Utility.log(TAG, "closing serverSocket threw exception");
				if (ss == null || ss.isClosed())
					serverup = false;
			}
		}
	}
	/*
	 * public class PlaceholderFragment extends Fragment {
	 * 
	 * @Override public View onCreateView(LayoutInflater inflater, ViewGroup
	 * container, Bundle savedInstanceState) { View rootView =
	 * inflater.inflate(R.layout.fragment_main, container, false); text =
	 * (EditText) rootView.findViewById(R.id.editTextReceived); cbutton =
	 * (ImageButton) rootView.findViewById(R.id.buttonCon); sbutton =
	 * (ImageButton) rootView.findViewById(R.id.buttonSend); tlocal=(TextView)
	 * rootView.findViewById(R.id.textViewLocal); tserver=(TextView)
	 * rootView.findViewById(R.id.textViewServer); if (connected) {
	 * cbutton.setImageDrawable(context.getResources().getDrawable(
	 * R.drawable.ic_connected_b));
	 * sbutton.setImageDrawable(context.getResources().getDrawable(
	 * R.drawable.ic_send_b)); sbutton.setEnabled(true); } else {
	 * cbutton.setImageDrawable(context.getResources().getDrawable(
	 * R.drawable.ic_disconnected_b));
	 * sbutton.setImageDrawable(context.getResources().getDrawable(
	 * R.drawable.ic_action_send_disable)); sbutton.setEnabled(false); } return
	 * rootView; }
	 * 
	 * @Override public void onStart(){ super.onStart();
	 * tlocal.setText(getString
	 * (R.string.textviewserver)+Utility.getIpAddress());
	 * tserver.setText(getString
	 * (R.string.textviewlocal)+spf.getString(getString(
	 * R.string.pref_ip),null)); } }
	 */
}
