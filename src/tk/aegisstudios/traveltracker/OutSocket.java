package tk.aegisstudios.traveltracker;

/**
 * Extends InOutSocket to allow usage of the class with no handler for
 * server return. Must be called in same way as {@link InOutSocket}.
 */

public class OutSocket extends InOutSocket {
    @Override
    public void onPostExecute(String result) {}
}
