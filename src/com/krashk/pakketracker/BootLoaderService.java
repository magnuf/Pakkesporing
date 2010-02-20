package com.krashk.pakketracker;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootLoaderService extends BroadcastReceiver{

	public static final String TAG = "LocationLoggerServiceManager";

	@Override
	public void onReceive(Context context, Intent intent) {
		// just make sure we are getting the right intent (better safe than sorry)
		if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
			TrackingUtils.updateTrackingService(context);
		} else {
			Log.e(TAG, "Received unexpected intent " + intent.toString());
		}
	}
}
