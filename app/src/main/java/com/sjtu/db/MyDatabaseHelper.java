package com.sjtu.db;

import android.content.Context;

import com.sjtu.db.TableContracts.Accounts;
import com.sjtu.db.TableContracts.Areas;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

/**
 * Created by CharlesZhu on 2016/4/14.
 */
public class MyDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "cc4sf.db";
    private static final String DATABASE_PWD = "cc4sf.db";

    private static final int DATABASE_VERSION1 = 1;

    private static final int DATABASE_VERSION = DATABASE_VERSION1;

    private Context mContext;

    private static MyDatabaseHelper mInstance = null;

    public MyDatabaseHelper(Context context   ) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION, DATABASE_PWD);
        this.mContext = context;
    }

    public static synchronized MyDatabaseHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MyDatabaseHelper(context);
        }
        return mInstance;
    }

    /**
     * 需要在调用此方法加密数据库（建议在Application中加载）
     *
     * @param context
     */
    public static void Init(Context context) {
        InitializeDatabaseWithPasscode(context, DATABASE_NAME, DATABASE_PWD);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //创建accounts表
        db.execSQL("CREATE TABLE " + Accounts.TABLE_NAME + " (" + Accounts._ID + " INTEGER PRIMARY KEY,"
                + Accounts.USER_ID + " TEXT,"
                + Accounts.NAME + " TEXT,"
                + Accounts.ACCOUNT_STATE + " INTEGER DEFAULT 0"
                + ");");
        //创建areas表
        db.execSQL("CREATE TABLE " + Areas.TABLE_NAME + " (" + Areas._ID + " INTEGER PRIMARY KEY,"
                + Areas.ACCOUNT_ID + " INTEGER REFERENCES accounts(_id) DEFAULT -1,"
                + Areas.CODE + " TEXT,"
                + Areas.NAME + " TEXT,"
                + Areas.LEVEL + " INTEGER DEFAULT -1"
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
