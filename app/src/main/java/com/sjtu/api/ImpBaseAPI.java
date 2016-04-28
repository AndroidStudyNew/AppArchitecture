package com.sjtu.api;

import com.sjtu.base.BaseAPI;
import com.sjtu.base.BaseException;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;

/**
 * Created by CharlesZhu on 2016/4/14.
 */
public class ImpBaseAPI extends BaseAPI {

    public static final int TEST_ENVIRONMENT = 1;
    public static final int OFFICIAL_ENVIRONMENT = 2;

    public static final int SERVER_OK = -1;
    public static final int NO_NETWORK = -2;

    final String HTTP_POST = "POST";

    protected static final int API_USER = 1;
    protected static final int API_AREAS = 2;
    protected static final int API_COMMON = 6;

    public ImpBaseAPI(int apiType, String appver) {
        super(apiType, appver);
    }

    @Override
    public String getAPI(int api) {
        //TODO
        if (api == API_USER) {

        } else if (api == API_AREAS) {

        } else if (api == API_COMMON) {

        }
        return null;
    }

    /**
     * 用户登录，获取用户信息
     *
     * @param name
     * @param password
     * @return
     * @throws BaseException
     */
    public UserInfoResult getUserInfo(String name,String password) throws BaseException {
        String args = null;
        try {
            args = "/loadUserInfo?name=" + URLEncoder.encode(password,"UTF-8")+ "&file_name=" + URLEncoder.encode(password,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        final UserInfoResult[] result = new UserInfoResult[1];
        operation(args, new Ope() {
            @Override
            public void onResponseOk(HttpURLConnection con) throws BaseException {
                try {
                    String content = readContent(con.getInputStream());
                    JSONObject obj = new JSONObject(content);
                    result[0] = new UserInfoResult(obj);
                    if (!result[0].isOK()) {
                        throw new BaseException(result[0].ret);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, API_USER, 0);
        return result[0];
    }

    /**
     * POST类型API使用例：上传名片图片
     *
     * @throws BaseException
     */
   /* public UploadFileResult uploadFile(final String token, final String filePath) throws BaseException {
        String args = null;
        try {
            args = "/uploadFile?token=" + token + "&file_name=" + URLEncoder.encode(filePath,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        operation(args, new Ope() {

            @Override
            void post(HttpURLConnection con) throws PostException {
                try {
                    con.setRequestMethod(HTTP_POST);
                    con.setDoOutput(true);
                    OutputStream out = con.getOutputStream();
                    byte[] buf = new byte[1024];
                    int len = 0;
                    FileInputStream fis = new FileInputStream(filePath);
                    while ((len = fis.read(buf)) != -1) {
                        out.write(buf, 0, len);
                    }
                    fis.close();
                    out.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new PostException(e);
                }
            }

            @Override
            void onResponseOk(HttpURLConnection con) throws BaseException {
                try {
                    String content = readContent(con.getInputStream());
                    JSONObject obj = new JSONObject(content);
//                    result[0] = new UploadFileResult(obj);
//                    if (!result[0].isOK()) {
//                        throw new BaseException(result[0].ret);
//                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, API_AREAS, 0);
        return null;
    }*/


    /**
     * DPS张数充值
     * @param signature
     * @param sign_content
     * @return
     * @throws BaseException
     */
    public boolean updateDPSProperty(String signature, final String sign_content) throws BaseException {
        return false;
    }

    /**
     * SFDC购买充值
     * @param signature
     * @param sign_content
     * @return
     * @throws BaseException
     */
    public boolean updateSFDCProperty(String signature, final String sign_content) throws BaseException {
        return false;
    }

}
