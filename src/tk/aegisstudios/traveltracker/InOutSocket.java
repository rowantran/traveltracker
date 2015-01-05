package tk.aegisstudios.traveltracker;

import java.io.*;
import java.net.Socket;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Travel Tracker server interface through socket.
 * {@link #onPostExecute} must be overwritten when
 * extending this class.
 */
public abstract class InOutSocket extends AsyncTask<String, Void, String> {
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
			Log.i("InOutSocket#doInBackground", "Successfully created socket connection");

			sockIStream = getSocketInputStream(connSocket);
			if (sockIStream != null) {
				Log.i("InOutSocket#doInBackground", "Successfully fetched input stream");

				sockIReader = inputStreamToReader(sockIStream);
				sockBufferedIReader = bufferReader(sockIReader);

				sockOStream = getSocketOutputStream(connSocket);
				if (sockOStream != null) {
					Log.i("InOutSocket#doInBackground", "Successfully created output stream");
					sockOWriter = outputStreamToWriter(sockOStream);

					writeToSocket(sockOWriter, params[0]);
					response = getResponse(sockBufferedIReader);

					Log.i("InOutSocket#doInBackground", "Closing streams");
					closeStream(sockIStream);
					closeStream(sockOStream);

					Log.i("InOutSocket#doInBackground", "Closing socket");
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
			Log.e("InOutSocket#createSocket", "IOException while creating socket");
			e.printStackTrace();
		}
		return connSocket;
	}

	private InputStream getSocketInputStream(Socket connSocket) {
		InputStream socketInputStream = null;
		try {
			socketInputStream = connSocket.getInputStream();
		} catch (IOException e) {	
			Log.e("InOutSocket#getSocketInputStream", "IOException while getting input stream");
			e.printStackTrace();
		}
		return socketInputStream;
	}
	
	private OutputStream getSocketOutputStream(Socket connSocket) {
		OutputStream socketOutputStream = null;
		try {
			socketOutputStream = connSocket.getOutputStream();
		} catch (IOException e) {
			Log.e("InOutSocket#getSocketOutputStream", "IOException while getting output stream");
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
			Log.e("InOutSocket#writeToSocket", "Error while writing to socket");
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
				Log.i("InOutSocket#getResponse", "Received response");
				responseIsNull = false;
				if (!closeReader(reader)) {
					Log.e("InOutSocket#getResponse", "closeReader returned error");
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
			Log.e("InOutSocket#closeReader", "IOException while closing reader");
		}
		return returnValue;
	}

	private boolean closeStream(InputStream stream) {
		boolean returnValue = false;
		try {
			stream.close();
			returnValue = true;
		} catch (IOException e) {
			Log.e("InOutSocket#closeStream", "IOException while closing stream");
		}
		return returnValue;
	}

	private boolean closeStream(OutputStream stream) {
		boolean returnValue = false;
		try {
			stream.close();
			returnValue = true;
		} catch (IOException e) {
			Log.e("InOutSocket#closeStream", "IOException while closing stream");
		}
		return returnValue;
	}

	private boolean closeSocket(Socket socket) {
		boolean returnValue = false;
		try {
			socket.close();
			returnValue = true;
		} catch (IOException e) {
			Log.e("InOutSocket#closeSocket", "IOException while closing socket");
		}
		return returnValue;
	}

	@Override
	protected abstract void onPostExecute(String result);
}