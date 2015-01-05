package tk.aegisstudios.traveltracker;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class StarterActivity extends Activity {
	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starter);
        
		if (accountDoesExist()) {
			new Redirection(this).redirectToHome();
		}
		
		setHeaderFont();
	}

	public void onReg (View v) {
		new Redirection(this).redirectToRegistration();
	}
	
	public void onSign (View v) {
		new Redirection(this).redirectToSignIn();
	}
	
	private boolean accountDoesExist() {
		return new File(this.getFilesDir(), "savedAuth.txt").exists();
	}
	
	private void setHeaderFont() {
		Typeface sspl = Typeface.createFromAsset(getAssets(), "sspl.ttf");
		TextView logo = (TextView) findViewById(R.id.logo);
		logo.setTypeface(sspl);
	}
	
	
}
