package com.krashk.pakketracker;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

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
    	
    	if (c.moveToFirst()){
    		do {
    			int packageid = c.getInt(c.getColumnIndex(PackagesDbAdapter.KEY_ID));
    			String packageNumber = c.getString(c.getColumnIndex(PackagesDbAdapter.KEY_NUMBER));
    			String oldStatus = c.getString(c.getColumnIndex(PackagesDbAdapter.KEY_STATUS));
    			try {
    				String newStatus = TrackingUtils.updateStatus(packageNumber, oldStatus);
    				if (newStatus != null){
    					packagesDbAdapter.updatePackage(packageid, newStatus, R.attr.changed);
    				}
    			} catch (ClientProtocolException e) {
    				// No action needed
    			} catch (IOException e) {
    				// No action needed
    			}
    		} while (c.moveToNext());
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

