package com.sjtu.util;

public class BillingUtil {

	private static final String TAG = "BillingUtil";

	public static final String EXTRA_PRODUCT_ID = "IAP.PRODUCT_ID";
	public static final String EXTRA_DEV_PAYLOAD = "IAP.DEV_PAYLOAD";
	/**
	 * 购买类型，分SFDC类型、DPS类型
	 */
	public static final String EXTRA_PURCHASE_TYPE = "IAP.PURCHASE_TYPE";
	public static final String EXTRA_SKU_TYPE = "IAP.SKU_TYPE";

	/**
	 * 购买类型：
	 * 1 PURCHASING_TYPE_SFDC SFDC类型，SFDC类型product_id 需要根据/pay/getPackageList接口返回，该数据存到本地文件Const.DIR_PAY + Const.SALESFORCE_ACCOUNT_PACKAGE_LIST_DATA
	 * 2 PURCHASING_TYPE_DPS DPS类型，DPS类型product_id 为本地固定写死
	 */
	public static final int PURCHASE_TYPE_SFDC = 100011;
	public static final int PURCHASE_TYPE_DPS = 100012;

	public static final int SKU_SFDC_TYPE_CONSUME = 100013;  //1表示套餐为消耗类型
	public static final int SKU_SFDC_TYPE_SUBSCIBE = 100014;  //2表示套餐为订阅类型

	//TODO SFDC走订阅应用内购买商品ID
	public static final String SKU_SFDC_MONTHLY_PRODUCT_ID = "salesforce.standard.1month";
	public static final String SKU_SFDC_YEARLY_PRODUCT_ID = "salesforce.standard.1year";

	/**
	 * DPS 走消耗  本地固定
 	 */
	public static final String DPS_PROPERTY_ID = "CamCard_DPS_Balance";
	public static final String SKU_DPS_10_PRODUCT_ID = "salesforce.proofreading.10";
	public static final String SKU_DPS_100_PRODUCT_ID = "salesforce.proofreading.100";
	public static final int DPS_NUMBER_FOR_10 = 10;
	public static final String DPS_PRICE_FOR_10 = "$5.99";
	public static final int DPS_NUMBER_FOR_100 = 100;
	public static final String DPS_PRICE_FOR_100 = "$39.99";

	public static final String getAppPublicKey() {
		return "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAj8au9C7lPAY6vWCZu8/E2IEWH4LcU/JVD6Nw/ATGcH8wViFEeblVnR8ytaaU8v6v9t2NixPXjekA08gCQ"
				+ "+"
				+ "XeWuhe7X0ipJKQNQky9GIECYGA7tjhkTM5ZXST94lYueua0SvH3qCo0cvqy+XrhQsguf7zBoVkqgJOxSbeG1Jg0ERZxfhAdgBNZtg"
				+ "+"
				+ "4hg2RpLsKm8/TfzlBD9BzKOLsXAyAeNGOkDjSWImXUqkjQ6/7fjVCF6ROCmouN8ciPY8jbbGjZhTxGstoMK2weAmbPeKw6CzjzyngoL8VZSwFHiK39MrMjOCdr3r45djUXyyqtFUPPODTeUsAdjx0FmhNr9+TQIDAQAB";
	}



}
