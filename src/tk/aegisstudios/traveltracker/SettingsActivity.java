package tk.aegisstudios.traveltracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class SettingsActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
	}
	
	public void onLogOut(View v) {
		getApplicationContext().deleteFile("savedAuth.txt");
		Intent intent = new Intent(this, StarterActivity.class);
		startActivity(intent);
	}
}
