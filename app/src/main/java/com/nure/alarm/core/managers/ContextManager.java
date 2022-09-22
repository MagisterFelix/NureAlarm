package com.nure.alarm.core.managers;

import android.content.Context;
import android.content.res.Configuration;

import java.util.Locale;

public class ContextManager {

    public static Context getLocaleContext(Context context) {
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(new Locale(new SessionManager(context).fetchLocale()));
        return context.createConfigurationContext(configuration);
    }
}
