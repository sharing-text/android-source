package nu.info.zeeshan.getthetext;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingFragment extends PreferenceFragment  implements OnSharedPreferenceChangeListener {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings);
    }

	@Override
	public void onSharedPreferenceChanged(SharedPreferences tspf, String key) {
		SharedPreferences spf=getActivity().getSharedPreferences(getString(R.string.pref_filename), Context.MODE_PRIVATE);
		spf.edit().remove(key).commit();
		spf.edit().putString(key, tspf.getString(key, "127.0.0.1")).commit();
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
