package com.mkthings.chilltimer.util;

public class TemperatureUtil {

	private String unit;

	public TemperatureUtil(String unit) {
		this.unit = unit;
	}

	public int calculateTemperature(int tempC) {
		return unit.equals("F") ? celciusToFarenheit(tempC) : tempC;
	}

	public static int celciusToFarenheit(int c) {
		return Math.round(c * (9 / 5) + 32);
	}
}
