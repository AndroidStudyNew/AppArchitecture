package com.sjtu.api;

import com.sjtu.base.BaseJsonObj;

import org.json.JSONObject;

/**
 * Created by CharlesZhu on 2016/3/14.
 */
public class UserInfoResult extends BaseMessage {

    public UserInfoResult(JSONObject obj) {
        super(obj);
    }

    public Data data;

    public static class Data extends BaseJsonObj {

        public Data(JSONObject obj) {
            super(obj);
        }

        public String code;
        public String name;//用户姓名
        public int level;//0

    }
}
