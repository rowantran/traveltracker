package tk.aegisstudios.traveltracker;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Method wrapper class for redirecting the user to different activities in Travel Tracker.
 */
class Redirection {
    private Context enclosingContext;

    /**
     * Instantiates a new {@code Redirection}.
     * @param sentEnclosingContext context of the {@link android.app.Activity} {@code Redirection}
     *                             is being used in
     */
    Redirection(Context sentEnclosingContext) {
        this.enclosingContext = sentEnclosingContext;
    }

    /**
     * Convenience method. Redirects the user to the splash screen (i.e.
     * {@link StarterActivity})
     */
    void redirectToSplash() {
        Intent splashIntent = createIntent(StarterActivity.class);
        enclosingContext.startActivity(splashIntent);
    }

    /**
     * Convenience method. Redirects the user to invite display screen (i.e.
     * {@link InviteDisplayActivity})
     *
     * @param groupName non-unique name of the group client is being invited to
     * @param groupID unique numeric identifier of the group user is being invited to
     * @param groupInviter unique username of the user inviting client to group
     */
    void redirectToInviteDisplay(String groupName, int groupID, String groupInviter) {
        Intent inviteDisplayIntent = createIntent(InviteDisplayActivity.class);
        inviteDisplayIntent.putExtra("groupName", groupName);
        inviteDisplayIntent.putExtra("groupID", groupID);
        inviteDisplayIntent.putExtra("groupInviter", groupInviter);

        enclosingContext.startActivity(inviteDisplayIntent);
    }

    /**
     * Convenience method. Redirects the user to main screen (i.e.
     * {@link MainActivity})
     */
    void redirectToHome() {
        Intent homeIntent = createIntent(MainActivity.class);
        enclosingContext.startActivity(homeIntent);
    }

    /**
     * Convenience method. Redirects the user to main screen after
     * displaying message (i.e. {@link MainActivity})
     *
     * @param toastMessage message to be displayed
     */
    void redirectToHome(String toastMessage) {
        showToast(toastMessage);
        redirectToHome();
    }

    /**
     * Convenience method. Redirects the user to registration
     * screen (i.e. {@link RegisterActivity})
     */
    void redirectToRegistration() {
        Intent registrationIntent = createIntent(RegisterActivity.class);
        enclosingContext.startActivity(registrationIntent);
    }

    /**
     * Convenience method. Redirects the user to sign in (i.e.
     * {@link SignInActivity})
     */
    void redirectToSignIn() {
        Intent signInIntent = createIntent(SignInActivity.class);
        enclosingContext.startActivity(signInIntent);
    }

    Intent createIntent(Class className) {
        Intent createdIntent = new Intent(enclosingContext, className);
        createdIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        createdIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        return createdIntent;
    }

    private void showToast(String toastMessage) {
        Toast.makeText(enclosingContext, toastMessage, Toast.LENGTH_LONG).show();
    }
}
