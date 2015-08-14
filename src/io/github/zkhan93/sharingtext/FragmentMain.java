package io.github.zkhan93.sharingtext;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

public class FragmentMain extends Fragment {
	public static final String TAG = "io.github.zkhan93.sharingtext.GragmentMain";
	static Context context;
	public static ViewHolder holder;
	static SharedPreferences spf;

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
		holder.scanbutton = (ImageButton) rootView.findViewById(R.id.buttonScan);
		setSendButton(MainActivity.CLIENT_CONN);

		setConnButton(MainActivity.CLIENT_CONN,
				(MainActivity.CLIENT_CONN ? true
						: (MainActivity.serverup ? false : true)));
		Bundle bundle = getArguments();
		if (bundle != null) {
			holder.text.getText().append(bundle.getString(Intent.EXTRA_TEXT));
			// send is connected
		}
		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();
		((MainActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(false);
		((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
	}

	public static class ViewHolder {
		EditText text;
		ImageButton cbutton;
		ImageButton sbutton;
		ImageButton scanbutton;
		// TextView tserver;
		// TextView tlocal;
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
			holder.scanbutton.setEnabled(true);
		} else {
			holder.cbutton.setEnabled(false);
			holder.scanbutton.setEnabled(true);
		}
	}

}
