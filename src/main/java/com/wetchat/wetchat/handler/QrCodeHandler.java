package com.wetchat.wetchat.handler;

import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import org.jboss.logging.Logger;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 这个controller是我们自己调用的，而不是微信调用的，我们自己调用，然后这里调用微信服务器
 * 因此可以在本地跑，和服务器无关
 */
@RestController
@RequestMapping("/qrcode")
public class QrCodeHandler {
    private static final Logger logger = Logger.getLogger(QrCodeHandler.class);

    private static OkHttpClient client = new OkHttpClient();

    /**
     * http请求方式: POST
     * URL: https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=TOKEN
     * POST数据格式：json
     * POST数据例子：{"action_name": "QR_LIMIT_SCENE", "action_info": {"scene": {"scene_id": 123}}}
     * 或者也可以使用以下POST数据创建字符串形式的二维码参数：
     * {"action_name": "QR_LIMIT_STR_SCENE", "action_info": {"scene": {"scene_str": "123"}}}
     */
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");


    /**
     * 创建二维码(以生成永久的为例), TODO 在本地就可以运行，如果在微信中配置了白名单了已经
     * 调用时sceneId,可以随便填，比如123
     */
    @GetMapping("/createQrcode")
    public String createQrcode(String sceneId) {
        // 查询调用凭据
        String token = null;
        try {
            AccessToken accessToken = new AccessToken();
            token = accessToken.getAccessToken();
//            token = queryToken();
            String url = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=" + token;

            //{"action_name": "QR_LIMIT_SCENE", "action_info": {"scene": {"scene_id": 123}}}

            JSONObject req = new JSONObject();
            req.put("action_name", "QR_LIMIT_SCENE");
            JSONObject action_info = new JSONObject();
            JSONObject scene = new JSONObject();
            scene.put("scene_id", sceneId);
            action_info.put("scene", scene);
            req.put("action_info", action_info);


            String json = token.toString();
            try {
                RequestBody body = RequestBody.create(JSON, json);
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
                Response response = client.newCall(request).execute();
                String string = response.body().string();
                // {"ticket":"gQH47joAAAAAAAAAASxodHRwOi8vd2VpeGluLnFxLmNvbS9xL2taZ2Z3TVRtNzJXV1Brb3ZhYmJJAAIEZ23sUwMEmm3sUw==","expire_seconds":60,"url":"http:\/\/weixin.qq.com\/q\/kZgfwMTm72WWPkovabbI"}
                // {"errcode":40013,"errmsg":"invalid appid"}
                JSONObject rsp = JSONObject.parseObject(string);
                logger.info("-----查询ticket------>" + rsp.toJSONString());
                String errcode = rsp.getString("errcode");
                if (!StringUtils.isEmpty(errcode)) {
                    // 如果出错了
                    return "查询ticket出错了" + rsp.getString("errmsg");
                } else {
                    // 拿到票据，根据票据换取二维码图片链接
                    String ticket = rsp.getString("ticket");
                    // 临时有效期，临时二维码才有，永久二维码没有这个返回
//                    String expire_seconds = rsp.getString("expire_seconds");
                    String ticketUrl = rsp.getString("url");

                    // 获取二维码图片
                /*    HTTP GET请求（请使用https协议）
                    https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=TICKET*/
                    String qrUrl = String.format("https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=%s", ticket);
                    logger.info("----qrUrl--->:" + qrUrl);
                    return "<img src=" + qrUrl + "/>";
                    /*  Request qrRequest = new Request.Builder()
                            .url(url)
                            .build();

                    try {
                        Response qrResponse = client.newCall(request).execute();
                        String qr = response.body().string();
                        logger.info("----->二维码图片地址为:" + qr);
                        return qr;
                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.error("---->获取二维码地址出错," + e.getMessage());
                        return "-----获取二维码地址出错!" + e.getMessage();
                    }*/

                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("---->创建二维码出错," + e.getMessage());
                return "-----创建二维码出错!" + e.getMessage();
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("---->查询token异常," + e.getMessage());
            return "-----查询token异常!" + e.getMessage();
        }
    }
}
