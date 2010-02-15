package com.krashk.pakketracker;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;

public class TrackingUtils {


	public static String updateStatus(String packageNumber, String oldStatus) throws ClientProtocolException, IOException {	
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet("http://sporing.posten.no/sporing.html?q="+packageNumber);
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String responseBody = null;
	
		responseBody = client.execute(get, responseHandler);
		
		responseBody = responseBody.replace("\n", "");
		// first find the event field
		int startIndex = responseBody.indexOf("<div class=\"sporing-sendingandkolli-latestevent-text\">");
		int endIndex = responseBody.indexOf("</div>", startIndex);
		String newStatus = new String();
		if(startIndex == -1) {
             // no results
             newStatus = "Ugyldig sendingsnummer eller tjenestefeil";
             if (newStatus.equals(oldStatus)){
     			return null; // no changes
     		}
     		else {
     			return newStatus;
     		}
		}
         // remove all tags, whitespace - and trim
		newStatus = responseBody.substring(startIndex, endIndex)
			.replaceAll("\\<.*?\\>","").replaceAll("\\s+", " ").trim();
	
		// now find the date field
		startIndex = responseBody.indexOf("<div class=\"sporing-sendingandkolli-latestevent-date\">", startIndex);
		endIndex = responseBody.indexOf("</div>", startIndex);
	
		if (startIndex == -1){
            // no results
            newStatus = "Ugyldig sendingsnummer eller tjenestefeil";
            if (newStatus.equals(oldStatus)){
    			return null; // no changes
    		}
    		else {
    			return newStatus;
    		}
		}
		// remove all tags, whitespace - and trim
		newStatus += " " + responseBody.substring(startIndex, endIndex)
			.replaceAll("\\<.*?\\>","").replaceAll("\\s+", " ").trim();
	
		if (newStatus.equals(oldStatus)){
			return null; // no changes
			//return "TestStatus"; // Testing purposes only
		}
		else {
			return newStatus;
		}
	}
	
	public static boolean updateAllPackages(PackagesDbAdapter packagesDbAdapter){
		boolean hasChanged = false;
		packagesDbAdapter.open();
		Cursor c = packagesDbAdapter.fetchAllPackages();
		if (c.moveToFirst()){
    		do {
    			int packageid = c.getInt(c.getColumnIndex(PackagesDbAdapter.KEY_ID));
    			String packageNumber = c.getString(c.getColumnIndex(PackagesDbAdapter.KEY_NUMBER));
    			String oldStatus = c.getString(c.getColumnIndex(PackagesDbAdapter.KEY_STATUS));
    			try {
    				String newStatus = TrackingUtils.updateStatus(packageNumber, oldStatus);
    				if (newStatus != null){
    					packagesDbAdapter.updatePackage(packageid, newStatus, R.attr.changed);
    					hasChanged = true;
    				}
    			} catch (ClientProtocolException e) {
    				// No action needed
    			} catch (IOException e) {
    				// No action needed
    			}
    		} while (c.moveToNext());
    	}
    	c.close();
    	packagesDbAdapter.close();
    	return hasChanged;
	}
	
	public static void updateTrackingService(Context context, long delay, int repeaterType){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		boolean useRepeatingUpdates = prefs.getBoolean("notificationPref", true);
		if (useRepeatingUpdates){
			AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			Intent i = new Intent(context, TrackingService.class);
			PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
			mgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pi);
		}
	}
	
	public static void stopTrackingService(Context context){
		AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, TrackingService.class);
		PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
		mgr.cancel(pi);
	}
}
