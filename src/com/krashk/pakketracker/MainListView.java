package com.krashk.pakketracker;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

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
        
        Button createNew = (Button) findViewById(R.id.newpackagebutton);
        
        createNew.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(MainListView.this, NewPackageView.class);
				startActivity(i);
			}
		});

    }

    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	boolean result = super.onCreateOptionsMenu(menu);
        menu.add(0, REFRESH_ID, 0, R.string.refresh).setIcon(android.R.drawable.ic_menu_search);
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
    
    private void createNote(String packagenumber) {
        packageDbAdapter.createPackage(packagenumber);
        fillData();
    }
    
    private void fillData() {
        // Get all of the packages from the database and create the item list
        Cursor c = packageDbAdapter.fetchAllPackages();
        startManagingCursor(c);

        String[] from = new String[] { PackagesDbAdapter.KEY_STATUS, PackagesDbAdapter.KEY_NUMBER };
        int[] to = new int[] {R.id.status, R.id.tracknumber};
        
        // Now create an array adapter and set it to display using our rowlayout
        SimpleCursorAdapter packages =
            new SimpleCursorAdapter(this, R.layout.listitem, c, from, to);
        setListAdapter(packages);
    }
    
    @Override
	protected void onListItemClick (ListView l, View v, int position, long id){
//		HashMap<String,String> item = (HashMap<String, String>) getListView().getItemAtPosition(position);
//		
//		String result = item.get("filename");
//		
//		Intent resultIntent = new Intent();
//		resultIntent.putExtra(PUBLIC_STATIC_STRING_IDENTIFIER, result );
//		setResult(Activity.RESULT_OK, resultIntent);
//		finish();
	}
}

