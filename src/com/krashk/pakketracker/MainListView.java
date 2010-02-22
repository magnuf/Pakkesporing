package com.krashk.pakketracker;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;

public class MainListView extends ListActivity {

	public static final int UPDATE_DIALOG = 1;
	public static final int DELETE_ID = 2;
	public static final int REFRESH_ID = Menu.FIRST;
	public static final int SETTINGS_ID = Menu.FIRST +1;
	private PackagesDbAdapter packageDbAdapter;

	private int toBeDeletedPosition;
	
	/**	 Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		packageDbAdapter = new PackagesDbAdapter(this);
		
//		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
//		SharedPreferences.Editor editor = settings.edit();
//		editor.putString("mordi", "Er mann");
//		editor.commit();
		
		
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean update = prefs.getBoolean("updatePref", false);
		if (update){
			TrackingUtils.updateAllPackages(packageDbAdapter);
		}
		
		// Hack for å komme seg rundt at vi ikke vet om appen kjører
		PackageTracker pt = ((PackageTracker)getApplicationContext());
		pt.setAppRunning(true);

		setContentView(R.layout.listpackages);
		
		Button createNew = (Button) findViewById(R.id.newpackagebutton);
		createNew.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(MainListView.this, NewPackageView.class);
				startActivity(i);
			}
		});

		getListView().setOnItemLongClickListener( new OnItemLongClickListener (){
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				toBeDeletedPosition = position;
				showDialog(DELETE_ID);
				return true;
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
		fillData();
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		TrackingUtils.updateTrackingService(this);
		PackageTracker pt = ((PackageTracker)getApplicationContext());
		pt.setAppRunning(false);
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
		case REFRESH_ID:
			TrackingUtils.updateAllPackages(packageDbAdapter);
			fillData();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id){		
		case DELETE_ID:
			AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
			builder2.setMessage("Vil du slette denne pakken?")
				.setCancelable(true)
				.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int arg1){
						MainListView.this.packageDbAdapter.open();
						Cursor items = (Cursor)getListView().getItemAtPosition(MainListView.this.toBeDeletedPosition);
						if (items == null){
							Toast.makeText(MainListView.this, "Databasefeil", Toast.LENGTH_SHORT).show();
							packageDbAdapter.close();
							return;
						}
						int packageid = items.getInt(items.getColumnIndex(PackagesDbAdapter.KEY_ID));
						MainListView.this.packageDbAdapter.deletePackage(packageid);
						fillData();
						packageDbAdapter.close();
						dialog.cancel();
					}
				})
				.setNegativeButton("Nei", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int arg1){
						dialog.cancel();
					}
				});
				AlertDialog alert2 = builder2.create();
				return alert2;
		}
		return null;
	}

	private void fillData() {
		// Get all of the packages from the database and create the item list
		packageDbAdapter.open();
		Cursor c = packageDbAdapter.fetchAllPackages();
		startManagingCursor(c);

		String[] from = new String[] {PackagesDbAdapter.KEY_ID, PackagesDbAdapter.KEY_NUMBER,  PackagesDbAdapter.KEY_STATUS };
		int[] to = new int[] {R.id.pkid, R.id.tracknumber, R.id.status};

		// Now create an array adapter and set it to display using our rowlayout
		SimpleCursorAdapter packages =
			new SimpleCursorAdapter(this, R.layout.listitem, c, from, to);
		setListAdapter(packages);
		packageDbAdapter.close();

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

