package com.github.invghost.neostream;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;

import java.text.NumberFormat;
import java.util.Locale;

class Utility {
    static int getColor(Context context, int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getResources().getColor(id, context.getTheme());
        } else {
            return context.getResources().getColor(id);
        }
    }

    static Drawable getDrawable(Context context, int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getResources().getDrawable(id, context.getTheme());
        } else {
            return context.getResources().getDrawable(id);
        }
    }

    static String formatNumber(long number) {
        NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());
        return nf.format(number);
    }
}
