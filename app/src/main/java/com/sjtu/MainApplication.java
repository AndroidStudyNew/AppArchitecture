package com.sjtu;

import android.app.Application;
import android.content.ContentResolver;
import android.database.Cursor;

import com.sjtu.activity.R;
import com.sjtu.api.ImpBaseAPI;
import com.sjtu.base.Config;
import com.sjtu.db.MyDatabaseHelper;
import com.sjtu.db.TableContracts;
import com.sjtu.util.FileUtils;

/**
 * Created by CharlesZhu on 2016/4/14.
 */
public class MainApplication extends Application {

    Config sConfig = new Config();
    public static String Vendor_Id = null;

    @Override
    public void onCreate() {
        super.onCreate();
        MyDatabaseHelper.Init(this);
        initApiTypeVender();
        mSfapi = new ImpBaseAPI(getAPIType(), getAppId());
    }

    private void initApiTypeVender() {
        // 通过raw下config文件中api环境和vendor_id获取
        int sandbox = 3;
        try {
            sConfig.init(getResources().openRawResource(R.raw.config));
            Vendor_Id = sConfig.getVenderID();
            sandbox = sConfig.getApiType();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //  通过android内存上是否创建以下文件来区分 环境检测
        if (FileUtils.isFileExisting("/sdcard/IS_API")) {
            sandbox = 0;
        } else if (FileUtils.isFileExisting("/sdcard/IS_SANDBOX")) {
            sandbox = 1;
        } else if (FileUtils.isFileExisting("/sdcard/IS_PRERELEASE")) {
            sandbox = 3;
        }
        sAPI_TYPE = sandbox;
        if (Vendor_Id == null) {
            Vendor_Id = getString(R.string.vender);
        }
    }

    private static ImpBaseAPI mSfapi;
    public static int sAPI_TYPE;
    public static ImpBaseAPI getAPI() {
        return mSfapi;
    }

    public static int getAPIType() {
        return sAPI_TYPE;
    }

    public String getAppId() {
        return getString(R.string.key_app_id) + "@" + getString(R.string.app_version);
    }

    private static AccountInfo mAccountInfo;
    public long getCurrentAccountId() {
        getCurrentAccount();
        if (mAccountInfo != null) {
            return mAccountInfo.id;
        }
        return -1;
    }

    public AccountInfo getCurrentAccount() {
        if (mAccountInfo == null) {
            updateCurrentAccount();
        }
        return mAccountInfo;
    }

    public AccountInfo updateCurrentAccount() {
        mAccountInfo = null;
        ContentResolver resolver = getContentResolver();
        Cursor c = resolver.query(TableContracts.Accounts.CONTENT_URI, null,
                TableContracts.Accounts.ACCOUNT_STATE + "=" + TableContracts.Accounts.STATE_ACTIVE, null, null);
        if (c != null) {
            if (c.moveToNext()) {
                long id = c.getLong(c.getColumnIndex(TableContracts.Accounts._ID));
                int state = c.getInt(c.getColumnIndex(TableContracts.Accounts.ACCOUNT_STATE));
                String user_id = c.getString(c.getColumnIndex(TableContracts.Accounts.USER_ID));
                String name = c.getString(c.getColumnIndex(TableContracts.Accounts.NAME));
                mAccountInfo = new AccountInfo(id, user_id,  name, state);
            }
            c.close();
        }
        return mAccountInfo;
    }

    public static class AccountInfo {
        long id = -1;
        String user_id;
        String name;
        int state = -1;

        public AccountInfo(long id, String user_id, String name, int state) {
            this.id = id;
            this.user_id = user_id;
            this.name = name;
            this.state = state;
        }

    }
}
