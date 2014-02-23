package com.mkthings.chilltimer.util;

import zh.wang.android.apis.yweathergetter4a.WeatherInfo;
import zh.wang.android.apis.yweathergetter4a.YahooWeather;
import zh.wang.android.apis.yweathergetter4a.YahooWeather.SEARCH_MODE;
import zh.wang.android.apis.yweathergetter4a.YahooWeatherInfoListener;
import android.content.Context;
import android.util.Log;

import com.mkthings.chilltimer.model.VesselType;

public class ChillDurationCalculator {

	public static int CANT_GET_LOCATION_RESPONSE = -1;
	public static int TOO_WARM_OUTSIDE_RESPONSE = -2;

	public boolean running;
	private int cachedTemperature = -1;
	private boolean cancelled;

	/**
	 * Interface that client classes should implement to receive notification
	 * that the chill time has been calculated.
	 */
	public interface OnChillDurationCalculated {
		public void chillTimeCalculated(int duration, int temperature);
	}

	public void cancel() {
		cancelled = true;
	}

	public void calculate(final Context context, final VesselType vessel, final int temperature, final int quantity, final OnChillDurationCalculated handler) {
		running = true;
		if (vessel == VesselType.OUTSIDE && cachedTemperature == -1) {
			cancelled = false;
			YahooWeather weatherAPI = YahooWeather.getInstance();
			weatherAPI.setSearchMode(SEARCH_MODE.GPS);
			weatherAPI.queryYahooWeatherByGPS(context, new YahooWeatherInfoListener() {

				@Override
				public void gotWeatherInfo(WeatherInfo weatherInfo) {
					running = false;

					if (cancelled) {
						cancelled = false;
						return;
					}

					// if not running, we've been cancelled.
					if (weatherInfo == null) {
						Log.w("ChillDurationCalculator", "Unable to calculate location");
						handler.chillTimeCalculated(CANT_GET_LOCATION_RESPONSE, 0);
						return;
					}

					cachedTemperature = weatherInfo.getCurrentTempC();
					vessel.setAmbientTemperature(cachedTemperature);

					if (cachedTemperature > temperature) {
						handler.chillTimeCalculated(TOO_WARM_OUTSIDE_RESPONSE, cachedTemperature);
					} else {
						handler.chillTimeCalculated(millisUntilChilled(vessel, temperature, quantity), cachedTemperature);
					}
				}
			});
		} else if (vessel == VesselType.OUTSIDE && cachedTemperature != -1) {
			running = false;
			if (cachedTemperature > temperature) {
				handler.chillTimeCalculated(TOO_WARM_OUTSIDE_RESPONSE, cachedTemperature);
			} else {
				vessel.setAmbientTemperature(cachedTemperature);
				handler.chillTimeCalculated(millisUntilChilled(vessel, temperature, quantity), cachedTemperature);
			}
		} else {
			cancelled = true;
			running = false;
			handler.chillTimeCalculated(millisUntilChilled(vessel, temperature, quantity), temperature);
		}
	}

	/**
	 * Calculates the time it takes to cool {@value quantity} number of beers to
	 * the supplied {@value temperature} using the supplied {@value vessel}.
	 * 
	 * Uses Newton's law of cooling (T2 = T0 + (T1 - T0) * e ^ (-k * deltaT)).
	 * 
	 * @param vessel
	 * @param temperature
	 *            in Celsius
	 * @param quantity
	 * @return The number of minutes until the beverages are cooled.
	 */
	public static int minutesUntilChilled(VesselType vessel, double temperature, int quantity) {
		// Starting temperature a t=0 is room temperature, 21¡ celsius.
		double T0 = 21;

		// Ambient temperature in the cooling vessel
		double Ta = vessel.getAmbientTemperature();

		// because exponential decay approaches infinity as the target temperature
		// approaches the ambient temperature, fudge the numbers a bit so results
		// produced aren't crazy long when Ta and Tt are close.
		double threshold = 0.15;
		temperature = temperature < Ta + threshold ? Ta + threshold : temperature;

		// Cooling constant (k)
		double k = vessel.getK();

		double result = Math.log((temperature - Ta) / (T0 - Ta)) / k;
		result *= 1 + (vessel.getQuantityScaleFactor() * quantity);
		return (int) Math.round(result);
	}

	/**
	 * Same as minutesUntilChilled, but returns milliseconds instead.
	 * 
	 * @param vessel
	 * @param temperature
	 * @param quantity
	 * @return The number of milliseconds until the beverages are cooled.
	 */
	public static int millisUntilChilled(VesselType vessel, int temperature, int quantity) {
		return minutesUntilChilled(vessel, temperature, quantity) * 60 * 1000;
	}

}
