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
import java.net.Socket;
import java.net.UnknownHostException;

import org.w3c.dom.UserDataHandler;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class SignInActivity extends Activity {
	
    EditText signUser;
    EditText signPass;
    
    static boolean isCompleted;
    static String result = "";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        
        signUser = (EditText) findViewById(R.id.signUser);
        signPass = (EditText) findViewById(R.id.signPass);
    }
    
    public void onSignIn(View v) {
        new SignIn().execute("SIGNIN;" + signUser.getText() + "," + signPass.getText());
    }
    
    public class SignIn extends InOutSocketClass {
    	
        @Override
        protected void onPostExecute(String result) {
            String signToastMessage;
            if (result.split(";").length > 1) {
            	signToastMessage = "You succesfully logged in.";
                FileOutputStream savedAuthStream = openAuthenticationData();
                
                String[] separated = result.split(";");
                boolean authSaved = writeAuthenticationData(savedAuthStream, separated[1]);
                
                redirectToHome();
            } else if (result.split(";").length.equals(1)) {
            	String returnVal = result.split(";")[0];
            	switch (returnVal) {
            	    case "Wrong password":
            	    	signToastMessage = "Incorrect password entered";
            	    	break;
            	    case "User does not exist":
            	    	signToastMessage = "User does not exist";
            	    	break;
            	    case "Access error":
            	    	signToastMessage = "Error while accessing database";
            	    	break;
            	    default:
            	    	signToastMessage = "Invalid server response";
            	    	break;
            	}
            }
            Toast.makeText(getApplicationContext(), signToastMessage, Toast.LENGTH_LONG).show();
        }
    }

    private FileOutputStream openAuthenticationData() {
    	File savedAuth;
    	FileOutputStream savedAuthStream;
        
        savedAuth = new File(getFilesDir(), "savedAuth.txt");
        savedAuthStream = convertFileToStream(savedAuth);
        
        return savedAuthStream;
    }
    
    private boolean writeAuthenticationData(FileOutputStream savedAuthStream, String data) {
    	OutputStreamWriter savedAuthWriter = null;
    	
    	try {
	    	savedAuthWriter = new OutputStreamWriter(savedAuthStream);
	    	savedAuthWriter.write(data);
	    	return true;
    	} catch (IOException exception) {
    		exception.printStackTrace();
    		return false;
    	} finally {
    		try {
		    	savedAuthWriter.flush();
		    	savedAuthWriter.close();
		    	savedAuthStream.close();
    		} catch (IOException exception) {
    			exception.printStackTrace();
    			return false;
    		}
    	}
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
    
    private void redirectToHome() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }
}
