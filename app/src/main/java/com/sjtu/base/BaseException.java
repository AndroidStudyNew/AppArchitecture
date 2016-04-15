package com.sjtu.base;

/**
 * Created by CharlesZhu on 2016/4/14.
 */
public class BaseException extends Exception {

    public static final int CODE_ERROR_OPENCONN = -1001;
    public static final int CODE_ERROR_URL_IS_NULL = -1002;

    public static final int CODE_ERROR_PARAMETER_NOT_LEGAL = 1;//参数不合法
    public static final int CODE_ERROR_SALESFORCE_TOKEN_NOT_VALID = 2;//登录token失效
    public static final int CODE_ERROR_SALESFORCE_NUM_NOT_ENOUGH = 3;//salesforce张数不够
    public static final int CODE_ERROR_PROFEEDING_NUM_NOT_ENOUGH = 4;//精准识别张数不够
    public static final int CODE_ERROR_LACK_LASTNAME = 5;//缺少LastName
    public static final int CODE_ERROR_LACK_COMPANY = 6;//缺少Company
    public static final int CODE_ERROR_FIELD_ERROR = 7;//字段错误
    public static final int CODE_ERROR_PROFEEDING_FAIL = 8;//云识别失败
    public static final int CODE_ERROR_UNKNOWN = 9;//未知错误


    public int code;

    public BaseException() {
        super();
    }

    public BaseException(Throwable throwable) {
        super(throwable);
    }

    public BaseException(int code, Throwable throwable) {
        super(throwable);
        this.code = code;
    }

    public BaseException(int code) {
        super("BaseException Error(" + code + ")");
        this.code = code;
    }
}
