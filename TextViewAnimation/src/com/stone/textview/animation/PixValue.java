package com.stone.textview.animation;

import android.content.res.Resources;
import android.util.DisplayMetrics;

public enum PixValue
{
    dip
    {
        @Override
        public int valueOf(float value)
        {
            return Math.round(value * m.density);
        }
    },
    sp
    {
        @Override
        public int valueOf(float value)
        {
            return Math.round(value * m.scaledDensity);
        }
    };
    public static DisplayMetrics m = Resources.getSystem().getDisplayMetrics();

    public abstract int valueOf(float value);
}
