package com.krashk.pakketracker;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class NewPackageView extends Activity {

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newpackage);
        
        
        final Button button = (Button) findViewById(R.id.ok);
        button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				TextView textbox = (TextView) findViewById(R.id.entry);
				if (textbox.getText().length() > 0){
					HttpClient client = new DefaultHttpClient();
					HttpGet get = new HttpGet("http://sporing.posten.no/sporing.html?q="+textbox.getText());
					ResponseHandler<String> responseHandler = new BasicResponseHandler();
	                String responseBody = null;
	                
	        		try {
	        			responseBody = client.execute(get, responseHandler);
	        		} catch (ClientProtocolException e) {
	        			// CARE
	        		} catch (IOException e) {
	        			// CARE
	        		}
	        		responseBody = responseBody.replace("\n", "");
					// first find the event field
	        		int startIndex = responseBody.indexOf("<div class=\"sporing-sendingandkolli-latestevent-text\">");
	        		int endIndex = responseBody.indexOf("</div>", startIndex);

				    // remove all tags, whitespace - and trim
				    String newStatus = responseBody.substring(startIndex, endIndex)
				            .replaceAll("\\<.*?\\>","").replaceAll("\\s+", " ").trim();
	
				    // now find the date field
				    startIndex = responseBody.indexOf("<div class=\"sporing-sendingandkolli-latestevent-date\">", startIndex);
				    endIndex = responseBody.indexOf("</div>", startIndex);
	
				    // remove all tags, whitespace - and trim
				    newStatus += " " + responseBody.substring(startIndex, endIndex)
				            .replaceAll("\\<.*?\\>","").replaceAll("\\s+", " ").trim();
	
				    ((TextView)findViewById(R.id.output)).setText(newStatus);
				}
			}
		});
    }
}
