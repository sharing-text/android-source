package nu.info.zeeshan.getthetext;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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
	static boolean connected;
	static Context context;
	SharedPreferences spf;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
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
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	public class Connect extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				String sip = spf.getString(getString(R.string.pref_ip), null);
				int port = spf.getInt(getString(R.string.pref_port), -1);
				if (sip != null && port > 0) {
					s = new Socket(sip, port);
					br = new BufferedReader(new InputStreamReader(
							s.getInputStream()));
					output = new PrintWriter(s.getOutputStream(), true);
					connected = true;
					return true;
				} else {
					Log.d(TAG, "ip or port not set");
					connected = false;
					return false;
				}
			} catch (Exception e) {
				Log.d(TAG, "socket failed" + e);
				return false;
			}
		}

		protected void onPostExecute(Boolean result) {
			if (result) {
				FragementMain.holder.cbutton.setImageDrawable(context
						.getResources().getDrawable(R.drawable.ic_connected_b));// ("Disconnect");
				FragementMain.holder.sbutton.setEnabled(true);
				FragementMain.holder.sbutton.setImageDrawable(context
						.getResources().getDrawable(R.drawable.ic_send_b));
				getData gt = new getData();
				gt.execute();
			} else
				Toast.makeText(context,
						context.getString(R.string.toast_conn_error),
						Toast.LENGTH_LONG).show();
		};
	}

	public static void disconnect() {
		try {
			s.close();
			connected = false;
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
		if (connected) {
			disconnect();
		} else {
			new Connect().execute();
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
			output.println(msg);
			FragementMain.holder.text.getText().clear();
		}
	}

	public void clearText(View view) {
		FragementMain.holder.text.getText().clear();
	}

	static class getData extends AsyncTask<Void, String, String> {

		@Override
		protected String doInBackground(Void... params) {
			String str = null;
			try {
				while ((str = br.readLine()) != null) {
					publishProgress(str);
				}
				Log.d(TAG, "done in background finished");
			} catch (Exception e) {
				Log.d(TAG, "do in back " + e);
			}
			return str;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			FragementMain.holder.text.getText().append(values[0] + "\n");
		}

		@Override
		protected void onPostExecute(String result) {
			disconnect();

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
