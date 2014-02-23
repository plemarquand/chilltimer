package com.mkthings.chilltimer.view.activity;

import java.util.Date;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.CountDownTimer;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.mkthings.chilltimer.R;
import com.mkthings.chilltimer.util.TimeUtil;
import com.mkthings.chilltimer.view.control.ChillTimerFace;
 
@EActivity(R.layout.activity_timer)
public class TimerActivity extends Activity {

	private static final int SMOOTH_ANIM_MINUTE_CUTOFF = 15;
	private static final long ANIM_IN_DURATION = 500;
	private static final long ANIM_COMPLETE_DURATION = 250;

	@Extra
	protected int quantity;

	@Extra
	protected int duration;

	@ViewById(R.id.until_bev_chilled)
	protected TextView untilTxt;

	@ViewById(R.id.countdown_complete)
	protected TextView completeTxt;

	@ViewById(R.id.countdown_text_hour)
	protected TextView hourTxt;

	@ViewById(R.id.countdown_text_minute)
	protected TextView minuteTxt;

	@ViewById(R.id.countdown_text_second)
	protected TextView secondTxt;

	@ViewById(R.id.countdown_text_hour_label)
	protected TextView hourLabelTxt;

	@ViewById(R.id.countdown_text_minute_label)
	protected TextView minuteLabelTxt;

	@ViewById(R.id.countdown_text_second_label)
	protected TextView secondLabelTxt;

	@ViewById(R.id.timer_text_container)
	protected ViewGroup timerTextContainer;

	@ViewById(R.id.timerFace)
	protected ChillTimerFace timerFaceCtrl;

	private CountDownTimer timer;
	private long startTime;
	private boolean mPlayed;

	@AfterViews
	protected void configureViews() {
		startTime = new Date().getTime();

		Intent intent = new Intent(this, com.mkthings.chilltimer.service.CountdownService_.class);
		intent.putExtra("duration", duration);
		intent.putExtra("quantity", quantity);
		startService(intent);

		int untilId = quantity > 1 ? R.string.until_description_plural : R.string.until_description;
		untilTxt.setText(getString(untilId));
		

		long time = duration - (new Date().getTime() - startTime);
		createTimer(time);
		timer.start();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		if (hasFocus && !mPlayed) {
			mPlayed = true;
			startAnimations();
		}
	}

	private void startAnimations() {
		timerFaceCtrl.setPivotX(timerFaceCtrl.getWidth() * 0.5f);
		timerFaceCtrl.setPivotY(timerFaceCtrl.getHeight() * 0.5f);
		timerFaceCtrl.setScaleX(0);
		timerFaceCtrl.setScaleY(0);

		ObjectAnimator faceScaleXAnim = ObjectAnimator.ofFloat(timerFaceCtrl, "scaleX", 0);
		faceScaleXAnim.setFloatValues(1);
		faceScaleXAnim.setInterpolator(new DecelerateInterpolator());
		faceScaleXAnim.setDuration(ANIM_IN_DURATION);
		faceScaleXAnim.start();

		ObjectAnimator faceScaleYAnim = ObjectAnimator.ofFloat(timerFaceCtrl, "scaleY", 0);
		faceScaleYAnim.setFloatValues(1);
		faceScaleYAnim.setInterpolator(new DecelerateInterpolator());
		faceScaleYAnim.setDuration(ANIM_IN_DURATION);
		faceScaleYAnim.start();
	}

	private void createTimer(final long duration) {
		
		if (timer != null) {
			return;
		}

		long timerTickDuration = duration < (1000 * 60 * SMOOTH_ANIM_MINUTE_CUTOFF) ? 33 : 1000;
		timer = new CountDownTimer(duration, timerTickDuration) {
			@Override
			public void onFinish() {

				completeTxt.setVisibility(View.VISIBLE);
				completeTxt.setText(getString(R.string.ready));

				hourTxt.setVisibility(View.GONE);
				hourLabelTxt.setVisibility(View.GONE);
				minuteTxt.setVisibility(View.GONE);
				minuteLabelTxt.setVisibility(View.GONE);
				secondTxt.setVisibility(View.GONE);
				secondLabelTxt.setVisibility(View.GONE);

				int untilId = quantity > 1 ? R.string.enjoy_plural : R.string.enjoy;
				untilTxt.setText(getString(untilId));

				timerFaceCtrl.setPercent(1);

				ObjectAnimator thicknessAnim = ObjectAnimator.ofFloat(timerFaceCtrl, "innerRingThickness", 0);
				thicknessAnim.setFloatValues(timerFaceCtrl.getDiameter() * 0.05f);
				thicknessAnim.setInterpolator(new DecelerateInterpolator());
				thicknessAnim.setDuration(ANIM_COMPLETE_DURATION);
				thicknessAnim.start();
			}

			@Override
			public void onTick(long millisUntilFinished) {
				updateText(millisUntilFinished);
				float percentComplete = 1 - ((float) millisUntilFinished / (float) duration);
				timerFaceCtrl.setPercent(percentComplete);
				updateInnerRingThickness();
			}
		};
	}

	private void updateInnerRingThickness() {
		timerFaceCtrl.setInnerRingThickness(timerFaceCtrl.getDiameter() * 0.15f);
	}

	@Override
	protected void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
	}

	private void updateText(long time) {
		TimeUtil model = new TimeUtil(time);

		hourTxt.setText(model.getHours());
		minuteTxt.setText(model.getMinutes());
		secondTxt.setText(model.getSeconds());

		hourTxt.setVisibility(hourTxt.getText() == "" ? View.GONE : View.VISIBLE);
		hourLabelTxt.setVisibility(hourTxt.getText() == "" ? View.GONE : View.VISIBLE);
		minuteTxt.setVisibility(minuteTxt.getText() == "" ? View.GONE : View.VISIBLE);
		minuteLabelTxt.setVisibility(minuteTxt.getText() == "" ? View.GONE : View.VISIBLE);
		secondTxt.setVisibility(secondTxt.getText() == "" ? View.GONE : View.VISIBLE);
		secondLabelTxt.setVisibility(secondTxt.getText() == "" ? View.GONE : View.VISIBLE);
	}

	@Override
	protected void onStop() {
		EasyTracker.getInstance(this).activityStop(this);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		timer.cancel();
		super.onDestroy();
	}

}
