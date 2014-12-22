package tk.aegisstudios.traveltracker;

import java.io.*;
import java.net.Socket;

import android.os.AsyncTask;
import android.util.Log;


public class InOutSocketClass extends AsyncTask<String, Void, String> {
	Socket connSocket;

	InputStream sockIStream;
	InputStreamReader sockIReader;
	BufferedReader sockBufferedIReader;

	OutputStream sockOStream;
	OutputStreamWriter sockOWriter;
	
	String response = null;
	
	@Override
	protected String doInBackground (String... params) {	
		connSocket = createSocket();
		if (connSocket != null) {
			Log.i("InOutSocketClass#doInBackground", "Successfully created socket connection");

			sockIStream = getSocketInputStream(connSocket);
			if (sockIStream != null) {
				Log.i("InOutSocketClass#doInBackground", "Successfully fetched input stream");

				sockIReader = inputStreamToReader(sockIStream);
				sockBufferedIReader = bufferReader(sockIReader);

				sockOStream = getSocketOutputStream(connSocket);
				if (sockOStream != null) {
					sockOWriter = outputStreamToWriter(sockOStream);

					writeToSocket(sockOWriter, params[0]);
					response = getResponse(sockBufferedIReader);

					closeStream(sockIStream);
					closeStream(sockOStream);

					closeSocket(connSocket);
				}
			}
		}
		return response;
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
				response = sockBufferedIReader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (response != null) {
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

	@Override
	protected void onPostExecute(String result) {
		// To be overridden by children
	}
}