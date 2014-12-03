package nu.info.zeeshan.getthetext;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity {
	public static String TAG="nu.info.zeeshan.getthetext.MainActivity";
	public static EditText text;
	public static Button cbutton;
	static Socket s;
	static BufferedReader br;
	static boolean connected;
	SharedPreferences spf;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		spf=getSharedPreferences(getString(R.string.pref_filename), Context.MODE_PRIVATE);
		
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
			Intent intent=new Intent(this,SettingsActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	public void onStart(){
		super.onStart();
	}
	public class Connect extends AsyncTask<Void, Void, Void>{
		
			@Override
			protected Void doInBackground(Void... params) {
				try{
					s=new Socket(spf.getString(getString(R.string.pref_ip), "127.0.0.1"),23456);
					br=new BufferedReader(new InputStreamReader(s.getInputStream()));
					}catch(Exception e){
						Log.d(TAG,"socket failed"+e);
					}
				return null;
			}
			protected void onPostExecute(Void result) {
				cbutton.setText("Disconnect");
				getData gt=new getData();
				 gt.execute();
				 connected=true;
			};
	}
	public void disconnect(){
		try{
			s.close();
			connected=false;
			cbutton.setText("Connect");
			
		}catch(Exception e){
			Log.d(TAG,"cannot close s "+e);
		}
		
	}
	public void setConnection(View view){
		if(connected){
			disconnect();
		}
		else{
			new Connect().execute();
		}
		
	}
	public void clearText(View view){
		text.getText().clear();
	}
	static class getData extends AsyncTask<Void, String, String>{

		@Override
		protected String doInBackground(Void... params) {
			String str = null;
			try{
				while(true){
					str=br.readLine();
					publishProgress(str);
				}
			}catch(Exception e){
				Log.d(TAG,"do in back"+e);
			}
			return str;
		}
		@Override
		protected void onProgressUpdate(String... values) {
			text.getText().append(values[0]+"\n");
		}
	}
	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			text=(EditText)rootView.findViewById(R.id.editTextReceived);
			cbutton=(Button)rootView.findViewById(R.id.buttonCon);
			
			return rootView;
		}
	}
	
}
