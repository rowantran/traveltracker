package tk.aegisstudios.traveltracker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends Activity {
    
    Button reg;
    
    EditText registrationUser;
    EditText registrationEmail;
    EditText registrationPass;
    EditText registrationConfPass;
    
    Location lastKnownLocation;
    LocationManager locationManager;
    LocationListener locationListener;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    
        registrationUser = (EditText) findViewById(R.id.regUser);
        registrationEmail = (EditText) findViewById(R.id.regEmail);
        registrationPass = (EditText) findViewById(R.id.regPass);
        registrationConfPass = (EditText) findViewById(R.id.regPass2);
    }
    
    public void onReg(View v) {
        if (FieldUtil.fieldIsBlank(registrationUser) || FieldUtil.fieldIsBlank(registrationPass) || FieldUtil.fieldIsBlank(registrationEmail) || FieldUtil.fieldIsBlank(registrationConfPass)) {
            showToast("Please fill in all fields.");
        } else {
            if (FieldUtil.fieldsEqual(registrationPass, registrationConfPass)) {
                new Registerer().execute("CREATE;" + registrationUser.getText().toString().replace(",", "") + "," + registrationEmail.getText().toString().replace(",", "") + "," + registrationPass.getText().toString().replace(",", ""));
            } else {
                Toast.makeText(getApplicationContext(), R.string.passMismatched, Toast.LENGTH_LONG).show();
            }
        }
    }

    public class Registerer extends InOutSocketClass {
        @Override
        protected void onPostExecute(String result) {
            String regToastMessage;

            if (result.split(";").length > 1 && result.split(";")[0].equals("Success")) {
                regToastMessage = "Account succesfully created";
                
                FileOutputStream savedAuthStream = openAuthenticationData();
                boolean authSaved = writeAuthenticationData(savedAuthStream, result.split(";")[1]);
                
                if (authSaved) {
                	showToast("Succesfully saved login data!");
                } else {
                	showToast("Failed to save login data.");
                }
          
                redirectToHome();
            } 
            
            switch (result) {
                case "xn":
                    regToastMessage = "Unable to connect to server. Are you connected to the Internet?";
                    break;
                case "Access error":
                    regToastMessage = "Sorry, we had a problem accessing the database. Please try again.";
                    break;
                case "Existing user error":
                    regToastMessage = "Sorry, that user already exists. Please pick a different username and try again.";
                    break;
                case "User adding error":
                    regToastMessage = "Sorry, we had a problem while registering your account. Please try again.";
                    break;
                default:
                    regToastMessage = "Unknown error code.";
            }
            
            showToast(regToastMessage);
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
    
    private void redirectToHome(){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }
    
}
