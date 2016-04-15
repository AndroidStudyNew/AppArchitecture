package com.sjtu.db;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.sjtu.MainApplication;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by CharlesZhu on 2016/4/14.
 */
public class MyContentProvider extends ContentProvider {

    private static final String TAG = "SFContentProvider";

    private static final int ACCOUNTS = 1;
    private static final int ACCOUNTS_ID = 2;
    private static final int AREAS = 3;
    private static final int AREAS_ID = 4;

    private static final UriMatcher sUriMatcher;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(TableContracts.AUTHORITY, TableContracts.Accounts.TABLE_PATH, ACCOUNTS);
        sUriMatcher.addURI(TableContracts.AUTHORITY, TableContracts.Accounts.TABLE_PATH_WITH_PARAM, ACCOUNTS_ID);
        
        sUriMatcher.addURI(TableContracts.AUTHORITY, TableContracts.Areas.TABLE_PATH, AREAS);
        sUriMatcher.addURI(TableContracts.AUTHORITY, TableContracts.Areas.TABLE_PATH_WITH_PARAM, AREAS_ID);
    }

    private static final class GetTableAndWhereOutParameter {
        public String table;
        public String where;
    }

    private static MyDatabaseHelper mOpenHelper;

    static Handler handler;
    static HandlerThread thread;
    private long mLastAccountsNotifyTime = 0;
    private long mLastAreasNotifyTime = 0;
    private static final int NOTIFY_INTERAL = 5000;

    public static final int MSG_ACCOUNT = 6;
    public static final int MSG_AREA = MSG_ACCOUNT + 1;
    
    private void notifyUri(Uri uri) {
        long thisTime = System.currentTimeMillis();
        int msg = -1;
        long lastNotifyTime = -1;
        if (uri.toString().contains(TableContracts.Areas.CONTENT_URI.toString())) {
            msg = MSG_AREA;
            lastNotifyTime = mLastAccountsNotifyTime;
        }
        if (uri.toString().contains(TableContracts.Accounts.CONTENT_URI.toString())) {
            msg = MSG_ACCOUNT;
            lastNotifyTime = mLastAreasNotifyTime;
        }
        if (msg > 0) {
            if (thisTime - lastNotifyTime < NOTIFY_INTERAL) {
                if (handler.hasMessages(msg)) {
                } else {
                    handler.sendEmptyMessageDelayed(msg, NOTIFY_INTERAL);
                }
            } else {
                if (handler.hasMessages(msg)) {
                    handler.removeMessages(msg);
                    handler.sendEmptyMessage(msg);
                } else {
                    handler.sendEmptyMessage(msg);
                }
            }
        }
    }

    private class NotifyHandler extends Handler {

        public NotifyHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            long time = System.currentTimeMillis();
            switch (what) {
                case MSG_ACCOUNT:
                    notifyAccounts();
                    mLastAccountsNotifyTime = time;
                    break;
                case MSG_AREA:
                    notifyAreas();
                    mLastAreasNotifyTime = time;
                    break;
                default:
                    break;
            }
        }

        @Override
        public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
            return super.sendMessageAtTime(msg, uptimeMillis);
        }
    }

    private void notifyAccounts() {
        getContext().getContentResolver().notifyChange(TableContracts.Accounts.CONTENT_URI, null);
    }

    private void notifyAreas() {
        getContext().getContentResolver().notifyChange(TableContracts.Areas.CONTENT_URI, null);
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = MyDatabaseHelper.getInstance(getContext());
        thread = new HandlerThread(TAG);
        thread.setPriority(Thread.NORM_PRIORITY - 2);
        thread.start();
        handler = new NotifyHandler(thread.getLooper());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        GetTableAndWhereOutParameter gtw = new GetTableAndWhereOutParameter();
        getTableAndWhere(uri, sUriMatcher.match(uri), selection, gtw);
        try {
            SQLiteDatabase db = mOpenHelper.getReadableDatabase();
            String groupBy = null;
            Cursor c = db.query(gtw.table, projection, gtw.where, selectionArgs, groupBy, null, sortOrder);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        ContentValues values;
        long rowId = 0;
        long aid = ((MainApplication) getContext().getApplicationContext()).getCurrentAccountId();
        SQLiteDatabase db = null;
        try {
            db = mOpenHelper.getWritableDatabase();
        } catch (SQLiteException e) {
            Log.e(TAG, "insert", e);
            return null;
        }
        Uri newUri = null;
        if (contentValues != null) {
            values = new ContentValues(contentValues);
        } else {
            values = new ContentValues();
        }
        switch (sUriMatcher.match(uri)) {
            case ACCOUNTS:
            case ACCOUNTS_ID:
                rowId = db.insert(TableContracts.Accounts.TABLE_NAME, null, values);
                if (rowId > 0) {
                    newUri = ContentUris.withAppendedId(TableContracts.Accounts.CONTENT_URI, rowId);
                }
                break;
            case AREAS:
            case AREAS_ID:
                if (!values.containsKey(TableContracts.Areas.ACCOUNT_ID)) {
                    values.put(TableContracts.Areas.ACCOUNT_ID, aid);
                }
                rowId = db.insert(TableContracts.Areas.TABLE_NAME, null, values);
                if (rowId > 0) {
                    newUri = ContentUris.withAppendedId(TableContracts.Areas.CONTENT_URI, rowId);
                }
                break;
            default:
                throw new SQLException("Unknown URL " + uri);
        }
        notifyUri(uri);
        return newUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        GetTableAndWhereOutParameter gtw = new GetTableAndWhereOutParameter();
        getTableAndWhere(uri, sUriMatcher.match(uri), selection, gtw);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = db.delete(gtw.table, gtw.where, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        Log.d(TAG, "delete uri " + uri);
        notifyUri(uri);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        GetTableAndWhereOutParameter gtw = new GetTableAndWhereOutParameter();
        getTableAndWhere(uri, sUriMatcher.match(uri), selection, gtw);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = db.update(gtw.table, values, gtw.where, selectionArgs);
        Log.d(TAG, "update uri " + uri);
        notifyUri(uri);
        return count;
    }

    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentProviderResult[] results = super.applyBatch(operations);
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }

    private void getTableAndWhere(Uri uri, int match, String userWhere, GetTableAndWhereOutParameter out) {
        long aid = -1;
        if (match != ACCOUNTS && match != ACCOUNTS_ID) {
            aid = ((MainApplication) getContext().getApplicationContext()).getCurrentAccountId();
        }
        String where = null;
        switch (match) {
            case ACCOUNTS_ID:
                where = TableContracts.Accounts._ID + "=" + uri.getLastPathSegment();
            case ACCOUNTS:
                out.table = TableContracts.Accounts.TABLE_NAME;
                break;
            case AREAS_ID:
                where = TableContracts.Areas._ID + "=" + uri.getLastPathSegment();
            case AREAS:
                if (userWhere == null || (userWhere != null && !userWhere.contains(TableContracts.Areas.ACCOUNT_ID))) {
                    if (TextUtils.isEmpty(where)) {
                        where = TableContracts.Areas.ACCOUNT_ID + "=" + aid;
                    } else {
                        where = where + " AND (" + TableContracts.Areas.ACCOUNT_ID + "=" + aid + ")";
                    }
                }
                if (!TextUtils.isEmpty(userWhere)) {
                    if (TextUtils.isEmpty(where)) {
                        where = userWhere;
                    } else {
                        where = where + " AND (" + userWhere + ")";
                    }
                }
                userWhere = null;
                out.table = TableContracts.Areas.TABLE_NAME;
                break;
            default:
                throw new IllegalStateException("Unknown URL " + uri);
        }
        if (!TextUtils.isEmpty(userWhere)) {
            if (!TextUtils.isEmpty(where)) {
                out.where = where + " AND (" + userWhere + ")";
            } else {
                out.where = userWhere;
            }
        } else {
            out.where = where;
        }
    }
}
