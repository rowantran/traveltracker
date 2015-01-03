package tk.aegisstudios.traveltracker;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.Buffer;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
				redirectToSplash();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				String request = "STORELOC;"+username+","+token+","+String.valueOf(location.getLatitude())+","+String.valueOf(location.getLongitude());
				new OutSocketClass().execute(request);
			}
			public void onStatusChanged(String provider, int status, Bundle extras) {}
			public void onProviderEnabled(String provider) {}
			public void onProviderDisabled(String provider) {}
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

				Socket connSocket;

				InputStream sockIStream;
				InputStreamReader sockIReader;
				BufferedReader sockBufferedIReader;

				OutputStream sockOStream;
				OutputStreamWriter sockOWriter;

				String response = null;

				connSocket = createSocket();
				if (connSocket != null) {
					Log.i("Geofence Creation", "Successfully created socket connection");

					sockIStream = getSocketInputStream(connSocket);
					if (sockIStream != null) {
						Log.i("Geofence Creation", "Successfully fetched input stream");

						sockIReader = inputStreamToReader(sockIStream);
						sockBufferedIReader = bufferReader(sockIReader);

						sockOStream = getSocketOutputStream(connSocket);
						if (sockOStream != null) {
							Log.i("Geofence Creation", "Successfully created output stream");
							sockOWriter = outputStreamToWriter(sockOStream);

							writeToSocket(sockOWriter, "GETDESTINATION;"+username+","+token+","+groupID);
							response = getResponse(sockBufferedIReader);

							Log.i("Geofence Creation", "Closing streams");
							closeStream(sockIStream);
							closeStream(sockOStream);

							Log.i("Geofence Creation", "Closing socket");
							closeSocket(connSocket);
						}
					}
				}

				String[] groupDestination = response.split(",");
				double[] groupDestinationDouble = {Double.parseDouble(groupDestination[0]), Double.parseDouble(groupDestination[1])};

				Intent proximityAlertIntent = new Intent();
				proximityAlertIntent.setAction("tk.aegisstudios.traveltracker.locationArrivalReceiver");

				proximityAlertIntent.putExtra("groupID", groupID);
				proximityAlertIntent.putExtra("username", username);
				proximityAlertIntent.putExtra("token", token);

				locationManager.addProximityAlert(groupDestinationDouble[0], groupDestinationDouble[1], PROXIMITY_ALERT_RADIUS, -1, PendingIntent.getBroadcast(getApplicationContext(), groupID, proximityAlertIntent, PendingIntent.FLAG_UPDATE_CURRENT));

				locationManager.requestLocationUpdates(locationManager.getBestProvider(locCriteria, true), 30, 0, locationListener);
			}
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

	private void redirectToSplash() {
		Intent intent = new Intent(this, StarterActivity.class);
		startActivity(intent);
	}

	private Socket createSocket() {
		Socket connSocket = null;
		try {
			connSocket = new Socket("192.168.1.8", 1337);
		} catch (IOException e) {
			Log.e("InOutSocketClass#createSocket", "IOException while creating socket");
			e.printStackTrace();
		}
		return connSocket;
	}

	private InputStream getSocketInputStream(Socket connSocket) {
		InputStream socketInputStream = null;
		try {
			socketInputStream = connSocket.getInputStream();
		} catch (IOException e) {
			Log.e("InOutSocketClass#getSocketInputStream", "IOException while getting input stream");
			e.printStackTrace();
		}
		return socketInputStream;
	}

	private OutputStream getSocketOutputStream(Socket connSocket) {
		OutputStream socketOutputStream = null;
		try {
			socketOutputStream = connSocket.getOutputStream();
		} catch (IOException e) {
			Log.e("InOutSocketClass#getSocketOutputStream", "IOException while getting output stream");
		}
		return socketOutputStream;
	}

	private InputStreamReader inputStreamToReader(InputStream stream) {
		return new InputStreamReader(stream);
	}

	private OutputStreamWriter outputStreamToWriter(OutputStream stream) {
		return new OutputStreamWriter(stream);
	}

	private BufferedReader bufferReader(Reader reader) {
		BufferedReader createdBufferedReader = new BufferedReader(reader);
		return createdBufferedReader;
	}

	private boolean writeToSocket(OutputStreamWriter writer, String message) {
		boolean returnValue = false;
		try {
			writer.write(message);
			writer.flush();
			returnValue = true;
		} catch (IOException e) {
			Log.e("InOutSocketClass#writeToSocket", "Error while writing to socket");
		}
		return returnValue;
	}

	private String getResponse(BufferedReader reader) {
		boolean responseIsNull = true;
		String response = null;
		while (responseIsNull) {
			try {
				response = reader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (response != null) {
				Log.i("InOutSocketClass#getResponse", "Received response");
				responseIsNull = false;
				if (!closeReader(reader)) {
					Log.e("InOutSocketClass#getResponse", "closeReader returned error");
				}
			}
		}
		return response;
	}

	private boolean closeReader(BufferedReader reader) {
		boolean returnValue = false;
		try {
			reader.close();
			returnValue = true;
		} catch (IOException e) {
			Log.e("InOutSocketClass#closeReader", "IOException while closing reader");
		}
		return returnValue;
	}

	private boolean closeStream(InputStream stream) {
		boolean returnValue = false;
		try {
			stream.close();
			returnValue = true;
		} catch (IOException e) {
			Log.e("InOutSocketClass#closeStream", "IOException while closing stream");
		}
		return returnValue;
	}

	private boolean closeStream(OutputStream stream) {
		boolean returnValue = false;
		try {
			stream.close();
			returnValue = true;
		} catch (IOException e) {
			Log.e("InOutSocketClass#closeStream", "IOException while closing stream");
		}
		return returnValue;
	}

	private boolean closeSocket(Socket socket) {
		boolean returnValue = false;
		try {
			socket.close();
			returnValue = true;
		} catch (IOException e) {
			Log.e("InOutSocketClass#closeSocket", "IOException while closing socket");
		}
		return returnValue;
	}
}
