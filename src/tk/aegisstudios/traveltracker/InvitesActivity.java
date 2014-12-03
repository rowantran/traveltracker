package tk.aegisstudios.traveltracker;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


public class InvitesActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	public class InvitesFetcher extends InOutSocketClass {
		@Override
		public void onPostExecute(String result) {
			String[] invites = result.split(",");
			LinearLayout ll = new LinearLayout(getApplicationContext());
			ll.setOrientation(LinearLayout.VERTICAL);
			for (String i : invites) {
				String groupName = i.split(";")[0];
				String groupID = i.split(";")[1];
				LinearLayout invLL = new LinearLayout(getApplicationContext());
				invLL.setOrientation(LinearLayout.HORIZONTAL);
				TextView groupNameDisplay = new TextView(getApplicationContext());
				groupNameDisplay.setText(groupName);
				invLL.addView(groupNameDisplay, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100));
				ll.addView(invLL, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
			}
			setContentView(ll);
		}
	}
}
