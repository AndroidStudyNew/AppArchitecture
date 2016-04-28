package com.sjtu.util;

import android.content.Context;

import com.sjtu.MainApplication;

/**
 * Created by lenovo on 2016/4/28.
 */
public class PreferenceHelper {

    public static String getSpecificPreferenceKey(Context context, String prefix) {
        MainApplication mainApplication = (MainApplication) context.getApplicationContext();
        MainApplication.AccountInfo accountInfo = mainApplication.getCurrentAccount();
        if (accountInfo == null) {
            return prefix;
        }
        String suffix = "_" + accountInfo.getUser_id();
        return prefix + suffix;
    }

}
