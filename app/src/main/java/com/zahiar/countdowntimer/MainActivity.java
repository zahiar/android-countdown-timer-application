package com.zahiar.countdowntimer;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * This is the main activity which allows the user to set a timer to count down
 * from.
 * 
 * @author Zahiar Ahmed
 */
public class MainActivity extends Activity {
	public static final String DURATION_VALUE = "duration_value";
	public static final String MAP_HOURS = "hours";
	public static final String MAP_MINUTES = "minutes";
	public static final String MAP_SECONDS = "seconds";

	private LocalBroadcastManager mLocalBroadcastManager;
	private Receiver mLocalReceiver;
	private TextView hoursTextView;
	private TextView minsTextView;
	private TextView secsTextView;
	private String countdownValue = "";
	private Intent serviceIntent = null;

	/**
	 * This method initialises the layout of the Activity and the LocalBroadcast
	 * manager and receiver.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
		mLocalReceiver = new Receiver();
		registerLocalReceiver();

		hoursTextView = (TextView) findViewById(R.id.textView5);
		minsTextView = (TextView) findViewById(R.id.TextView01);
		secsTextView = (TextView) findViewById(R.id.TextView05);
	}

	/**
	 * When the application resumes, the local receiver will be re-registered,
	 * and the countdown variables will be reset. This is to handle the edge
	 * case where if the service finishes whilst the application is paused, when
	 * it resumes the application will think the service is still running.
	 */
	@Override
	protected void onResume() {
		super.onResume();

		registerLocalReceiver();
		resetCountdown();
	}

	/**
	 * When the application pauses, the local broadcast manager is unregistered
	 * in order to save battery life, as the activity is no longer visible.
	 */
	@Override
	protected void onPause() {
		super.onPause();

		mLocalBroadcastManager.unregisterReceiver(mLocalReceiver);
	}

	/**
	 * This method registers a set of defined broadcasts which will be handled
	 * by this activity.
	 */
	private void registerLocalReceiver() {
		IntentFilter filter = new IntentFilter(
				TimerService.BROADCAST_ACTION_TIMER_RUNNING);
		filter.addAction(TimerService.BROADCAST_ACTION_TIMER_FINISHED);
		mLocalBroadcastManager.registerReceiver(mLocalReceiver, filter);
	}

	/**
	 * This methods gets the service intent.
	 * 
	 * @return The serviceIntent variable
	 */
	private Intent getServiceIntent() {
		return serviceIntent;
	}

	/**
	 * This method overwrites the serviceIntent variable with a new Intent.
	 */
	private void setServiceIntent() {
		serviceIntent = new Intent(this, TimerService.class);
	}

	/**
	 * This method starts the background service when the user presses the
	 * "Set Timer" button.
	 * 
	 * @param view The view
	 */
	public void startService(View view) {
		if (serviceIntent == null) {
			long durationValue = calculateTimeInSecs(countdownValue);

			if (durationValue > 0) {
				serviceIntent = new Intent(this, TimerService.class);
				serviceIntent.putExtra(DURATION_VALUE, durationValue);
				startService(serviceIntent);
			}
		} else {
			Toast.makeText(this, "Timer is already running", Toast.LENGTH_SHORT)
                .show();
		}
	}

	/**
	 * This method stops the background service when the user presses the
	 * "Stop Timer" button, and resets all countdown related variables, as well
	 * as the display.
	 * 
	 * @param view The view
	 */
	public void stopService(View view) {
		if (serviceIntent != null) {
			stopService(serviceIntent);
			resetCountdown();
		} else {
			Toast.makeText(this, "Timer is not running", Toast.LENGTH_SHORT)
                .show();
		}
	}

	/**
	 * This method handles the user's input when they press one of the number
	 * keys (buttons).
	 * 
	 * @param view The view
	 */
	public void addNumber(View view) {
		int strLength = countdownValue.length();
		if (serviceIntent == null && strLength < 6) {
			Button buttonView = (Button) view;
			String buttonText = buttonView.getText().toString();

			if (strLength == 0 && buttonText.equals("0")) {
				return;
			}

			countdownValue += buttonText;
			updateDisplayUI(convertTimeStrToSeconds(countdownValue));
		} else {
			Toast.makeText(this, "Timer is already running", Toast.LENGTH_SHORT)
                .show();
		}
	}

	/**
	 * This method handles the removing of a number from the user input.
	 * 
	 * @param view The view
	 */
	public void removeNumber(View view) {
		int strLength = countdownValue.length();

		if (serviceIntent == null && strLength > 0) {
			if (strLength == 1) {
				countdownValue = "";
			} else {
				countdownValue = countdownValue.substring(0, strLength - 1);
			}

			updateDisplayUI(convertTimeStrToSeconds(countdownValue));
		}
	}

	/**
	 * This method resets the countdown by resetting the countdown variables to
	 * their defaults.
	 */
	private void resetCountdown() {
		countdownValue = "";
		updateDisplayUI(0, 0, 0);
		serviceIntent = null;
	}

	/**
	 * This method takes in a String of seconds (HHMMSS) and converts it into a
	 * Map containing the individual hours, minutes and seconds.
	 * 
	 * @param timeStr String of seconds in format, HHMMSS
	 * 
	 * @return A map containing the individual hours, minutes and seconds.
	 */
	private Map<String, Integer> convertTimeStrToSeconds(String timeStr) {
		String hours = "0";
		String minutes = "0";
		String seconds = "0";

		int stringLength = timeStr.length();
		if (stringLength > 0 && stringLength <= 2) {
			seconds = timeStr;
		} else if (stringLength > 2) {
			seconds = timeStr.substring(stringLength - 2);

			int minutesSubStrStart = 0;
			if (stringLength == 5) {
				minutesSubStrStart = 1;
			} else if (stringLength == 6) {
				minutesSubStrStart = 2;
			}
			minutes = timeStr.substring(minutesSubStrStart, stringLength - 2);

			if (stringLength >= 5) {
				hours = timeStr.substring(0, stringLength - 4);
			}
		}

		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put(MAP_HOURS, Integer.valueOf(hours));
		map.put(MAP_MINUTES, Integer.valueOf(minutes));
		map.put(MAP_SECONDS, Integer.valueOf(seconds));

		return map;
	}

	/**
	 * This method converts seconds into milliseconds.
	 *
	 * @param secondsStr String of seconds in format, HHMMSS
	 *
	 * @return A long value representing the milliseconds
	 */
	private long calculateTimeInSecs(String secondsStr) {
		Map<String, Integer> map = convertTimeStrToSeconds(secondsStr);

		return (map.get(MAP_HOURS) * 3600) + (map.get(MAP_MINUTES) * 60)
            + map.get(MAP_SECONDS);
	}

	/**
	 * Updates the display by setting the new values
	 * 
	 * @param hours The new hours value
	 * @param mins The new minutes value
	 * @param secs The new seconds value
	 */
	private void updateDisplayUI(Integer hours, Integer mins, Integer secs) {
		hoursTextView.setText(String.format("%02d", hours));
		minsTextView.setText(String.format("%02d", mins));
		secsTextView.setText(String.format("%02d", secs));
	}

	/**
	 * Updates the display by setting the new values
	 * 
	 * @param map A map containing the values hours, minutes and seconds
	 */
	private void updateDisplayUI(Map<String, Integer> map) {
		updateDisplayUI(map.get(MAP_HOURS), map.get(MAP_MINUTES),
            map.get(MAP_SECONDS));
	}

	/**
	 * This class handles all broadcasts sent by this application.
     *
     * @author Zahiar Ahmed
     */
	public class Receiver extends BroadcastReceiver {

		/**
		 * This method handles both the "TIMER_FINISHED" and "TIMER_RUNNING"
		 * broadcasts sent.
		 */
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (action.equals(TimerService.BROADCAST_ACTION_TIMER_FINISHED)) {
				resetCountdown();
			} else if (action.equals(TimerService.BROADCAST_ACTION_TIMER_RUNNING)) {
				/*
				 * If we have received this broadcast but the serviceIntent
				 * variable is null, then we know the application has just been
				 * restarted/resumed so we set the serviceIntent again.
				 */
				if (getServiceIntent() == null) {
					setServiceIntent();
				}

				long remainingSecs = intent.getLongExtra(
						TimerService.INTENT_COUNTDOWN_VALUE, 0);

				Map<String, Integer> map = new HashMap<String, Integer>();
				map.put(MAP_HOURS, (int) (remainingSecs / 3600));
				map.put(MAP_MINUTES, (int) ((remainingSecs % 3600) / 60));
				map.put(MAP_SECONDS, (int) (remainingSecs % 60));

				updateDisplayUI(map);
			}
		}
	}
}
