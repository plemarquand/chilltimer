package com.mkthings.chilltimer.view.activity;

import java.util.Date;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.mkthings.chilltimer.R;
import com.mkthings.chilltimer.model.VesselType;
import com.mkthings.chilltimer.util.ChillDurationCalculator;
import com.mkthings.chilltimer.util.ChillDurationCalculator.OnChillDurationCalculated;
import com.mkthings.chilltimer.util.TemperatureUtil;
import com.mkthings.chilltimer.util.TimeUtil;
import com.mkthings.ui.control.DiscreteSeekBar;
import com.mkthings.util.MathUtil;

@EActivity
public class TimerSetupActivity extends Activity {

	private static int TIMER_DURATION = 1000;

	@ViewById(R.id.how_many_spinner)
	protected Spinner howManySpn;

	@ViewById(R.id.vessel_spinner)
	protected Spinner vesselSpn;

	@ViewById(R.id.temperature_description)
	protected TextView descriptionTxt;

	@ViewById(R.id.timerText)
	protected TextView timerTxt;

	@ViewById(R.id.timeText)
	protected TextView timeTxt;

	@ViewById(R.id.min_temperature)
	protected TextView minTempTxt;

	@ViewById(R.id.max_temperature)
	protected TextView maxTempTxt;

	@ViewById(R.id.startTimerButton)
	protected Button startBtn;

	@ViewById(R.id.location_calculation_loading_container)
	protected ViewGroup loadingCtr;

	@ViewById(R.id.how_many_container)
	protected ViewGroup howManyCtr;

	@ViewById(R.id.temperature_seek)
	protected DiscreteSeekBar temperatureSeekBar;

	@ViewById(R.id.timer_container)
	protected View timerCtr;

	private boolean mConfiguring;
	private OnSharedPreferenceChangeListener listener;
	private int calculatedDurationMillis = -1;
	private ChillDurationCalculator calculator = new ChillDurationCalculator();
	private Handler timerHandler = new Handler();
	private Runnable timerRunnable = new Runnable() {
		@Override
		public void run() {
			updateTimeText();
			timerHandler.postDelayed(this, TIMER_DURATION);
		}
	};

	private OnItemSelectedListener spinnerUpdateHandler = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int index, long arg3) {
			savePreferences();
			updateUI();
			calculateTime();
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};

	private OnSeekBarChangeListener seekBarChangeHandler = new OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			savePreferences();
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			calculateTime();
		}
	};


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timer_setup);
		configure();
	}

	protected void configure() {
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		populateData();
		configureListeners();
		configureDefaults();
		calculateTime();
		updateUI();
	}

	private void configureDefaults() {
		SharedPreferences preferences = getPreferences(MODE_PRIVATE);

		mConfiguring = true;
		temperatureSeekBar.setProgress(preferences.getInt("targetTemperatureIndex", 0));
		howManySpn.setSelection(preferences.getInt("quantityIndex", 1));
		vesselSpn.setSelection(preferences.getInt("vesselIndex", 0));

		mConfiguring = false;
		calculateTime();

		listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
				populateData();
			}
		};
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(listener);
	}

	private void savePreferences() {
		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt("targetTemperatureIndex", temperatureSeekBar.getProgress());
		editor.putInt("quantityIndex", howManySpn.getSelectedItemPosition());
		editor.putInt("vesselIndex", vesselSpn.getSelectedItemPosition());
		editor.commit();
	}

	private void populateData() {
		String unit = PreferenceManager.getDefaultSharedPreferences(this).getString("temperatureUnitPref", "F");
		TemperatureUtil calculator = new TemperatureUtil(unit);


		int[] descriptionThresholds = getResources().getIntArray(R.array.description_temperature_array);
		int[] temperatureData = getResources().getIntArray(R.array.target_temperature_array);
		minTempTxt.setText(getResources().getString(R.string.temperature, calculator.calculateTemperature(temperatureData[1]), unit));
		maxTempTxt.setText(getResources().getString(R.string.temperature, calculator.calculateTemperature(temperatureData[0]), unit));
		
		float[] ticks = new float[descriptionThresholds.length + 2];
		ticks[0] = 0;
		for(int i = 1; i < descriptionThresholds.length; i++) {
			ticks[i] = 1 - ((float)descriptionThresholds[i - 1] / (float)temperatureData[0]);
		}
		ticks[ticks.length - 1] = 1;
		
		temperatureSeekBar.setTickPositions(ticks);
		temperatureSeekBar.setOnSeekBarChangeListener(seekBarChangeHandler);
	}

	private void configureListeners() {
		howManySpn.setOnItemSelectedListener(spinnerUpdateHandler);
		vesselSpn.setOnItemSelectedListener(spinnerUpdateHandler);
	}

	@Click(R.id.startTimerButton)
	protected void startBtnClicked() {
		String[] quantityData = getResources().getStringArray(R.array.beverage_count_array);
		int quantity = Integer.parseInt(quantityData[(int) howManySpn.getSelectedItemId()]);

		// May return null if a EasyTracker has not yet been initialized
		// with a property ID.
		EasyTracker easyTracker = EasyTracker.getInstance(this);

		// MapBuilder.createEvent().build() returns a Map of event
		// fields and values that are set and sent with the hit.
		easyTracker.send(MapBuilder.createEvent("ui_action", "start_timer", "quantity", (long) quantity).build());
		easyTracker.send(MapBuilder.createEvent("ui_action", "start_timer", "vessel", (long) vesselSpn.getSelectedItemPosition()).build());
		easyTracker.send(MapBuilder.createEvent("ui_action", "start_timer", "temperature", (long) getDesiredTemperature()).build());

		TimerActivity_.intent(this).duration(calculatedDurationMillis).quantity(quantity).start();
	}

	private int getDesiredTemperature() {
		int[] temperatureData = getResources().getIntArray(R.array.target_temperature_array);
		return (int) MathUtil.map(temperatureSeekBar.getProgress(), 0, temperatureSeekBar.getMax(), temperatureData[0], temperatureData[1]);
	}

	private int getMaximumTempPercent(float ambientTemperature) {
		int[] temperatureData = getResources().getIntArray(R.array.target_temperature_array);
		return (int) MathUtil.map(ambientTemperature, temperatureData[0], temperatureData[1], 0, temperatureSeekBar.getMax());
	}

	private String getDescriptionText(int temp) {
		String[] strs = getResources().getStringArray(R.array.temperature_description_array);
		int[] arr = getResources().getIntArray(R.array.description_temperature_array);
		for (int i = 0; i < arr.length; i++) {
			if (temp >= arr[i]) {
				return strs[i];
			}
		}
		return "";
	}

	private VesselType getVessel() {
		return VesselType.values()[(int) vesselSpn.getSelectedItemId()];
	}

	private int getQuantity() {
		String[] quantityData = getResources().getStringArray(R.array.beverage_count_array);
		return Integer.parseInt(quantityData[(int) howManySpn.getSelectedItemId()]);
	}

	private void calculateTime() {
		if (calculator.running || mConfiguring) {
			return;
		}

		final int desiredTemperature = getDesiredTemperature();
		final int quantity = getQuantity();
		final VesselType vessel = getVessel();

		descriptionTxt.setText(getDescriptionText(desiredTemperature));
		loadingCtr.setVisibility(View.VISIBLE);
		timerCtr.setVisibility(View.GONE);
		
		//TODO: Why does it flicker when you go full on the bar with Outside mode?

		calculator.cancel();
		calculator.calculate(this, vessel, desiredTemperature, quantity, new OnChillDurationCalculated() {

			@Override
			public void chillTimeCalculated(int duration, int temperature) {
				calculatedDurationMillis = duration;

				loadingCtr.setVisibility(View.GONE);
				timerCtr.setVisibility(View.VISIBLE);

				int maxValid = getMaximumTempPercent((float) vessel.getAmbientTemperature());
				temperatureSeekBar.setMaximumValidValue(maxValid);

				if (duration == ChillDurationCalculator.CANT_GET_LOCATION_RESPONSE) {
					timerTxt.setVisibility(View.GONE);
					timeTxt.setText(getString(R.string.location_not_found));
				} else if (vessel.getAmbientTemperature() >= desiredTemperature) {
					temperatureSeekBar.setProgress(maxValid);
				} else {
					timerTxt.setVisibility(View.VISIBLE);
					updateTimeText();
				}
			}
		});
	}

	private void updateUI() {
		howManyCtr.setVisibility((getVessel().getQuantityScaleFactor() == 0) ? View.GONE : View.VISIBLE);
	}

	private void updateTimeText() {
		if (calculatedDurationMillis > 0) {

			timerTxt.setText(TimeUtil.createTimerText(calculatedDurationMillis, this));

			Date date = new Date();
			date.setTime(date.getTime() + calculatedDurationMillis);
			timeTxt.setText("(" + DateFormat.getTimeFormat(this).format(date).toString() + ")");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.timer_setup, menu);
		return true;
	}

	// This method is called once the menu is selected
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent i = new Intent(this, SettingsActivity.class);
			startActivity(i);
			break;
		}
		return true;
	}

	@Override
	protected void onStart() {
		EasyTracker.getInstance(this).activityStart(this);
		timerHandler.postDelayed(timerRunnable, TIMER_DURATION);
		super.onStart();
	}

	@Override
	protected void onRestart() {
		timerHandler.postDelayed(timerRunnable, TIMER_DURATION);
		super.onRestart();
	}

	@Override
	protected void onPause() {
		timerHandler.removeCallbacks(timerRunnable);
		super.onPause();
	}

	@Override
	protected void onStop() {
		EasyTracker.getInstance(this).activityStop(this);
		timerHandler.removeCallbacks(timerRunnable);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		timerHandler.removeCallbacks(timerRunnable);
		stopService(new Intent(this, com.mkthings.chilltimer.service.CountdownService_.class));
		super.onDestroy();
	}
}
