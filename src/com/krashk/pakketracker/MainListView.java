package com.krashk.pakketracker;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import android.app.AlarmManager;
import android.app.ListActivity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.CheckBoxPreference;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class MainListView extends ListActivity {
	
	public static final int REFRESH_ID = Menu.FIRST;
	public static final int SETTINGS_ID = Menu.FIRST +1;
    private PackagesDbAdapter packageDbAdapter;
	
    /**	 Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listpackages);
        packageDbAdapter = new PackagesDbAdapter(this);
        packageDbAdapter.open();
        
        fillData();
        
        packageDbAdapter.close();
        
        Button createNew = (Button) findViewById(R.id.newpackagebutton);
        createNew.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(MainListView.this, NewPackageView.class);
				startActivity(i);
			}
		});

        // Fjerne notification om vi kom hit via den
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancel(R.attr.notification_id);
		
		TrackingUtils.stopTrackingService(this);
    }

    @Override
    public void onResume(){
    	super.onResume();
    	packageDbAdapter.open();
        
        fillData();
        
        packageDbAdapter.close();
    }
    
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	TrackingUtils.updateTrackingService(this, 30000, 0);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	boolean result = super.onCreateOptionsMenu(menu);
        menu.add(0, REFRESH_ID, 0, R.string.refresh).setIcon(android.R.drawable.ic_popup_sync);
        menu.add(0, SETTINGS_ID, 0, R.string.settings).setIcon(android.R.drawable.ic_menu_preferences);
        return result;
    }
    
    
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
        case SETTINGS_ID:
        	Intent i = new Intent(MainListView.this, PreferenceView.class);
			startActivity(i);
			break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    
    
    private void fillData() {
        // Get all of the packages from the database and create the item list
        Cursor c = packageDbAdapter.fetchAllPackages();
        startManagingCursor(c);

        String[] from = new String[] {PackagesDbAdapter.KEY_ID, PackagesDbAdapter.KEY_NUMBER,  PackagesDbAdapter.KEY_STATUS };
        int[] to = new int[] {R.id.pkid, R.id.tracknumber, R.id.status};
        
        // Now create an array adapter and set it to display using our rowlayout
        SimpleCursorAdapter packages =
            new SimpleCursorAdapter(this, R.layout.listitem, c, from, to);
        setListAdapter(packages);
        
    }
    
    @Override
	protected void onListItemClick (ListView l, View v, int position, long id){
    	packageDbAdapter.open();
    	Cursor items = (Cursor)getListView().getItemAtPosition(position);
    	if (items == null){
    		Toast.makeText(((View) l.getParent()).getContext(), "Databasefeil", Toast.LENGTH_SHORT).show();
    		packageDbAdapter.close();
    		return;
    	}
    	int packageid = items.getInt(items.getColumnIndex(PackagesDbAdapter.KEY_ID));
    	String packageNumber = items.getString(items.getColumnIndex(PackagesDbAdapter.KEY_NUMBER));
    	String oldStatus = items.getString(items.getColumnIndex(PackagesDbAdapter.KEY_STATUS));
    	try {
    		String newStatus = TrackingUtils.updateStatus(packageNumber, oldStatus);
			if (newStatus != null){
				packageDbAdapter.updatePackage(packageid, newStatus, R.attr.changed);
				Toast.makeText(((View)l.getParent()).getContext(), "Endringer i pakkestatus!", Toast.LENGTH_SHORT).show();
			}
			else {
				Toast.makeText(((View)l.getParent()).getContext(), "Ingen endringer i status", Toast.LENGTH_SHORT).show();
			}
			fillData();
		} catch (ClientProtocolException e) {
			Toast.makeText(((View) l.getParent()).getContext(), "Feil i HTTP-protokollen", Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			Toast.makeText(((View) l.getParent()).getContext(), "Feil ved tilkobling til nettverk/internett", Toast.LENGTH_SHORT).show();
		} finally {
			packageDbAdapter.close();
		}
		
    	
    }
}

