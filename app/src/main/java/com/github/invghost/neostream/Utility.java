package com.github.invghost.neostream;

import android.content.Context;
import android.os.Build;

class Utility {
    static int getColor(Context context, int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getResources().getColor(id, context.getTheme());
        } else {
            return context.getResources().getColor(id);
        }
    }
}
