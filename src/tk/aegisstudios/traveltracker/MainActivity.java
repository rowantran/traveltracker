package tk.aegisstudios.traveltracker;
import java.io.*;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.location.Criteria;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class MainActivity extends Activity {
	LocationManager locationManager;
	LocationListener locationListener;
	Criteria locCriteria;

	String username;
	String token;

	private final static int PROXIMITY_ALERT_RADIUS = 50;

	private final static String AUTHENTICATION_DATA_LOCATION = "savedAuth.txt";

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
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				String request = "STORELOC;" + username + "," + token + "," + String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude());
				new OutSocket().execute(request);
			}

			public void onStatusChanged(String provider, int status, Bundle extras) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
			}
		};
		locCriteria = new Criteria();
		locCriteria.setAccuracy(Criteria.ACCURACY_FINE);
		locCriteria.setAltitudeRequired(false);
		locCriteria.setBearingAccuracy(Criteria.ACCURACY_LOW);
		locCriteria.setHorizontalAccuracy(Criteria.ACCURACY_FINE);
		locCriteria.setSpeedAccuracy(Criteria.NO_REQUIREMENT);
		locCriteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
		locCriteria.setSpeedAccuracy(Criteria.NO_REQUIREMENT);
		locCriteria.setSpeedRequired(false);
		locCriteria.setVerticalAccuracy(Criteria.NO_REQUIREMENT);
		locCriteria.setBearingRequired(false);

		new GroupFetcher().execute("GETGROUPS;" + username + "," + token);
	}

	@Override
	public void onResume() {
		super.onResume();
		new GroupFetcher().execute("GETGROUPS;" + username + "," + token);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_invites:
				locationManager.removeUpdates(locationListener);
				Intent intent = new Intent(this, InvitesActivity.class);
				startActivity(intent);
				return true;
			case R.id.action_new:
				locationManager.removeUpdates(locationListener);
				Intent intent2 = new Intent(this, NewGroupActivity.class);
				startActivity(intent2);
				return true;
			case R.id.action_settings:
				locationManager.removeUpdates(locationListener);
				Intent intent3 = new Intent(this, SettingsActivity.class);
				startActivity(intent3);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public class GroupFetcher extends InOutSocket {
		@Override
		protected void onPostExecute(String result) {
			String groupsOriginal = result;
			String[] groups = result.split(",");
			LinearLayout dynLayout = new LinearLayout(getApplicationContext());
			dynLayout.setBackgroundColor(getResources().getColor(R.color.bg));
			dynLayout.setOrientation(LinearLayout.VERTICAL);
			try {
				FileOutputStream savedAuthO = openFileOutput("savedGroups.txt", MODE_PRIVATE);
				OutputStreamWriter savedAuthOW = new OutputStreamWriter(savedAuthO);

				savedAuthOW.write(groupsOriginal);
				savedAuthOW.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (groupsOriginal.equals("none")) {
			} else {
				for (String groupData : groups) {
					final int groupID = Integer.parseInt(groupData.split(";")[0]);
					String groupName = groupData.split(";")[1];
					Button b = new Button(getApplicationContext());

					LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 70);
					b.setBackground(getResources().getDrawable(R.drawable.groupdisplayer));
					b.setText(groupName);
					b.setTextColor(getResources().getColor(R.color.fg));
					b.setGravity(Gravity.CENTER);
					b.setPadding(5, 5, 5, 5);
					b.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							Button bv = (Button) v;
							String groupName = bv.getText().toString();
							locationManager.removeUpdates(locationListener);
							Intent intent = new Intent(getApplicationContext(), DisplayGroupActivity.class);
							intent.putExtra("groupName", groupName);
							intent.putExtra("groupID", groupID);
							startActivity(intent);
						}
					});

					dynLayout.addView(b, layoutParams);
				}
			}
			setContentView(dynLayout);

			for (String groupPair : groups) {
				int groupID = Integer.parseInt(groupPair.split(";")[0]);
				new GroupDestinationFetcher(groupID).execute("GETDESTINATION;" + username + "," + token + "," + groupID);
			}

			locationManager.requestLocationUpdates(locationManager.getBestProvider(locCriteria, true), 30, 0, locationListener);

		}
	}

	/**
	 * Receiver for data about groups' destinations.
	 */
	public class GroupDestinationFetcher extends InOutSocket {
		private int groupID;

		/**
		 * Creates a new {@link tk.aegisstudios.traveltracker.MainActivity.GroupDestinationFetcher} instance.
		 * @param groupID the unique identifier of the group the class will fetch information about
		 */
		public GroupDestinationFetcher(int groupID) {
			this.groupID = groupID;
		}

		@Override
		public void onPostExecute(String result) {
			String[] groupDestination = response.split(",");
			double[] groupDestinationDouble = {Double.parseDouble(groupDestination[0]), Double.parseDouble(groupDestination[1])};

			Intent proximityAlertIntent = new Intent();
			proximityAlertIntent.setAction("tk.aegisstudios.traveltracker.locationArrivalReceiver");

			proximityAlertIntent.putExtra("groupID", groupID);
			proximityAlertIntent.putExtra("username", username);
			proximityAlertIntent.putExtra("token", token);

			locationManager.addProximityAlert(groupDestinationDouble[0], groupDestinationDouble[1], PROXIMITY_ALERT_RADIUS, -1, PendingIntent.getBroadcast(getApplicationContext(), groupID, proximityAlertIntent, PendingIntent.FLAG_UPDATE_CURRENT));
		}
	}


}
