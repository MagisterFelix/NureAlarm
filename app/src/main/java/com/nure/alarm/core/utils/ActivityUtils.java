package com.nure.alarm.core.utils;

import android.app.Activity;
import android.content.Intent;

public class ActivityUtils {

    public static String CHECK_LAST_ACTIVITY = "checkLastActivity";

    public static void startActivity(Activity activity, Intent intent) {
        activity.startActivity(intent);
        activity.finish();
        activity.overridePendingTransition(0, 0);
    }
}
