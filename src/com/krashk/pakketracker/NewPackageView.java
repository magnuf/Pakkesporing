package com.krashk.pakketracker;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
				if (textbox.getText().length() == 0){
					Toast.makeText(((View) arg0.getParent()).getContext(), R.string.empty, Toast.LENGTH_SHORT);
				}
				else if (textbox.getText().length() > 0){

					PackagesDbAdapter packageDbAdapter = new PackagesDbAdapter(((View) arg0.getParent()).getContext());

					packageDbAdapter.open();
					packageDbAdapter.createPackage(textbox.getText().toString());
					packageDbAdapter.close();
					
				}
			}
		});
	}
}
