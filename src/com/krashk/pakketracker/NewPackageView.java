package com.krashk.pakketracker;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.content.Intent;
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
				String packagenumber = textbox.getText().toString();
				if (packagenumber.length() == 0){
					Toast.makeText(((View) arg0.getParent()).getContext(), R.string.empty, Toast.LENGTH_SHORT).show();
				}
				else if (packagenumber.length() > 0){

					PackagesDbAdapter packageDbAdapter = new PackagesDbAdapter(((View) arg0.getParent()).getContext());

					packageDbAdapter.open();
					Long packageid = packageDbAdapter.createPackage(packagenumber);
					try {
						String newStatus = TrackingUtils.updateStatus(packagenumber, new String());
						if (newStatus != null){
							packageDbAdapter.updatePackage(packageid, packagenumber, newStatus);
						}
					} catch (ClientProtocolException e) {
						Toast.makeText(((View) arg0.getParent()).getContext(), "Feil i HTTP-protokollen", Toast.LENGTH_SHORT).show();
					} catch (IOException e) {
						Toast.makeText(((View) arg0.getParent()).getContext(), "Feil ved tilkobling til nettverk/internett", Toast.LENGTH_SHORT).show();
					}
					packageDbAdapter.close();
					Intent i = new Intent(NewPackageView.this, MainListView.class);
					startActivity(i);

					Intent i = new Intent(NewPackageView.this, MainListView.class);
					startActivity(i);

				}
			}
		});
	}
}
