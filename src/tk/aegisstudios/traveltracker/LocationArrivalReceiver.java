package tk.aegisstudios.traveltracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class LocationArrivalReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle intentInformation = intent.getExtras();
        if (intentInformation.getBoolean("KEY_PROXIMITY_ENTERING")) {
            Log.i("LocationArrivalReceiver", "User entered group destination area");
            int groupID = intentInformation.getInt("groupID");
            String username = intentInformation.getString("username");
            String token = intentInformation.getString("token");

            Log.i("LocationArrivalReceiver", "Sending notification to server");
            new LocationArrivalMessenger().execute("LOCATIONARRIVAL;"+username+","+token+","+groupID);
        }
    }

    public class LocationArrivalMessenger extends InOutSocket {
        @Override
        public void onPostExecute(String result) {
            if (result.equals("Success")) {
                Log.i("LocationArrivalMessenger", "Successfully notified server of location arrival");
            } else {
                Log.w("LocationArrivalMessenger", "Server failed to register location arrival");
            }
        }
    }
}
