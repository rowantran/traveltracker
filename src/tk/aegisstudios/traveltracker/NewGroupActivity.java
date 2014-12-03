package tk.aegisstudios.traveltracker;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

public class NewGroupActivity extends Activity {
	TextView groupName;
	Button groupBtn;
	
	String username;
	String token;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_newgroup);
		
		groupName = (EditText) findViewById(R.id.groupName);
		
		groupBtn = (Button) findViewById(R.id.groupBtn);
		
	}
	
	public void onCreateGroup(View v) {
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
		
		new GroupCreator().execute("NEWGROUP;"+username+","+token+","+groupName.getText().toString().replace(",", ""));
		Log.d("f", "Created InOutSocket");
	}
	
	public class GroupCreator extends InOutSocketClass {
		@Override
		protected void onPostExecute(String result) {
			String regToastMessage;

			if (result.equals("Success")) {
				regToastMessage = "Group succesfully created.";
			} else if (result.equals("Error")) {
				regToastMessage = "Error. Please try again.";
			} else {
				regToastMessage = "Unknown error code.";
			}
			Toast.makeText(getApplicationContext(), regToastMessage, 
					Toast.LENGTH_LONG).show();
		}
	}
}
