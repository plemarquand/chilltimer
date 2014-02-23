package com.mkthings.util;

import android.graphics.Color;

public class ColorUtil {
	public static int adjustLightness(int color, float tintAmt) {
		int r = (int) (Color.red(color) * tintAmt);
		int g = (int) (Color.green(color) * tintAmt);
		int b = (int) (Color.blue(color) * tintAmt);
		return Color.rgb(r, g, b);
	}
}
