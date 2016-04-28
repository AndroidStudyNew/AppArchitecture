package com.sjtu.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.sjtu.Const;
import com.sjtu.MainApplication;
import com.sjtu.base.BaseException;
import com.sjtu.bill.util.IabBroadcastReceiver;
import com.sjtu.bill.util.IabHelper;
import com.sjtu.bill.util.IabResult;
import com.sjtu.bill.util.Inventory;
import com.sjtu.bill.util.Purchase;
import com.sjtu.util.BillingUtil;
import com.sjtu.util.PreferenceHelper;


/**
 * google pay 支付的帮助类
 */
public class BillingHelpActivity extends AppCompatActivity {

    private final static String TAG = "BillingHelpActivity";
    private final static int REQUEST_CODE = 100;
    private boolean mBillingSupported = false;

    private String mProductID;
    private String mDeveloperPayload;
    /**
     * 购买类型，分SFDC类型、DPS类型
     */
    private int mPurchaseType;
    /**
     * 套餐类型，只针对SFDC类型，分 次数类型、订阅类型
     */
    private int mSkuType;

    private BillingHelper mBillingHelper;
    private ProgressDialog mCheckSignDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCheckSignDialog = new ProgressDialog(this);
        mCheckSignDialog.setMessage(getString(R.string.msg_check_order));
        Intent i = getIntent();
        mProductID = i.getStringExtra(BillingUtil.EXTRA_PRODUCT_ID);
        mDeveloperPayload = i.getStringExtra(BillingUtil.EXTRA_DEV_PAYLOAD);
        mPurchaseType = i.getIntExtra(BillingUtil.EXTRA_PURCHASE_TYPE,-1);
        mSkuType = i.getIntExtra(BillingUtil.EXTRA_SKU_TYPE,-1);

        Log.d(TAG, "pid=" + mProductID + ",mDeveloperPayload=" + mDeveloperPayload + ",\n mPurchaseType=" + mPurchaseType + ",mSkuType=" + mSkuType);

        mBillingHelper = new BillingHelperV3();
        mBillingHelper.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mBillingHelper.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBillingHelper.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBillingHelper.onDestory();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mBillingHelper.onActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        mBillingHelper.onSaveInstaceState(savedInstanceState);
    }

    abstract class BillingHelper {
        public void onCreate(Bundle savedInstanceState) {
        }

        public void onStart() {
        }

        public void onStop() {
        }

        public void onDestory() {
        }

        public void onSaveInstaceState(Bundle savedInstanceState) {
        }

        public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
            return false;
        }

        public abstract void checkSign(final int purchase_type,String sku, final String signed_data, final String signature);
    }

    class BillingHelperV3 extends BillingHelper  implements IabBroadcastReceiver.IabBroadcastListener {

        private IabHelper mIabHelper;

        // Provides purchase notification while this app is running
        IabBroadcastReceiver mBroadcastReceiver;
        /**
         * 支付完成的回调
         */
        private IabHelper.OnIabPurchaseFinishedListener mPurchaseListener = new IabHelper.OnIabPurchaseFinishedListener() {

            @Override
            public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
                Log.d(TAG, "Purchase Vip Finished");
                if (result.isFailure()) {
                    Log.d(TAG, "Purchase Fail " + result.getMessage());
                } else {
                    Log.d(TAG, "Subscription success");
                    Log.d(TAG, purchase.toString());
                }
                if (!verifyDeveloperPayload(purchase)) {
                    // TODO Error purchasing. Authenticity verification failed
                    return;
                }
                checkSign(mPurchaseType,purchase.getSku(), purchase.getOriginalJson(), purchase.getSignature());
                Log.d(TAG, "Purchase successful.");
            }
        };

        /**
         * Listener that's called when we finish querying the items and subscriptions we ownr
         * 查询完成的回调，RestoreOrder的时候用，当有订单成功付款但由于种种原因（突然断网、断电等）没收到Google支付成功的回调时，在这里可以查询到此订单
         */
        IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
            public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
                Log.d(TAG, "Query inventory finished.");

                // Have we been disposed of in the meantime? If so, quit.
                if (mIabHelper == null) {
                    Log.d(TAG,"Failed to query inventory: (mIabHelper = null");
                    return;
                }

                if (result.isFailure()) {
                    Log.d(TAG,"Failed to query inventory: " + result);
                    return;
                }

                Log.d(TAG, "Query inventory was successful.");

                /*
                 * Check for items we own. Notice that for each purchase, we check
                 * the developer payload to see if it's correct! See
                 * verifyDeveloperPayload().
                 */
                Purchase purchase = inventory.getPurchase(mProductID);
                if (purchase != null && verifyDeveloperPayload(purchase)) {
                    checkSign(mPurchaseType,purchase.getSku(),purchase.getSignature(),purchase.getOriginalJson());
                }
            }
        };

        /**
         * IabHelper设置完成的回调，主要判断 googleplay 是否可用
         */
        private IabHelper.OnIabSetupFinishedListener mSetupFinishedListener = new IabHelper.OnIabSetupFinishedListener() {

            @Override
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished.");

                if (!result.isSuccess()) {
                    Toast.makeText(getApplicationContext(), R.string.msg_googleplay_unavailable, Toast.LENGTH_LONG).show();
                    Log.d(TAG, "IABHelper Setup Failed");
                    finish();
                    return;
                }
                mBillingSupported = mIabHelper.subscriptionsSupported();
                if (!mBillingSupported) {
                    Log.d(TAG, "Subscription NOT support");
                    Toast.makeText(getApplicationContext(), R.string.msg_googleplay_unavailable, Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (mIabHelper == null) return;
                try {
                    if (mSkuType == BillingUtil.SKU_SFDC_TYPE_CONSUME) {  //消耗类型
                        Log.d(TAG, "launchPurchaseFlow");
                        mIabHelper.launchPurchaseFlow(BillingHelpActivity.this,mProductID,REQUEST_CODE,mPurchaseListener,mDeveloperPayload);
                    } else if (mSkuType == BillingUtil.SKU_SFDC_TYPE_SUBSCIBE) {  //订阅类型
                        mIabHelper.launchSubscriptionPurchaseFlow(BillingHelpActivity.this, mProductID, REQUEST_CODE,mPurchaseListener, mDeveloperPayload);
//                        mIabHelper.launchPurchaseFlow(BillingHelpActivity.this,mProductID,IabHelper.ITEM_TYPE_SUBS,REQUEST_CODE,mPurchaseListener,mDeveloperPayload);
                        Log.d(TAG, "launchSubscriptionPurchaseFlow");
                    }
                } catch (IabHelper.IabAsyncInProgressException e) {
                    e.printStackTrace();
                }

                // Important: Dynamically register for broadcast messages about updated purchases.
                // We register the receiver here instead of as a <receiver> in the Manifest
                // because we always call getPurchases() at startup, so therefore we can ignore
                // any broadcasts sent while the app isn't running.
                // Note: the receiver must be registered after
                // IabHelper is setup, but before first call to getPurchases().
                mBroadcastReceiver = new IabBroadcastReceiver(BillingHelperV3.this);
                IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
                registerReceiver(mBroadcastReceiver, broadcastFilter);

                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                Log.d(TAG, "Setup successful. Querying inventory.");
                try {
                    mIabHelper.queryInventoryAsync(mGotInventoryListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    e.printStackTrace();
                }
            }
        };


        @Override
        public void onCreate(Bundle savedInstanceState) {
            SharedPreferences sf = PreferenceManager.getDefaultSharedPreferences(BillingHelpActivity.this);
            int purchaseType = sf.getInt(PreferenceHelper.getSpecificPreferenceKey(BillingHelpActivity.this,Const.KEY_INAPP_SKU_TYPE + mPurchaseType), -1);
            String sku = sf.getString(PreferenceHelper.getSpecificPreferenceKey(BillingHelpActivity.this,Const.KEY_INAPP_SKU_TYPE + mPurchaseType),"");
            String signature = sf.getString(PreferenceHelper.getSpecificPreferenceKey(BillingHelpActivity.this,Const.KEY_INAPP_SIGNATURE + mPurchaseType), "");
            String signed_data = sf.getString(PreferenceHelper.getSpecificPreferenceKey(BillingHelpActivity.this,Const.KEY_INAPP_SIGNED_DATA + mPurchaseType), "");
            if (purchaseType == mPurchaseType && !TextUtils.isEmpty(signature) && !TextUtils.isEmpty(signed_data)) {
                checkSign(purchaseType,sku,signed_data, signature);
            } else {
                String base64EncodedPublicKey = BillingUtil.getAppPublicKey();
                mIabHelper = new IabHelper(BillingHelpActivity.this, base64EncodedPublicKey);
                mIabHelper.enableDebugLogging(true, TAG);
                // Start setup. This is asynchronous and the specified listener
                // will be called once setup completes.
                mIabHelper.startSetup(mSetupFinishedListener);
            }
        }

        @Override
        public void onDestory() {
            try {
                if (mIabHelper != null) {
                    mIabHelper.dispose();
                    mIabHelper = null;
                }
            } catch (IabHelper.IabAsyncInProgressException e) {
                e.printStackTrace();
            }
        }

        /**
         * @param purchase_type  购买类型，分SFDC类型、DPS类型
         * @param sku 购买商品的id
         * @param signed_data google pay 返回的签名数据
         * @param signature google pay 返回的签名
         */
        @Override
        public void checkSign(final int purchase_type, final String sku, final String signed_data, final String signature) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mCheckSignDialog != null) {
                                mCheckSignDialog.show();
                            }
                        }
                    });
                    boolean result = false;
                    try {
                        if (!TextUtils.isEmpty(sku)) {
                            if (purchase_type == BillingUtil.PURCHASE_TYPE_DPS) {
                                result = MainApplication.getAPI().updateDPSProperty(signature,signed_data);
                            } else if (purchase_type == BillingUtil.PURCHASE_TYPE_SFDC) {
                                result = MainApplication.getAPI().updateSFDCProperty(signature,signed_data);
                            }
                        } else {
                            result = false;
                        }
                    } catch (BaseException e) {
                        e.printStackTrace();
                    }

                    SharedPreferences sf = PreferenceManager.getDefaultSharedPreferences(BillingHelpActivity.this);
                    if (result) { //EXTRA_SUCCESS
                        sf.edit().remove(PreferenceHelper.getSpecificPreferenceKey(BillingHelpActivity.this, Const.KEY_INAPP_SKU_TYPE + purchase_type))
                                .remove(PreferenceHelper.getSpecificPreferenceKey(BillingHelpActivity.this,Const.KEY_INAPP_SIGNED_DATA + purchase_type))
                                .remove(PreferenceHelper.getSpecificPreferenceKey(BillingHelpActivity.this,Const.KEY_INAPP_SIGNATURE + purchase_type))
                                .remove(PreferenceHelper.getSpecificPreferenceKey(BillingHelpActivity.this,Const.KEY_PURCHASE_TYPE + purchase_type))
                                .commit();
                    } else { //FAILED
                        sf.edit().putString(PreferenceHelper.getSpecificPreferenceKey(BillingHelpActivity.this,Const.KEY_INAPP_SKU_TYPE + purchase_type), sku)
                                .putString(PreferenceHelper.getSpecificPreferenceKey(BillingHelpActivity.this,Const.KEY_INAPP_SIGNED_DATA + purchase_type), signed_data)
                                .putString(PreferenceHelper.getSpecificPreferenceKey(BillingHelpActivity.this,Const.KEY_INAPP_SIGNATURE + purchase_type), signature)
                                .putInt(PreferenceHelper.getSpecificPreferenceKey(BillingHelpActivity.this,Const.KEY_PURCHASE_TYPE + purchase_type), purchase_type)
                                .commit();
                    }
                    final boolean isFinish = result;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mCheckSignDialog != null) {
                                mCheckSignDialog.dismiss();
                            }
                            if (isFinish) {
                                //TODO 表示上传服务器购买数据失败，应该做重试机制

                            } else { // 上传服务器购买数据成功

                            }
                            finish();
                        }
                    });
                }
            }).start();
        }

        @Override
        public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
            return mIabHelper != null && mIabHelper.handleActivityResult(requestCode, resultCode, data);
        }

        /**
         * Verifies the developer payload of a purchase.
         */
        boolean verifyDeveloperPayload(Purchase p) {
            /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */
            String payload = p.getDeveloperPayload();
            if (payload.equalsIgnoreCase(mDeveloperPayload)) {
                return true;
            }
            return false;
        }

        @Override
        public void receivedBroadcast() {
            // Received a broadcast notification that the inventory of items has changed
            Log.d(TAG, "Received broadcast notification. Querying inventory.");
            try {
                mIabHelper.queryInventoryAsync(mGotInventoryListener);
            } catch (IabHelper.IabAsyncInProgressException e) {
                e.printStackTrace();
            }
        }
    }

}
