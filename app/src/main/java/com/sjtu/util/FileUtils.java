package com.sjtu.util;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;

/**
 * Created by CharlesZhu on 2016/4/14.
 */
public class FileUtils {

    private static final String TAG = "FileUtils";

    /**
     * 判断文件是否存在
     * @param path
     * @return
     */
    public static boolean isFileExisting(String path) {
        boolean result = false;
        if (!TextUtils.isEmpty(path)) {
            File f = new File(path);
            result = f.exists();
        }
        if (!result) {
            Log.i(TAG, "file is not exists " + path);
        }
        return result;
    }
}
