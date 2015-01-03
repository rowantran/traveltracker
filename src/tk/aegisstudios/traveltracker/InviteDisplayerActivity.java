package tk.aegisstudios.traveltracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.*;

public class InviteDisplayerActivity extends Activity {
    private boolean wasAccepted = false;

    private String username;
    private String token;
    private final static String AUTHENTICATION_DATA_LOCATION = "savedAuth.txt";

    private String groupName;
    private int groupID;
    private String groupInviter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitedisplayer);

        groupName = getIntent().getExtras().getString("groupName");
        groupID = getIntent().getExtras().getInt("groupID");
        groupInviter = getIntent().getExtras().getString("groupInviter");

        getActionBar().setTitle(groupName);

        TextView inviteGroupName = (TextView) findViewById(R.id.inviteGroupName);
        inviteGroupName.setText(groupName);

        TextView inviteGroupInviter = (TextView) findViewById(R.id.inviteGroupInviter);
        inviteGroupInviter.setText("Invited to group by " + groupInviter);


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
    }

    public void onAccept(View v) {
        wasAccepted = true;
        new InviteDecisionSender().execute("REPLYINVITE;"+username+","+token+","+groupID+",y");
    }

    public void onDecline(View v) {
        wasAccepted = false;
        new InviteDecisionSender().execute("REPLYINVITE;"+username+","+token+","+groupID+",n");
    }

    private void showToast(String toastMessage) {
        Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG).show();
    }

    private void redirectToSplash() {
        Intent intent = new Intent(this, StarterActivity.class);
        startActivity(intent);
    }

    public class InviteDecisionSender extends InOutSocketClass {
        @Override
        public void onPostExecute(String result) {
            String confirmationToastMessage;
            if (result.equals("Success")) {
                if (wasAccepted) {
                    confirmationToastMessage = "Successfully accepted invite";
                } else {
                    confirmationToastMessage = "Successfully declined invite";
                }
            } else {
                if (wasAccepted) {
                    confirmationToastMessage = "Failed to accept invite";
                } else {
                    confirmationToastMessage = "Failed to decline invite";
                }
            }

            showToast(confirmationToastMessage);
        }
    }
}