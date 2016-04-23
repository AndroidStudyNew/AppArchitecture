package com.sjtu.util;

import android.os.Environment;
import android.os.StatFs;

import java.io.File;

/**
 *
 * Created by CharlesZhu on 2016/4/23.
 */
public class Util {

    /**
     * if sd-card is not mounted, return false.
     *
     * @return true, if sd-card has more than 8MB. otherwise return false.
     */
    public static boolean hasEnoughSpace() {
        return getAvailableSpace() > (8f * 1024 * 1024);
    }

    /**
     * @return 0 sdcard not available, or left space in sdcard
     */
    public static long getAvailableSpace() {
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            return 0;
        }
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = null;
        try {
            stat = new StatFs(path.getPath());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return 0;
        }
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return blockSize * availableBlocks;
    }

}
