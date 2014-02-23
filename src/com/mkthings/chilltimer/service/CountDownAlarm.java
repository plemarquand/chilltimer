package com.mkthings.chilltimer.service;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.mkthings.chilltimer.R;

public class CountDownAlarm extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
		wl.acquire();

		final Context ctx = context;
		SoundPool soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
				AudioManager audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
				float actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
				float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
				float volume = actualVolume / maxVolume;
				soundPool.play(sampleId, volume, volume, 1, 0, 1f);
			}
		});
		soundPool.load(context, R.raw.beerpour, 1);

		int notificationTextId = intent.getIntExtra("quantity", 1) == 1 ? R.string.notification_text : R.string.notification_text_plural;

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_launcher).setContentTitle(context.getString(R.string.app_name))
				.setContentText(context.getString(notificationTextId)).setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE);

		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(0, mBuilder.build());

		EasyTracker easyTracker = EasyTracker.getInstance(context);
		easyTracker.send(MapBuilder.createEvent("ui_action", "timer_complete", "", (long) 0).build());

		wl.release();
	}

	public void startAlarm(Context context, int duration, int quantity) {
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent alarmIntent = new Intent(context, CountDownAlarm.class);
		alarmIntent.putExtra("quantity", quantity);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		// Starting with API 19 (KitKat) calls to AlarmManager.set are inexact,
		// so we need to call setExact instead.
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
			alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + duration, pendingIntent);
		} else {
			setKitKatAlarm(alarmManager, duration, pendingIntent);
		}
	}

	@TargetApi(19)
	private void setKitKatAlarm(AlarmManager alarmManager, int duration, PendingIntent pendingIntent) {
		alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + duration, pendingIntent);
	}

	public void cancelAlarm(Context context) {
		Intent intent = new Intent(context, CountDownAlarm.class);
		PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(sender);
	}

}
