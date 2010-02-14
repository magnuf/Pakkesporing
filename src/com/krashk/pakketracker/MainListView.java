package com.krashk.pakketracker;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import android.app.AlarmManager;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
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

        
        AlarmManager mgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(this, TrackingService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        
        mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 30000, pi);
        
    }


    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	boolean result = super.onCreateOptionsMenu(menu);
        menu.add(0, REFRESH_ID, 0, R.string.refresh).setIcon(android.R.drawable.ic_popup_sync);
        menu.add(0, SETTINGS_ID, 0, R.string.settings).setIcon(android.R.drawable.ic_menu_preferences);
        return result;
    }
    
    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
        case INSERT_ID:
            createNote();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    */
    
    
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
    	Cursor items = (Cursor)getListView().getItemAtPosition(position);
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
			Toast.makeText(((View) v.getParent()).getContext(), "Feil i HTTP-protokollen", Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			Toast.makeText(((View) v.getParent()).getContext(), "Feil ved tilkobling til nettverk/internett", Toast.LENGTH_SHORT).show();
		}
    	
    }
}

