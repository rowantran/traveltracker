package tk.aegisstudios.traveltracker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import java.io.*;

public class DisplayGroupActivity extends Activity {
    private String groupName;
    private int groupID;

	private final static String AUTHENTICATION_DATA_LOCATION = "savedAuth.txt";

	private String username;
	private String token;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        groupName = getIntent().getExtras().getString("groupName");
        groupID = getIntent().getExtras().getInt("groupID");

        getActionBar().setTitle(getIntent().getExtras().getString("groupName"));

		try {
			File authenticationFile = new File(this.getFilesDir(), AUTHENTICATION_DATA_LOCATION);
			BufferedReader saved = new BufferedReader(new FileReader(authenticationFile));
			String readFrSaved = saved.readLine();

			if (readFrSaved != null) {
				String[] readFrSavedS = readFrSaved.split(",");
				username = readFrSavedS[0];
				token = readFrSavedS[1];
				saved.close();
			} else {
				getApplicationContext().deleteFile("savedAuth.txt");
				redirectToSplash();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		new GroupMembersFetcher().execute("GETGROUPMEMBERS;"+username+","+token+","+Integer.toString(groupID));
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
			case R.id.edit_location:
			Intent locationIntent = new Intent(getApplicationContext(), EditGroupDestination.class);
				locationIntent.putExtra("groupName", groupName);
				locationIntent.putExtra("groupID", groupID);
				locationIntent.putExtra("username", username);
				locationIntent.putExtra("token", token);
			startActivity(locationIntent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void redirectToSplash() {
		Intent intent = new Intent(this, StarterActivity.class);
		startActivity(intent);
	}

	public class GroupMembersFetcher extends InOutSocketClass {
		@Override
		public void onPostExecute(String result) {
			int PADDING_WIDTH = 5;

			String[] groupMembers = result.split(";");
			LinearLayout parentLayout = new LinearLayout(getApplicationContext());
			parentLayout.setBackgroundColor(getResources().getColor(R.color.bg));
			parentLayout.setOrientation(LinearLayout.VERTICAL);

			TextView memberNameHeader = new TextView(getApplicationContext());

			LinearLayout.LayoutParams layoutParamsHeader = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 60);
			layoutParamsHeader.setMargins(5, 5, 10, 5);

			memberNameHeader.setText("MEMBERS");
			memberNameHeader.setTextColor(getResources().getColor(R.color.fg));
			memberNameHeader.setPadding(PADDING_WIDTH, PADDING_WIDTH, PADDING_WIDTH, PADDING_WIDTH);
			memberNameHeader.setTypeface(null, Typeface.BOLD);
			memberNameHeader.setGravity(Gravity.CENTER_HORIZONTAL);

			parentLayout.addView(memberNameHeader, layoutParamsHeader);

			for (String memberName : groupMembers) {

				TextView memberNameDisplay = new TextView(getApplicationContext());

				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 60);
				layoutParams.setMargins(5, 5, 0, 10);

				memberNameDisplay.setText(memberName);
				memberNameDisplay.setTextColor(getResources().getColor(R.color.fg));
				memberNameDisplay.setPadding(PADDING_WIDTH, PADDING_WIDTH, PADDING_WIDTH, PADDING_WIDTH);
				memberNameDisplay.setGravity(Gravity.CENTER_HORIZONTAL);

				parentLayout.addView(memberNameDisplay, layoutParams);
			}
			setContentView(parentLayout);
		}
	}

}
