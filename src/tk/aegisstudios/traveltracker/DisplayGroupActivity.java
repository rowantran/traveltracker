package tk.aegisstudios.traveltracker;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class DisplayGroupActivity extends Activity {
    String groupName;
    int groupID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        groupName = getIntent().getExtras().getString("groupName");
        groupID = getIntent().getExtras().getInt("groupID");
        getActionBar().setTitle(getIntent().getExtras().getString("groupName"));
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.displaygroup, menu);
		return true;
	}
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.add_member:
			Intent intent = new Intent(getApplicationContext(), AddMemberToGroupActivity.class);
			intent.putExtra("groupName", groupName);
			intent.putExtra("groupID", groupID);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
