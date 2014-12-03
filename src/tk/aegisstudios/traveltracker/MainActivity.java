package tk.aegisstudios.traveltracker;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
	String username;
	String token;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
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
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		LocationListener locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				String request = "STORELOC;"+username+","+token+","+String.valueOf(location.getLatitude())+","+String.valueOf(location.getLongitude());
				new OutSocketClass().execute(request);
			}
			public void onStatusChanged(String provider, int status, Bundle extras) {}
			public void onProviderEnabled(String provider) {}
			public void onProviderDisabled(String provider) {}
		};
                Criteria locCriteria = new Criteria();
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
                try {
                    locationManager.addTestProvider("bestprovider", false, false, true, false, false, false, false, 2, 3);
                } catch (IllegalArgumentException e) {
                    locationManager.clearTestProviderLocation("bestprovider");
                    locationManager.removeTestProvider("bestprovider");
                    locationManager.addTestProvider("bestprovider", false, false, true, false, false, false, false, 2, 3);
                }
                Location testLoc = new Location("bestprovider");
                testLoc.setLatitude(37.234754);
                testLoc.setLongitude(-121.754087);
                testLoc.setAccuracy(50);
                testLoc.setTime(System.currentTimeMillis());
                testLoc.setElapsedRealtimeNanos(System.currentTimeMillis());
                locationManager.setTestProviderLocation("bestprovider", testLoc);
		locationManager.requestLocationUpdates(locationManager.getBestProvider(locCriteria, true), 30, 0, locationListener);
				new GroupFetcher().execute("GETGROUPS;"+username+","+token);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		new GroupFetcher().execute("GETGROUPS;"+username+","+token);
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
			Intent intent = new Intent(this, InvitesActivity.class);
			startActivity(intent);
			return true;
		case R.id.action_new:
			Intent intent2 = new Intent(this, NewGroupActivity.class);
			startActivity(intent2);
			return true;
		case R.id.action_settings:
			Intent intent3 = new Intent(this, SettingsActivity.class);
			startActivity(intent3);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	public class OutSocketClass extends AsyncTask<String, Void, Void> {
		Socket sock;
		OutputStream sockOS;
		OutputStreamWriter sockOSW;
		
		@Override
		protected Void doInBackground (String... params) {
			if (sock == null) {
				try {
					sock = new Socket("192.168.1.8", 1337);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					sockOS = sock.getOutputStream();
					sockOSW = new OutputStreamWriter(sockOS);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}
			
			try {
				sockOSW.write(params[0]);
				sockOSW.flush();
				sockOSW.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				sock.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}
    public class GroupFetcher extends InOutSocketClass {
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
	            	final String groupID = groupData.split(";")[0];
	            	String groupName = groupData.split(";")[1];
	                Button b = new Button(getApplicationContext());
	
	                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 70);
	                b.setBackground(getResources().getDrawable(R.drawable.groupdisplayer));
	                b.setText(groupName);
	                b.setTextColor(getResources().getColor(R.color.fg));
	                b.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
	                b.setPadding(5, 5, 5, 5);
	                b.setOnClickListener(new View.OnClickListener() {
	                    public void onClick(View v) {
	                        Button bv = (Button) v;
	                        String groupName = bv.getText().toString();
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
        }
    }
    
    private FileOutputStream openAuthenticationData() {
    	File savedAuth;
    	FileOutputStream savedAuthStream;
        
        savedAuth = new File(getFilesDir(), "savedAuth.txt");
        savedAuthStream = convertFileToStream(savedAuth);
        
        return savedAuthStream;
    }
    
    private FileOutputStream convertFileToStream(File fileObj) {
    	FileOutputStream savedAuthStream = null;
    	
    	try {
    		savedAuthStream = new FileOutputStream(fileObj, true);
    	} catch (FileNotFoundException exception) {
    		showToast("Could not open file to save authentication data!");
    	}
    	
    	return savedAuthStream;
    }
    
    private void showToast(String toastMessage) {
        Toast.makeText(getApplicationContext(), toastMessage, 
                Toast.LENGTH_LONG).show();
    }
}
