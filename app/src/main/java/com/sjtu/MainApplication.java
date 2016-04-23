package com.sjtu;

import android.app.Application;
import android.content.ContentResolver;
import android.database.Cursor;

import com.sjtu.activity.R;
import com.sjtu.api.ImpBaseAPI;
import com.sjtu.api.UserInfoResult;
import com.sjtu.base.BaseException;
import com.sjtu.base.Config;
import com.sjtu.db.MyDatabaseHelper;
import com.sjtu.db.TableContracts;
import com.sjtu.util.FileUtils;

/**
 * Created by CharlesZhu on 2016/4/14.
 */
public class MainApplication extends Application {

    Config sConfig = new Config();
    private static String Vendor_Id = null;

    /**
     * 注意以下顺序不能乱
     */
    @Override
    public void onCreate() {
        super.onCreate();
        MyDatabaseHelper.Init(this);// 初始化数据库
        FileUtils.initDir(this);// 初始化文件夹
        initApiTypeVender();
        mApi = new ImpBaseAPI(getAPIType(), getAppId());
        initWork();
    }

    private void initApiTypeVender() {
        // 通过raw下config文件中api环境和vendor_id获取
        int apitype = ImpBaseAPI.OFFICIAL_ENVIRONMENT;
        try {
            sConfig.init(getResources().openRawResource(R.raw.config));
            Vendor_Id = sConfig.getVenderID();
            apitype = sConfig.getApiType();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //  通过android内存上是否创建以下文件来区分 环境检测
        if (FileUtils.isFileExisting("/sdcard/IS_API")) {
            apitype = ImpBaseAPI.OFFICIAL_ENVIRONMENT;
        } else if (FileUtils.isFileExisting("/sdcard/IS_SANDBOX")) {
            apitype = ImpBaseAPI.TEST_ENVIRONMENT;
        }
        sAPI_TYPE = apitype;
        if (Vendor_Id == null) {
            Vendor_Id = getString(R.string.vender);
        }
    }

    boolean mInitWorkFinished = false;
    private void initWork() {
        final Thread initWork = new Thread(new Runnable() {

            @Override
            public void run() {
                synchronized (MainApplication.this) {
                    if (mInitWorkFinished)
                        return;

                    if (mAccountInfo != null) {
                        //获取用户信息，保存到本地
                        try {
                            //TODO测试
                            UserInfoResult userInfoResult = mApi.getUserInfo("","");
                            FileUtils.saveFile(userInfoResult.toString(), FileUtils.getSpecifyFileDirectory(FileUtils.FILE_TYPE_ENUM.DIR_TMP, MainApplication.this) + FileUtils.FILE_TMP_DATA, false);
                        } catch (BaseException e) {
                            e.printStackTrace();
                        }
                    }
                    mInitWorkFinished = true;
                }
            }
        }, "initWork");
        initWork.start();
    }

    private static ImpBaseAPI mApi;
    public static int sAPI_TYPE;
    public static ImpBaseAPI getAPI() {
        return mApi;
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
                String token = c.getString(c.getColumnIndex(TableContracts.Accounts.ACCOUNT_TOKEN));
                mAccountInfo = new AccountInfo(id, user_id,  name, state, token);
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
        String token;

        public AccountInfo(long id, String user_id, String name, int state, String token) {
            this.id = id;
            this.user_id = user_id;
            this.name = name;
            this.state = state;
            this.token = token;
        }

        public String getUser_id() {
            return user_id;
        }

        public String getName() {
            return name;
        }

        public String getToken() {
            return token;
        }
    }
}
