package com.sjtu.api;

import com.sjtu.base.BaseJsonObj;

import org.json.JSONObject;

/**
 * 最基本的返回体
 * Created by CharlesZhu on 2016/3/14.
 */
public class BaseMessage extends BaseJsonObj {

    public BaseMessage(JSONObject obj) {
        super(obj);
    }

    public int ret;
    public String err;

    public boolean isOK() {//ret为0代表请求成功
        return ret == 0;
    }
}
