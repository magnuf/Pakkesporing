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
			//return null; // no changes
			return "TestStatus"; // Testing purposes only
		}
		else {
			return newStatus;
		}
	}
	
	public static void updateTrackingService(Context context, long delay, int repeaterType){
        AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, TrackingService.class);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
        mgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pi);
	}
}
