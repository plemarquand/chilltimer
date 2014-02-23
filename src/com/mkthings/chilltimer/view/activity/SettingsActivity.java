package com.mkthings.chilltimer.view.activity;

import android.app.Activity;
import android.os.Bundle;

import com.google.analytics.tracking.android.EasyTracker;
import com.mkthings.chilltimer.view.fragment.SettingsFragment;

public class SettingsActivity extends Activity {
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
	
	@Override
	protected void onStart() {
		EasyTracker.getInstance(this).activityStart(this);
		super.onStart();
	}
	
	@Override
	protected void onStop() {
		EasyTracker.getInstance(this).activityStop(this);
		super.onStop();
	}
}
