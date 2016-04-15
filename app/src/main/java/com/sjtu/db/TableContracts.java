package com.sjtu.db;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by CharlesZhu on 2016/4/14.
 */
public class TableContracts {

    public static String AUTHORITY = "com.sjtu.ocr.post.provider";

    //账户表
    public static final class Accounts implements BaseColumns {
        public static final String TABLE_NAME = "accounts";
        public static final String TABLE_PATH = "accounts";
        public static final String TABLE_PATH_WITH_PARAM = "accounts/#";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_PATH);

        //表字段
        public static final String USER_ID = "user_id";
        public static final String NAME = "name";//用户姓名
        public static final String ACCOUNT_STATE = "account_state";//当前账户状态

        public static final int STATE_ACTIVE = 1;//账户为登录状态
        public static final int STATE_LOGOUT = 2;//账户为登出状态

    }

    public static final class Areas implements BaseColumns {
        public static final String TABLE_NAME = "areas";
        public static final String TABLE_PATH = "areas";
        public static final String TABLE_PATH_WITH_PARAM = "areas/#";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_PATH);

        //表字段
        public static final String ACCOUNT_ID = "account_id";//标识属于哪个账户下的
        public static final String CODE = "code";
        public static final String NAME = "name";
        public static final String LEVEL = "level";
    }

}
