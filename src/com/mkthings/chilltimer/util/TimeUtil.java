package com.mkthings.chilltimer.util;

import android.annotation.SuppressLint;
import android.content.Context;

import com.mkthings.chilltimer.R;

public class TimeUtil {
	private static final int MILLIS_IN_SECOND = 1000;
	private static final int MILLIS_IN_MINUTE = MILLIS_IN_SECOND * 60;
	private static final int MILLIS_IN_HOUR = MILLIS_IN_MINUTE * 60;

	private int mHours = 0;
	private int mMinutes = 0;
	private int mSeconds = 0;

	public TimeUtil(long date) {

		mHours = (int) Math.floor(date / MILLIS_IN_HOUR);

		date -= mHours * MILLIS_IN_HOUR;
		mMinutes = (int) Math.floor(date / MILLIS_IN_MINUTE);

		date -= mMinutes * MILLIS_IN_MINUTE;
		mSeconds = (int) Math.floor(date / MILLIS_IN_SECOND);
	}

	public String getHours() {
		return mHours > 0 ? mHours + "" : "";
	}

	public String getMinutes() {
		return mMinutes > 0 ? (mHours > 0 ? padZeros(mMinutes) : mMinutes) + "" : "";
	}

	public String getSeconds() {
		return padZeros(mSeconds);
	}

	/**
	 * Creates a time string from the supplied date in the format "00h 00m" or
	 * "00 min" if there are no hours.
	 * 
	 * @param date
	 * @param context
	 * @return
	 */
	public static String createTimerText(long date, Context context) {

		int hours = (int) Math.floor(date / MILLIS_IN_HOUR);
		int minutes;

		StringBuilder builder = new StringBuilder();
		if (hours > 0) {
			builder.append(padZerosIfZero(hours));
			builder.append("h ");

			date -= hours * MILLIS_IN_HOUR;
			minutes = (int) Math.floor(date / MILLIS_IN_MINUTE);

			builder.append(padZeros(minutes));
			builder.append("m");
		} else {
			minutes = (int) Math.floor(date / MILLIS_IN_MINUTE);
			builder.append(String.format(context.getString(R.string.minute_abbv), minutes));
		}

		return builder.toString();
	}

	@SuppressLint("DefaultLocale")
	private static String padZeros(int val) {
		return String.format("%02d", val);
	}

	private static String padZerosIfZero(int val) {
		return val == 0 ? padZeros(val) : "" + val;
	}
}
