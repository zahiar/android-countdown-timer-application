package com.zahiar.countdowntimer;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;

/**
 * This activity is used to show the user dialog once the timer has finished.
 *
 * @author Zahiar Ahmed
 */
public class TimerDialogActivity extends Activity {

    /**
     * This method initialises the activity and calls a helper method to create
     * and show the dialog.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createAndShowDialog();
    }

    /**
     * This method creates the dialog, setting up all the required parameters
     * and then displays the dialog.
     */
    private void createAndShowDialog() {
        Uri ringtoneUri = RingtoneManager
            .getDefaultUri(RingtoneManager.TYPE_ALARM);
        final Ringtone ringtone = RingtoneManager
            .getRingtone(this, ringtoneUri);

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setCancelable(false)
            .setTitle(getResources().getString(R.string.app_name))
            .setMessage("Times up!")
            .setPositiveButton("Dismiss",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (ringtone != null) {
                            ringtone.stop();
                        }

                        dialog.dismiss();
                        finish();
                    }
                }).create();

        dialog.setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {
                if (ringtone != null) {
                    ringtone.play();
                }
            }
        });

        dialog.show();
    }
}
