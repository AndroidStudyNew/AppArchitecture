package com.sjtu.util;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.sjtu.MainApplication;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;

/**
 * 文件操作相关类
 *
 * Created by CharlesZhu on 2016/4/14.
 */
public class FileUtils {

    private static final String TAG = "FileUtils";

    /**
     * 保存临时文件的目录
     */
    public static String DIR_TMP;
    /**
     * 保存临时文件1的目录
     */
    private static String DIR_TMP1;
    /**
     * 保存临时文件2的目录
     */
    private static String DIR_TMP2;

    /**
     * 保存某数据的具体文件名
     */
    public static final String FILE_TMP_DATA = "FILE_TMP_DATA.data";

    /**
     * 保存某数据的具体文件名
     */
    public static final String FILE_TMP1_DATA = "FILE_TMP1_DATA.data";

    /**
     * 保存某数据的具体文件名
     */
    public static final String FILE_TMP2_DATA = "FILE_TMP2_DATA.data";

    /**
     * 将下面需要userid标识的文件夹放在这里面
     */
    public enum FILE_TYPE_ENUM {
        DIR_TMP,//临时文件
        DIR_TMP1,//临时文件1
        DIR_TMP2//临时文件2
    }


    public static void initDir(Context context) {
        if (!Util.hasEnoughSpace()) {
            return;
        }
        DIR_TMP = getExternalFilesDir(context, "tmp").getAbsolutePath() + "/";
        DIR_TMP1 = getExternalFilesDir(context, "tmp1").getAbsolutePath() + "/";
        DIR_TMP2 = getExternalFilesDir(context, "tmp2").getAbsolutePath() + "/";
    }

    public static String getSpecifyFileDirectory(FILE_TYPE_ENUM file_type_enum, Context context) {
        if (file_type_enum == null) {
            return null;
        }
        MainApplication mainApplication = (MainApplication) context.getApplicationContext();
        MainApplication.AccountInfo accountInfo = mainApplication.getCurrentAccount();
        if (accountInfo == null) {
            return null;
        }
        String userDirectory = accountInfo.getUser_id() + "/";
        String directory = null;
        if (file_type_enum == FILE_TYPE_ENUM.DIR_TMP) {
            directory = DIR_TMP + userDirectory;
            File file = new File(directory);
            if (!file.exists()) {
                file.mkdirs();
            }
        } else if (file_type_enum == FILE_TYPE_ENUM.DIR_TMP1) {
            directory = DIR_TMP1 + userDirectory;
            File file = new File(directory);
            if (!file.exists()) {
                file.mkdirs();
            }
        } else if (file_type_enum == FILE_TYPE_ENUM.DIR_TMP2) {
            directory = DIR_TMP2 + userDirectory;
            File file = new File(directory);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        return directory;
    }

    private static File getExternalFilesDir(Context context, String dir) {
        File appFiles = context.getExternalFilesDir(dir);
        if (appFiles == null) {
            String packageName = context.getPackageName();
            File externalPath = Environment.getExternalStorageDirectory();
            appFiles = new File(externalPath.getAbsolutePath() + "/Android/data/" + packageName + "/files/" + dir);
            if (!appFiles.exists()) {
                appFiles.mkdirs();
            }
        }
        return appFiles;
    }

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

    /**
     * 保存文件
     *
     * @param data
     * @param file
     */
    public static void saveFile(String data, String file, boolean append) {
        if (!TextUtils.isEmpty(data) && !TextUtils.isEmpty(file)) {
            try {
                File f = new File(file);
                if (!f.exists()) {
                    f.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(file, append);
                BufferedWriter output = new BufferedWriter(new OutputStreamWriter(fos));

                output.write(data);
                output.flush();
                output.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 复制文件
     * @param src 源文件路径
     * @param dst 目标文件路径
     * @return
     */
    public static boolean copyFile(final String src, final String dst) {
        try {
            copyFile(new File(src), new File(dst));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 复制文件
     * @param src 源文件
     * @param dst 目标文件
     * @return
     */
    public static void copyFile(File src, File dst) throws IOException {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }


}
