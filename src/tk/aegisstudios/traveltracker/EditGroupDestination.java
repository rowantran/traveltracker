package tk.aegisstudios.traveltracker;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class EditGroupDestination extends Activity {
    private int groupID;
    private String groupName, username, token;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editgroupdestination);

        groupName = getIntent().getExtras().getString("groupName");
        groupID = getIntent().getExtras().getInt("groupID");
        username = getIntent().getExtras().getString("username");
        token = getIntent().getExtras().getString("token");
    }

    private void showToast(String toastMessage) {
        Toast.makeText(getApplicationContext(), toastMessage,
                Toast.LENGTH_LONG).show();
    }

    public void onSubmitGroupDestination(View v) {
        EditText destinationEditText = (EditText) findViewById(R.id.destination_edittext);
        String destinationText = destinationEditText.getText().toString();
        double lat, lng;

        Geocoder gc = new Geocoder(getApplicationContext());

        if(gc.isPresent()){
            try {
                List<Address> list = gc.getFromLocationName(destinationText, 1);

                Address address = list.get(0);

                lat = address.getLatitude();
                lng = address.getLongitude();

                String destinationLatitude = String.valueOf(lat);
                String destinationLongitude = String.valueOf(lng);


                new GroupDestinationSubmitter().execute("SUBMITDESTINATION;"+username+","+token+","+groupID+","+destinationLatitude+","+destinationLongitude);
            } catch (IOException e) {
                e.printStackTrace();
                showToast("Error! Please restart the app and try again");
            }
        }
    }

    public class GroupDestinationSubmitter extends InOutSocket {
        @Override
        public void onPostExecute(String result) {
            if (result.equals("Success")) {
                showToast("Successfully changed group destination!");
                finish();

                Intent intent = new Intent(getApplicationContext(), DisplayGroupActivity.class);
                intent.putExtra("groupName", groupName);
                intent.putExtra("groupID", groupID);
                startActivity(intent);
            } else {
                showToast("Error");
            }
        }
    }
}