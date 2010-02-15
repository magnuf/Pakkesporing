package com.krashk.pakketracker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.IBinder;

public class TrackingService extends Service {

	private PackagesDbAdapter packagesDbAdapter;
	
	
	@Override
	public void onCreate(){
		packagesDbAdapter = new PackagesDbAdapter(this);
		packagesDbAdapter.open();
	}
	
	@Override
    public void onStart(Intent intent, int startId) {
		//if background data is disabled, don't do anything
		if(!((ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE)).getBackgroundDataSetting()){
			return;
		}
		
    	boolean hasChanges = TrackingUtils.updateAllPackages(packagesDbAdapter);
    	
    	if ( hasChanges ){
    		PackageTracker pt = ((PackageTracker)getApplicationContext());
            if (!pt.isAppRunning()){ // Vil ikke kjøre igang notifcation om appen er oppe og kjører
	    		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				Notification notification = new Notification(R.drawable.icon, "Ny status på sending", 0);
				PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainListView.class), 0);
				notification.setLatestEventInfo(this, "Statusendring for sending", "En av dine sendinger har endringer i status", pendingIntent);
				notificationManager.cancel(R.attr.notification_id); // Om det er nye endringer uten at bruker har sjekket, fjern forrige note.
				notificationManager.notify(R.attr.notification_id, notification);
            }
    	}
    }
	@Override
	public void onDestroy() {
		packagesDbAdapter.close();
		TrackingUtils.updateTrackingService(this);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}



}

