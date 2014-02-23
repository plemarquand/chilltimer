package com.mkthings.ui.control;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.SeekBar;

import com.mkthings.chilltimer.R;

public class DiscreteSeekBar extends SeekBar {

	private static final int DEFAULT_TICK_COLOR = Color.parseColor("#FF444444");
	private static final int DEFAULT_INVALID_RANGE_COLOR = Color.parseColor("#88AE233A");
	private static final int DEFAULT_TICK_COUNT = 4;
	private static final int DEFAULT_TICK_SIZE = 15;
	private static final int DEFAULT_TICK_TICKNESS = 2;

	private Paint mTickPaint;
	private int mTickCount;
	private int mTickInterval;
	private float mCenterY;
	private float mTickSize;
	private int mMaxValid;
	private Paint mMaxValidPaint;
	private float[] mTickPositions;
	private int mTickWidthArea;

	public DiscreteSeekBar(Context context) {
		super(context);
	}

	public DiscreteSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray arr = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DiscreteSeekBar, 0, 0);

		int tickColor = arr.getColor(R.styleable.DiscreteSeekBar_tickColor, DEFAULT_TICK_COLOR);
		int invalidRangeColor = arr.getColor(R.styleable.DiscreteSeekBar_invalidRangeColor, DEFAULT_INVALID_RANGE_COLOR);
		int tickWidth = arr.getInt(R.styleable.DiscreteSeekBar_tickWidth, DEFAULT_TICK_TICKNESS);
		mTickSize = arr.getDimension(R.styleable.DiscreteSeekBar_tickSize, DEFAULT_TICK_SIZE);
		mTickCount = arr.getInt(R.styleable.DiscreteSeekBar_tickCount, DEFAULT_TICK_COUNT);

		mTickPaint = new Paint();
		mTickPaint.setStyle(Paint.Style.STROKE);
		mTickPaint.setColor(tickColor);
		mTickPaint.setStrokeWidth(tickWidth);

		mMaxValidPaint = new Paint();
		mMaxValidPaint.setStyle(Paint.Style.STROKE);
		mMaxValidPaint.setColor(invalidRangeColor);
		mMaxValidPaint.setStrokeWidth(tickWidth * 4);
	}

	public void setMaximumValidValue(int max) {
		if (max != mMaxValid) {
			if (max > getMax()) {
				max = getMax();
			}
			mMaxValid = max;
			invalidate();
		}
	}

	public void setTickPositions(float[] ticks) {
		mTickPositions = ticks;
	}

	@Override
	protected synchronized void onDraw(Canvas canvas) {

		if (mMaxValid > 0) {
			float perc = ((float) mMaxValid / (float) getMax());

			float w = getWidth() - getThumbOffset() * 2;
			float startX = getThumbOffset() + w * perc;
			canvas.drawLine(startX, mCenterY, getWidth() - getThumbOffset(), mCenterY, mMaxValidPaint);
		}

		if (mTickPositions != null) {
			int len = mTickPositions.length;
			for(int i = 0; i < len; i++) {
				int tickX = Math.round(mTickWidthArea * mTickPositions[i] + getThumbOffset());
				canvas.drawLine(tickX, mCenterY - mTickSize * 0.5f, tickX, mCenterY + mTickSize * 0.5f, mTickPaint);
			}
		} else {
			for (int i = 0; i < mTickCount; i++) {
				int tickX = mTickInterval * i + getThumbOffset();
				canvas.drawLine(tickX, mCenterY - mTickSize * 0.5f, tickX, mCenterY + mTickSize * 0.5f, mTickPaint);
			}

		}

		super.onDraw(canvas);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		mTickWidthArea = w - getThumbOffset() * 2;
		mTickInterval = (int) Math.round(mTickWidthArea / (mTickCount - 1));
		mCenterY = h * 0.5f;
	}
}
