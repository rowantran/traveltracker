package tk.aegisstudios.traveltracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.Socket;

import android.os.AsyncTask;

public class InOutSocketClass extends AsyncTask<String, Void, String> {
	Socket sock;
	InputStream sockIStream;
	InputStreamReader sockIReader;
	BufferedReader sockBISR;
	OutputStream sockOS;
	OutputStreamWriter sockOSW;
	
	String response;
	
	@Override
	protected String doInBackground (String... params) {	
		sock = createSocket();
		
		sockIStream = getSocketInputStream(sock);
		sockIReader = inputStreamToReader(sockIStream);
		
		sockBISR = new BufferedReader(sockISR);
		sockOS = sock.getOutputStream();
		sockOSW = new OutputStreamWriter(sockOS);
		
		try {
			sockOSW.write(params[0]);
			sockOSW.flush();
			
			boolean responseIsNull = true;
			while (responseIsNull) {
				try {
					response = sockBISR.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
				// Exit loop once data is received
				if (response != null) {
					responseIsNull = false;
					sockBISR.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
		
	}
	
	private Socket createSocket() {
		Socket sockObj = null;
		try {
			sockObj = new Socket("192.168.1.8", 1337);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sockObj;
	}
	
	private InputStream getSocketInputStream(Socket sockObj) {
		InputStream sockInputStream = null;
		try {
			sockInputStream = sockObj.getInputStream();
		} catch (IOException e) {	
			e.printStackTrace();
		}
		return sockInputStream;
	}
	
	
	private InputStreamReader inputStreamToReader(InputStream stream) {
		
	}
	
	@Override
	protected void onPostExecute(String result) {
		// To be overridden by child classes
        ;
	}
}