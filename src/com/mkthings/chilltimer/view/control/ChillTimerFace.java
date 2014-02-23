package com.mkthings.chilltimer.view.control;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.mkthings.chilltimer.R;
import com.mkthings.util.ColorUtil;

public class ChillTimerFace extends View {

	private static final int DEFAULT_OUTER_THICKNESS_DP = 5;
	private static final int DEFAULT_INNER_THICKNESS_DP = DEFAULT_OUTER_THICKNESS_DP * 5;

	private static final int DEFAULT_OUTER_ALPHA = 255;
	private static final int DEFAULT_INNER_ALPHA = 20;

	private final static float GRADIENT_AMT = 0.75f;

	private int mFaceColor;
	private int mBackgroundColor;

	private float mOuterRingThickness;
	private float mInnerRingThickness;
	private int mOuterRingAlpha;
	private int mInnerRingAlpha;

	private float mPercent;
	private float mDiameter;
	private RectF mDrawRect;

	private Paint mFacePaint;
	private Paint mBackgroundPaint;
	private Paint mMaskPaint;
	private float mAngle;
	private float mRadius;

	public ChillTimerFace(Context context) {
		super(context);
	}

	public ChillTimerFace(Context context, AttributeSet attrs) {
		super(context, attrs);

		// Clipping not hardware accelerated in SDK < 18
		if (Build.VERSION.SDK_INT >= 18) {
			setLayerType(View.LAYER_TYPE_HARDWARE, null);
		} else {
			setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}

		TypedArray arr = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ChillTimerFace, 0, 0);

		try {
			mFaceColor = arr.getColor(R.styleable.ChillTimerFace_faceColor, Color.parseColor("#33B5E5"));
			mBackgroundColor = arr.getColor(R.styleable.ChillTimerFace_backgroundColor, Color.rgb(39, 45, 51));
			mOuterRingThickness = arr.getDimension(R.styleable.ChillTimerFace_outerRingThickness, DEFAULT_OUTER_THICKNESS_DP);
			mOuterRingAlpha = arr.getInt(R.styleable.ChillTimerFace_outerRingAlpha, DEFAULT_OUTER_ALPHA);
			mInnerRingThickness = arr.getDimension(R.styleable.ChillTimerFace_innerRingThickness, DEFAULT_INNER_THICKNESS_DP);
			mInnerRingAlpha = arr.getInt(R.styleable.ChillTimerFace_innerRingAlpha, DEFAULT_INNER_ALPHA);
			mPercent = arr.getFloat(R.styleable.ChillTimerFace_percent, 0);

			mFacePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mFacePaint.setStyle(Paint.Style.FILL);
			mFacePaint.setColor(mFaceColor);

			mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mBackgroundPaint.setStyle(Paint.Style.FILL);
			mBackgroundPaint.setColor(mBackgroundColor);

			mMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mMaskPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
			mMaskPaint.setColor(Color.TRANSPARENT);
		} finally {
			arr.recycle();
		}
	}

	public void setPercent(float perc) {
		if (perc != mPercent) {
			mPercent = perc;
			mAngle = mPercent * 360;
			invalidate();
		}
	}

	public float getPercent() {
		return mPercent;
	}

	public float getInnerRingThickness() {
		return mInnerRingThickness;
	}

	public void setInnerRingThickness(float innerThickness) {
		if (mInnerRingThickness != innerThickness) {
			mInnerRingThickness = innerThickness;
			invalidate();
		}
	}

	public float getDiameter() {
		return mDiameter;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		canvas.drawCircle(mDrawRect.left + mRadius, mDrawRect.top + mRadius, mRadius, mBackgroundPaint);
		mFacePaint.setAlpha(mOuterRingAlpha);
		canvas.drawArc(mDrawRect, -90, mAngle, true, mFacePaint);

		canvas.drawCircle(mRadius, mRadius, mRadius - mOuterRingThickness, mMaskPaint);

		mFacePaint.setAlpha(mInnerRingAlpha);
		canvas.drawArc(mDrawRect, -90, mAngle, true, mFacePaint);
		canvas.drawCircle(mDrawRect.left + mRadius, mDrawRect.top + mRadius, mRadius - mOuterRingThickness - mInnerRingThickness, mMaskPaint);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		float xpad = (float) (getPaddingLeft() + getPaddingRight());
		float ypad = (float) (getPaddingTop() + getPaddingBottom());

		float ww = (float) w - xpad;
		float hh = (float) h - ypad;

		mDiameter = Math.min(ww, hh);
		mRadius = mDiameter * 0.5f;
		mDrawRect = new RectF((mDiameter - ww) * 0.5f, (mDiameter - hh) * 0.5f, mDiameter, mDiameter);
		mBackgroundPaint.setShader(new LinearGradient(0, 0, 0, hh, ColorUtil.adjustLightness(mBackgroundColor, GRADIENT_AMT), mBackgroundColor, Shader.TileMode.MIRROR));

		super.onSizeChanged(w, h, oldw, oldh);
	}
}
