package nu.info.zeeshan.getthetext;

import nu.info.zeeshan.getthetext.util.Utility;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class FragementMain extends Fragment {
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
		holder.tlocal = (TextView) rootView.findViewById(R.id.textViewLocal);
		holder.tserver = (TextView) rootView.findViewById(R.id.textViewServer);
		if (MainActivity.connected) {
			holder.cbutton.setImageDrawable(context.getResources().getDrawable(
					R.drawable.ic_connected_b));
			holder.sbutton.setImageDrawable(context.getResources().getDrawable(
					R.drawable.ic_send_b));
			holder.sbutton.setEnabled(true);
		} else {
			holder.cbutton.setImageDrawable(context.getResources().getDrawable(
					R.drawable.ic_disconnected_b));
			holder.sbutton.setImageDrawable(context.getResources().getDrawable(
					R.drawable.ic_action_send_disable));
			holder.sbutton.setEnabled(false);
		}
		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();
		new SetIp().execute();
	}

	public static class ViewHolder {
		EditText text;
		ImageButton cbutton;
		ImageButton sbutton;
		TextView tserver;
		TextView tlocal;
	}

	private static class SetIp extends AsyncTask<Void, Void, String> {
		@Override
		protected String doInBackground(Void... params) {
			return Utility.getIpAddress();
		}

		@Override
		protected void onPostExecute(String result) {
			holder.tlocal.setText(context.getString(R.string.textviewserver)
					+ Utility.getIpAddress()+spf.getString(context.getString(R.string.pref_port), null));
			holder.tserver.setText(context.getString(R.string.textviewlocal)
					+ spf.getString(context.getString(R.string.pref_ip), null)+spf.getString(context.getString(R.string.pref_sport), null));
			super.onPostExecute(result);
		}
	}
}
