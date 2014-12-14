package nu.info.zeeshan.getthetext;

import nu.info.zeeshan.getthetext.util.Utility;

import org.apache.http.conn.util.InetAddressUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.widget.Toast;

public class SettingFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {
	public static String TAG = "nu.info.zeeshan.getthetext.util.Utility.SettingFragment";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.settings);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences tspf, String key) {
		SharedPreferences spf = getActivity().getSharedPreferences(
				getString(R.string.pref_filename), Context.MODE_PRIVATE);
		String str = tspf.getString(key, null).trim();
		if (str != null && str.length() > 0) {
			if (key.equalsIgnoreCase(getString(R.string.pref_ip))) {
				if (InetAddressUtils.isIPv4Address(str)) {
					spf.edit().remove(key).commit();
					spf.edit().putString(key, tspf.getString(key, null))
							.commit();
					Utility.log(TAG, "done");
				} else {
					Toast.makeText(getActivity().getApplicationContext(),
							getString(R.string.invalidip), Toast.LENGTH_SHORT)
							.show();
				}
			} else {
				int newport;
				try {
					newport = Integer.parseInt(str);
					if (newport < 1024) {
						Toast.makeText(getActivity().getApplicationContext(),
								getString(R.string.invalidport),
								Toast.LENGTH_SHORT).show();
					} else {
						spf.edit().remove(key).commit(); // remove old
						spf.edit().putInt(key, newport)// add new
								.commit();
					}
					Utility.log(TAG, "done");
				} catch (Exception e) {
					Toast.makeText(getActivity().getApplicationContext(),
							getString(R.string.invalidport), Toast.LENGTH_SHORT)
							.show();
				}

			}
		}else{
			Toast.makeText(getActivity().getApplicationContext(),
					getString(R.string.empty), Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}
}
