package io.github.zkhan93.sharingtext;

import io.github.zkhan93.sharingtext.util.Constants;
import io.github.zkhan93.sharingtext.util.Utility;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FragementMain extends Fragment {
	public static final String TAG = "io.github.zkhan93.sharingtext.GragmentMain";
	static Context context;
	public static ViewHolder holder;
	static SharedPreferences spf;
	LinearLayout infoView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main, container,
				false);
		holder = new ViewHolder();
		context = getActivity();
		spf = context.getSharedPreferences(getString(R.string.pref_filename),
				Context.MODE_PRIVATE);
		holder.text = (EditText) rootView.findViewById(R.id.editTextReceived);
		holder.cbutton = (ImageButton) rootView.findViewById(R.id.buttonCon);
		holder.sbutton = (ImageButton) rootView.findViewById(R.id.buttonSend);
		holder.tlocal = (TextView) rootView.findViewById(R.id.textViewLocal);
		holder.tserver = (TextView) rootView.findViewById(R.id.textViewServer);
		setSendButton(MainActivity.CLIENT_CONN);

		setConnButton(MainActivity.CLIENT_CONN,
				(MainActivity.CLIENT_CONN ? true
						: (MainActivity.serverup ? false : true)));
		Bundle bundle = getArguments();
		if (bundle != null) {
			holder.text.getText().append(bundle.getString(Intent.EXTRA_TEXT));
			// send is connected
		}
		infoView = (LinearLayout) rootView
				.findViewById(R.id.linearViewInformation);
		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();
		new SetIp().execute();
	}

	public void toggleinfoViewVisibility() {
		if (infoView != null) {
			Utility.log(TAG, "info view not null");
			infoView.setVisibility(infoView.getVisibility() == View.GONE ? View.VISIBLE
					: View.GONE);
		}
	}

	public static class ViewHolder {
		EditText text;
		ImageButton cbutton;
		ImageButton sbutton;
		TextView tserver;
		TextView tlocal;
	}

	public static String getText() {
		return holder.text.getText().toString();
	}

	public static void setSendButton(boolean enable) {
		holder.sbutton.setEnabled(enable);
	}

	public static void setConnButton(boolean connected, boolean clickable) {
		if (connected) {
			holder.cbutton.setImageDrawable(context.getResources().getDrawable(
					R.drawable.ic_action_connected));
		} else {
			holder.cbutton.setImageDrawable(context.getResources().getDrawable(
					R.drawable.ic_action_disconnected));
		}
		if (clickable) {
			holder.cbutton.setEnabled(true);
		} else {
			holder.cbutton.setEnabled(false);
		}
	}

	public static class SetIp extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			return Utility.getIpAddress();
		}

		@Override
		protected void onPostExecute(String result) {
			int lport = spf.getInt(context.getString(R.string.pref_port),
					Constants.DEF_PORT);
			int sport = spf.getInt(context.getString(R.string.pref_sport),
					Constants.DEF_PORT);

			holder.tlocal.setText(
			// context.getString(R.string.textviewlocal)+
					Utility.getIpAddress() + Constants.COLON + lport);
			holder.tserver.setText(
			// context.getString(R.string.textviewserver)+
					spf.getString(context.getString(R.string.pref_ip), null)
							+ Constants.COLON + sport);
			MainActivity.updating = false;
			Log.d("msg", "done ip update");
			super.onPostExecute(result);
		}
	}
}
