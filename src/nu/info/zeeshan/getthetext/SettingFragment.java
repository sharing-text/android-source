package nu.info.zeeshan.getthetext;

import nu.info.zeeshan.getthetext.util.Utility;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;

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

		if (key.equalsIgnoreCase(getString(R.string.pref_ip))) {
			String newip = tspf.getString(key, null);
			if (newip != null) {
				spf.edit().remove(key).commit();
				spf.edit().putString(key, tspf.getString(key, null)).commit();
				Utility.log(TAG, "done");
			}
		} else if (key.equalsIgnoreCase(getString(R.string.pref_port))) {
			int newport = tspf.getInt(key, -1);
			if (newport != -1) {
				spf.edit().remove(key).commit(); // remove old
				spf.edit().putInt(key, tspf.getInt(key, -1))// add new
						.commit();
				Utility.log(TAG, "done");
			}
		} else if (key.equalsIgnoreCase(getString(R.string.pref_sport))) {
			int newport = tspf.getInt(key, -1);
			if (newport != -1) {
				spf.edit().remove(key).commit(); // remove old
				spf.edit().putInt(key, tspf.getInt(key, -1))// add new
						.commit();
				Utility.log(TAG, "done");
			}
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
