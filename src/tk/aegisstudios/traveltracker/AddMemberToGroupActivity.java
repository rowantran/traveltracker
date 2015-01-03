package tk.aegisstudios.traveltracker;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AddMemberToGroupActivity extends Activity {
	EditText memberUsername;
	String groupName;
	int groupID;
	String username;
	String token;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_addmembertogroup);
		
		try {
			BufferedReader saved = new BufferedReader(new InputStreamReader(openFileInput("savedAuth.txt")));
			String readFrSaved = saved.readLine();
			String[] readFrSavedS = readFrSaved.split(",");
			username = readFrSavedS[0];
			token = readFrSavedS[1];
			saved.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		memberUsername = (EditText) findViewById(R.id.memberUsername);
		groupName = getIntent().getExtras().getString("groupName");
		groupID = getIntent().getExtras().getInt("groupID");
		
	}
	
	public void onAddMember(View v) {
		String memberUsernameVal = memberUsername.getText().toString();
		String request = "ADDMEMBER;"+username+","+token+","+groupID+","+memberUsernameVal;
		new MemberAdder().execute(request);
	}
	
	public class MemberAdder extends InOutSocketClass {
		@Override
		public void onPostExecute(String result) {
			if (result.equals("Success")) {
				String toastMessage = "Succesfully invited user.";
				Toast.makeText(getApplicationContext(), toastMessage, 
						Toast.LENGTH_LONG).show();
				Intent intent = new Intent(getApplicationContext(), DisplayGroupActivity.class);
				intent.putExtra("groupName", groupName);
				startActivity(intent);
				finish();
			} else {
				String toastMessage = "Error! Please try again.";
				Toast.makeText(getApplicationContext(), toastMessage, 
						Toast.LENGTH_LONG).show();
			}
		}
	}
}
