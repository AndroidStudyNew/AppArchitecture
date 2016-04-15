package com.sjtu.activity;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.sjtu.MainApplication;
import com.sjtu.db.TableContracts;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int LOADER_AREAS_DATA = 0;
    private static final String TAG = "MainActivity";
    private LoaderManager.LoaderCallbacks<Cursor> mAREADataLoaderCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                insertDb();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        initAccountDataLoader();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LoaderManager loaderManager = getSupportLoaderManager();
        loaderManager.destroyLoader(LOADER_AREAS_DATA);
    }

    private void initAccountDataLoader() {
        if (mAREADataLoaderCallbacks != null) {
            getSupportLoaderManager().restartLoader(LOADER_AREAS_DATA, null, mAREADataLoaderCallbacks);
        } else {
            mAREADataLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {

                @Override
                public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
                    String[] projection = {TableContracts.Areas._ID, TableContracts.Areas.CODE, TableContracts.Areas.NAME,
                            TableContracts.Areas.LEVEL};
                    String selection = TableContracts.Areas.ACCOUNT_ID + "=" + ((MainApplication) MainActivity.this.getApplication()).getCurrentAccountId();
                    CursorLoader cursorLoader = new CursorLoader(MainActivity.this, TableContracts.Areas.CONTENT_URI, projection, selection, null, null);
                    return cursorLoader;
                }

                @Override
                public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
                    updateView(cursor);
                }

                @Override
                public void onLoaderReset(Loader<Cursor> arg0) {
                }
            };
            getSupportLoaderManager().initLoader(LOADER_AREAS_DATA, null, mAREADataLoaderCallbacks);
        }
    }

    private String code;
    private String name;
    private int level;
    private void updateView(Cursor cursor) {
        if (cursor != null) {
            Log.e(TAG,cursor.getCount() + "");
            while (cursor.moveToNext()) {
                level = cursor.getInt(cursor.getColumnIndex(TableContracts.Areas.LEVEL));
                name = cursor.getString(cursor.getColumnIndex(TableContracts.Areas.NAME));
                code = cursor.getString(cursor.getColumnIndex(TableContracts.Areas.CODE));
                Log.e(TAG,"code:" + code + ",name:" + name + ",level:" + level);
            }
            cursor.close();
        }
    }


    //FIXME 用于测试
    private void insertDb() {
        updateAccountData();
        updateAreaData();
    }

    private void  updateAreaData() {
        //TableContracts.Cards.NAME, TableContracts.Cards.TYPE, TableContracts.Cards.COMPANY,TableContracts.Cards.TITLE,TableContracts.Cards.STATUS
        ContentResolver resolver = this.getContentResolver();
        resolver.delete(TableContracts.Areas.CONTENT_URI,null,null);
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        for (int i = 0; i < 20; i ++) {
            Log.e(TAG,"updateAreaData:" + i);
            ops.add(ContentProviderOperation.newInsert(TableContracts.Areas.CONTENT_URI)
                    .withValue(TableContracts.Areas.ACCOUNT_ID, ((MainApplication) MainActivity.this.getApplication()).getCurrentAccountId())
                    .withValue(TableContracts.Areas.CODE, 100008 + i)
                    .withValue(TableContracts.Areas.NAME, "马云" + i)
                    .withValue(TableContracts.Areas.LEVEL, 100008 + i)
                    .build());
        }
        try {
            ContentProviderResult[] contentProviderResults = resolver.applyBatch(TableContracts.AUTHORITY, ops);
            Log.e(TAG,"contentProviderResults:" + contentProviderResults.length);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
        ops.clear();
    }

    //FIXME 用于测试，向数据库插入数据 插入用户表
    private void updateAccountData() {
        ContentResolver resolver = this.getContentResolver();
        resolver.delete(TableContracts.Accounts.CONTENT_URI, null, null);
        resolver.delete(TableContracts.Areas.CONTENT_URI, null, null);


        Uri uri = TableContracts.Accounts.CONTENT_URI;
        ContentValues values = new ContentValues();
        values.put(TableContracts.Accounts.USER_ID,"00528000000uYrMAAU");
        values.put(TableContracts.Accounts.NAME,"K Kina7");
        values.put(TableContracts.Accounts.ACCOUNT_STATE, TableContracts.Accounts.STATE_ACTIVE);
        resolver.insert(uri, values);
        ((MainApplication)getApplication()).updateCurrentAccount();
    }
}
