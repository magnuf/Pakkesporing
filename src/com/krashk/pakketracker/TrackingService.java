package com.krashk.pakketracker;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
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
    	Cursor c =  packagesDbAdapter.fetchAllPackages();
    	boolean hasChanges = false;
    	if (c.moveToFirst()){
    		do {
    			int packageid = c.getInt(c.getColumnIndex(PackagesDbAdapter.KEY_ID));
    			String packageNumber = c.getString(c.getColumnIndex(PackagesDbAdapter.KEY_NUMBER));
    			String oldStatus = c.getString(c.getColumnIndex(PackagesDbAdapter.KEY_STATUS));
    			try {
    				String newStatus = TrackingUtils.updateStatus(packageNumber, oldStatus);
    				if (newStatus != null){
    					packagesDbAdapter.updatePackage(packageid, newStatus, R.attr.changed);
    					hasChanges = true;
    				}
    			} catch (ClientProtocolException e) {
    				// No action needed
    			} catch (IOException e) {
    				// No action needed
    			}
    		} while (c.moveToNext());
    	}
    	c.close();
    	if ( hasChanges ){
    		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			Notification notification = new Notification(R.drawable.icon, "Test", 0);
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainListView.class), 0);
			notification.setLatestEventInfo(this, "Statusendring for sending", "En av dine sendinger har endringer i status", pendingIntent);
			notificationManager.notify(0, notification);
    	}
    }
	@Override
	public void onDestroy() {
		packagesDbAdapter.close();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}



}

