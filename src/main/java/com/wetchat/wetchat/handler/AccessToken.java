package com.wetchat.wetchat.handler;

import com.alibaba.fastjson.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;

/**
 *  http://localhost/qrcode/createQrcode?sceneId=123
 * 辅助类 TODO 先把token放在内存，后面应该放在数据库或缓存中
 */
public class AccessToken implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(WechatHandler.class);
    private static OkHttpClient client = new OkHttpClient();
    private static String APPID = "wxe00b5ecfeb5112ab";
    private static String APPSECRET = "e51df15d9dff427e381b5f2edeb45226";
    private static String CLIENT_CREDENTIAL = "client_credential";

    /**
     * token
     */
    private String accessToken;
    /**
     * 创建时间
     */
    private Long createTime = 0L;
    /**
     * 过期时间，微信token默认过期时间为7200秒
     */
    private Long expiresIn = 0L;

    /**
     * 获取token
     *
     * @return
     */
    public String getAccessToken() throws Exception {
        /*
         * 比较时间, 当前时间 -创建时间 > 过期时间
         * 但是  如果在临界点会出问题， 因此把临界值减小，随便减少多少
         * 当前时间 -创建时间 > 过期时间 - 200
         */
        if (Instant.now().toEpochMilli() - createTime > (expiresIn - 200)) {
            // 如果过期了，则需要重新获取
            // 向微信服务器请求access_token
            // 不用捕获异常，因为并不知道 有异常，该如何处理，油调用者决定
            updateAccessToken();
            return this.accessToken;
        } else {
            return this.accessToken;
        }

    }

    /**
     * 修改token
     */
    public void updateAccessToken() throws Exception {
        // TODO token需要保存下来，因为查询token有限制，2小时一次。
        // 向微信服务器发送请求

        String rsp = queryToken();
              /*   {
                "access_token":"ACCESS_TOKEN",
                    "expires_in":7200
            }*/

        if (!StringUtils.isEmpty(rsp)) {
            JSONObject json = JSONObject.parseObject(rsp);
            String errcode = json.getString("errcode");
            if (StringUtils.isEmpty(errcode)) {
                String access_token = json.getString("access_token");
                Long expires_in = json.getLong("expires_in");
                // 设置
                this.setAccessToken(access_token);
                this.setExpiresIn(expires_in);
                // 设置创建时间
                this.setCreateTime(Instant.now().toEpochMilli());
            } else {
                String errmsg = json.getString("errmsg");
                throw new Exception("wechat server error,," + errmsg);
            }
        }


    }


    /**
     * 查询token
     *
     * @return
     * @throws IOException
     */
    public static String queryToken() throws IOException {
       /* String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=" + CLIENT_CREDENTIAL + "&appid="
                + APPID + "&secret=" + APPSECRET;
        return run(url);*/

       String rsp = "{\n" +
               "    \"access_token\":\"17_fnFd_B7VQnsti3rxxzDGsy5Rb06jm0VttJ6LYWAPlsflOEKpMCDVX-B_nk8TTtw2B8eccVOCHBRJEFPGN7hVBHXiSAX6SFxq3EbK60_-hiaCLVtS_p2zldMW0oAYCBy0jFz1LPp2L4ZHiUoJFSBiAFALYH\",\n" +
               "    \"expires_in\":7200\n" +
               "}";
       return rsp;
    }

    /**
     * 调用微信服务器
     *
     * @param url
     * @return
     * @throws IOException
     */
    public static String run(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        try (Response response = client.newCall(request).execute()) {
            String rsp = response.body().string();
            logger.info("--查询token--rsp--->:" + rsp);
            return rsp;
        }
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
