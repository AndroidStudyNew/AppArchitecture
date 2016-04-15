package com.sjtu.base;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by CharlesZhu on 2016/4/14.
 */
public abstract class BaseAPI {

    private static final String TAG = "BaseAPI";

    private int mAPIType;
    private String mAppAgent = null;
    private static final int DEFAULT_TIMEOUT = 12000;

    public BaseAPI(int apiType, String appver) {
        this.mAPIType = apiType;
        this.mAppAgent = appver;
    }

    /**
     * 对http post和get的封装
     */
    public abstract static class Ope {
        public abstract void onResponseOk(HttpURLConnection con) throws BaseException;

        void post(HttpURLConnection con) throws PostException {

        }
    }

    public abstract String getAPI(int type);

    /**
     * 获取网络连接
     *
     * @param string
     * @param timeout 网络超时，如果设置0，则取默认值12s
     * @return
     * @throws IOException
     */
    private HttpURLConnection openConnection(String string, int timeout) throws IOException {
        URL url = new URL(string);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        String agent = System.getProperty("http.agent");
        con.setRequestProperty("User-Agent", agent + " " + mAppAgent);
        if (timeout == 0) {
            timeout = DEFAULT_TIMEOUT;
        }
        con.setConnectTimeout(timeout);
        return con;
    }

    protected void operation(String args, Ope ope, int api, int timeout) throws BaseException {
        String baseUrl = null;
        baseUrl = getAPI(api);
        if (TextUtils.isEmpty(baseUrl)) {
            throw new BaseException(BaseException.CODE_ERROR_URL_IS_NULL);
        }
        HttpURLConnection con = null;
        try {
            con = openConnection(baseUrl + args, timeout);
            Log.d(TAG, "connect to:" + baseUrl + args);
            if (ope != null) {
                ope.post(con);
            }
            int code = con.getResponseCode();
            Log.d(TAG, baseUrl + args + " ResponseCode: " + code);
            if (code == HttpURLConnection.HTTP_OK) {//200
                if (ope != null) {
                    ope.onResponseOk(con);
                }
            } else {
                throw new BaseException(code);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new BaseException(BaseException.CODE_ERROR_OPENCONN, e);
        } finally {
            if (con != null)
                con.disconnect();
        }
    }

    protected String readContent(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuffer sb = new StringBuffer();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
        }
        return sb.toString();
    }
}
