package com.krashk.pakketracker;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
import android.text.format.DateUtils;
import android.text.format.Time;

public class TrackingUtils {


	private static SimpleDateFormat tidsFormat = new SimpleDateFormat("HH:mm");
	
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
	
	public static int getNumPackages(PackagesDbAdapter packagesDbAdapter){
		return packagesDbAdapter.getNumPackages();
	}
	
	public static void updateTrackingService(Context context){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		int intervalValPref = Integer.parseInt(prefs.getString("intervalVal", "0"));
		if (intervalValPref > 0){
			long nextUpdate;
			if(prefs.getBoolean("nightmodePref", true)){
				Time nextTime = new Time();
				Date now = new Date();
				Time startTime = new Time();
				Time stopTime = new Time();
				
				Date startDate; 
				Date stopDate;
				try {
					startDate = tidsFormat.parse(prefs.getString("updateStart", "08:00"));
					stopDate = tidsFormat.parse(prefs.getString("updateStop", "22:00"));
				} catch (ParseException e) {
					throw new RuntimeException("Feil ved parsing av dato-preferanse");
				}
				startTime.set(0, startDate.getMinutes(), startDate.getHours(), now.getDate(), now.getMonth(), now.getYear());
				stopTime.set(0, stopDate.getMinutes(), stopDate.getHours(), now.getDate(), now.getMonth(), now.getYear());
				nextTime.set(System.currentTimeMillis() + intervalValPref * DateUtils.MINUTE_IN_MILLIS);
				// stop- og startTime er nå innenfor samme døgn
				

				boolean nightUpdate = stopTime.before(startTime);
				if (startTime.after(nextTime) && stopTime.after(nextTime)){
					if (!nightUpdate){
						// next -> start ->  stopp , sett next til start 
						nextTime.set(startTime.toMillis(false));
					}
					else {
						// next -> stop -> start -> gyldig
					}
				}
				else {
					while (startTime.before(nextTime) && stopTime.before(nextTime)){ // loope til nextTime er mellom grensene
						if (!nightUpdate){
							// start -> stop -> next, sett start en dag frem
							startTime.set(startTime.toMillis(false) + DateUtils.DAY_IN_MILLIS);
						}
						else { // stop -> start -> next, set stop en dag frem
							stopTime.set(stopTime.toMillis(false) + DateUtils.DAY_IN_MILLIS);
						}
						nightUpdate = !nightUpdate; //Added a day to the other, they are now changing place
					}
					// nextTime er nå mellom start og stop, og hvilken som er minst er nå gitt ved "smallest"
					
					if (!nightUpdate){
						// start -> next -> stop, next er i gyldig intervall
					}
					else {
						// stop -> next -> start, next er utenfor gyldig intervall, sett next til start
						nextTime.set(startTime.toMillis(false));
					}
				}
				nextUpdate = nextTime.toMillis(false);
			
			}else{
				nextUpdate = System.currentTimeMillis() + intervalValPref * DateUtils.MINUTE_IN_MILLIS;
			}
			
			Date logdate = new Date(nextUpdate);
			FileOutputStream fos = null;
			try {
				fos = context.openFileOutput("timelog.txt", Context.MODE_APPEND);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			try {
				osw.write(logdate.getHours() + ":" + logdate.getMinutes());
				osw.flush();
				osw.close();
				fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
			AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			Intent i = new Intent(context, TrackingService.class);
			PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
			mgr.set(AlarmManager.RTC_WAKEUP, nextUpdate, pi);
		}
	}
	
	public static void stopTrackingService(Context context){
		AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, TrackingService.class);
		PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
		mgr.cancel(pi);
	}
}
