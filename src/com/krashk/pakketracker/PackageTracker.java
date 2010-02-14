package com.krashk.pakketracker;

import android.app.Application;

public class PackageTracker extends Application {

	private boolean appRunning;

	public void setAppRunning(boolean appRunning) {
		this.appRunning = appRunning;
	}

	public boolean isAppRunning() {
		return appRunning;
	}
	
	
}
