package tk.aegisstudios.traveltracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.*;


public class InvitesActivity extends Activity {
	private final static String AUTHENTICATION_DATA_LOCATION = "savedAuth.txt";
	private String username, token;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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
				new Redirection(this).redirectToSplash();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		new InvitesFetcher().execute("CHECKINVITES;" + username + "," + token);
	}
	public class InvitesFetcher extends InOutSocket {
		private final static int PADDING_SIZE = 5;

		@Override
		public void onPostExecute(String result) {
			String[] invites = result.split(",");
			LinearLayout ll = new LinearLayout(getApplicationContext());
			ll.setOrientation(LinearLayout.VERTICAL);
			for (String s : invites) {
				String[] splitInviteData = s.split(";");

				final String groupName = splitInviteData[0];
				final int groupID = Integer.parseInt(splitInviteData[1]);
				final String groupInviter = splitInviteData[2];

				LinearLayout invLL = new LinearLayout(getApplicationContext());
				invLL.setOrientation(LinearLayout.HORIZONTAL);

				Button groupNameDisplay = new Button(getApplicationContext());

				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 70);

				groupNameDisplay.setBackground(getResources().getDrawable(R.drawable.groupdisplayer));
				groupNameDisplay.setText(groupName);
				groupNameDisplay.setTextColor(getResources().getColor(R.color.fg));
				groupNameDisplay.setGravity(Gravity.CENTER);
				groupNameDisplay.setPadding(PADDING_SIZE, PADDING_SIZE, PADDING_SIZE, PADDING_SIZE);

				groupNameDisplay.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						new Redirection(getApplicationContext()).redirectToInviteDisplay(groupName, groupID, groupInviter);
					}
				});

				ll.addView(groupNameDisplay, layoutParams);
			}
			setContentView(ll);
		}
	}
}
