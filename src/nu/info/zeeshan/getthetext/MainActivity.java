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
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {
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
		if (clients_PWs == null)
			clients_PWs = new ArrayList<PrintWriter>();
		if(cSockets==null)
			cSockets=new ArrayList<Socket>();
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new FragementMain()).commit();
		}
		spf = getSharedPreferences(getString(R.string.pref_filename),
				Context.MODE_PRIVATE);
		context = getApplicationContext();
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
			if (!serverup) {
				startServer();
				item.setTitle(getString(R.string.serverdown));
				item.setIcon(getResources().getDrawable(
						R.drawable.ic_action_stop));
			} else {
				stopServer();
				item.setTitle(getString(R.string.serverup));
				item.setIcon(getResources().getDrawable(
						R.drawable.ic_action_start));
			}
		default:
			return super.onOptionsItemSelected(item);
		}
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
		new ServerStop().execute();
	}

	public class ConnectToServer extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				String sip = spf.getString(getString(R.string.pref_ip), null);
				int port = spf.getInt(getString(R.string.pref_port), Constants.DEF_PORT);
				if (sip != null && port > 0) {
					s = new Socket(sip, port);
					output = new PrintWriter(s.getOutputStream(), true);
					clients_PWs.add(output);
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
				getData gt = new getData();
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

	public void copyText(View view) {
		String msg = FragementMain.holder.text.getText().toString().trim();
		if (msg.length() > 0) {
			ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData data = ClipData.newPlainText("Text from PC", msg);
			clipboard.setPrimaryClip(data);
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
			new sendData().execute(msg);
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
static class sendData extends AsyncTask<String, Void, Void>{

	@Override
	protected Void doInBackground(String... params) {
		for(PrintWriter pw:clients_PWs)
			pw.println(params[0]);
		return null;
	}
	
}
	static class getData extends AsyncTask<Socket, String, String> {

		@Override
		protected String doInBackground(Socket... params) {
			BufferedReader lbr;
			String str = null;
			try {
				lbr = new BufferedReader(new InputStreamReader(
						params[0].getInputStream()));
				while ((str = lbr.readLine()) != null && !params[0].isInputShutdown()) {
					publishProgress(str);
				}
				Log.d(TAG, "getData finished properly");
			} catch (Exception e) {
				Log.d(TAG, "getData ended" + e);
				try {
					if (params[0] != null && !params[0].isClosed())
						params[0].close();
				} catch (Exception ee) {

				}
			}
			return str;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			FragementMain.holder.text.getText().append(values[0] + "\n");
		}
	}

	public static class ServerStart extends AsyncTask<Integer, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Integer... port) {
			try {
				ss = new ServerSocket(port[0]);
				serverup = true;
				while (true) {
					s = ss.accept();
					output = new PrintWriter(s.getOutputStream(), true);
					cSockets.add(s);
					clients_PWs.add(output);
					CLIENT_COUNT++;
					publishProgress();
				}
				// return true;
			} catch (IOException e) {
				Utility.log(TAG, "server stopped");
				serverup = false;
				return false;
			}
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			getData gt = new getData();
			gt.execute(s);
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

	public static class ServerStop extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				if (ss != null) {
					ss.close();
					serverup = false;
					for(Socket ts:cSockets){
						try{
						ts.close();
						}catch(Exception e){
							Utility.log(TAG,"a client cannot be closed");
						}
					}
					cSockets.clear();
					clients_PWs.clear();
				}
				return true;
			} catch (Exception e) {
				Utility.log(TAG,"closing serverSocket threw exception");
				if (!ss.isClosed())
					return false;
				else
					return true;
				
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if(result)
				Utility.log(TAG, "server closed");
			else
				Utility.log(TAG, "server not closed");
			super.onPostExecute(result);
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
