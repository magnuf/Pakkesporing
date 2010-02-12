package com.krashk.pakketracker;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.widget.SimpleCursorAdapter;

public class Mainlist extends ListActivity {
	
	public static final int INSERT_ID = Menu.FIRST;
     private PackagesDbAdapter packageDbAdapter;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listpackages);
        packageDbAdapter = new PackagesDbAdapter(this);
        packageDbAdapter.open();
        fillData();

    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	boolean result = super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID, 0, R.string.menu_insert);
        return result;
    }

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
        SimpleCursorAdapter notes =
            new SimpleCursorAdapter(this, R.layout.listitem, c, from, to);
        setListAdapter(notes);
    }
}

