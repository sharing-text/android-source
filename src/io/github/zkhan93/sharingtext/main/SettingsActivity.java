package io.github.zkhan93.sharingtext.main;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;


public class SettingsActivity extends ActionBarActivity {
	public static String TAG="io.github.zkhan93.sharingtext.main.SettingsActivity";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_settings);
		
		getFragmentManager().beginTransaction()
		.replace(R.id.settingContainer, new FragmentSettings()).commit();
		
		Toolbar toolbar=(Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
}
