package com.mkthings.chilltimer.service;

import org.androidannotations.annotations.EService;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

@EService
public class CountdownService extends Service {

	private CountDownAlarm alarm = new CountDownAlarm();

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
	}
 
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		alarm.startAlarm(this, intent.getIntExtra("duration", 0), intent.getIntExtra("quantity", 1));
		return super.onStartCommand(intent, flags, startId);
	}
}
