package com.wetchat.wetchat.handler;

import com.alibaba.fastjson.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * 网页授权
 * 需要在公众号设置，但是我的个人订阅号没地方设置。
 * 这代代码仅仅是模拟
 * 也可以通过测试公众号测试开发，测试公众号平台用后所有权限
 */
@RestController
@RequestMapping("/profile")
public class ProfileHandler {
    private static OkHttpClient client = new OkHttpClient();
    private static Logger logger = LoggerFactory.getLogger(ProfileHandler.class);

    /**
     * 用户会访问： https://open.weixin.qq.com/connect/oauth2/authorize?appid=APPID&redirect_uri=REDIRECT_URI&response_type=code&scope=SCOPE&state=STATE#wechat_redirect
     * 因此接收传过来的code
     * // TODO 这个网址, 需要把这个类的全路径品进去
     * www.xx.com/profile/get  拼进去可能有引号之类的，因此需要转码。
     * 拼好上述url， 放在菜单里，或者在url中访问。
     *
     * @param code
     * @return
     */
    @GetMapping("/get")
    public String get(String code) throws IOException {

        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=" + code + "&grant_type=authorization_code";
        Request request = new Request.Builder()
                .url(url)
                .build();
        try (Response response = client.newCall(request).execute()) {
            String rsp = response.body().string();
            /*

            {
               "access_token":"ACCESS_TOKEN",
               "expires_in":7200,
               "refresh_token":"REFRESH_TOKEN",
               "openid":"OPENID",
               "scope":"SCOPE"
            }
             */
            JSONObject jsonObject = JSONObject.parseObject(rsp);
            String errcode = jsonObject.getString("errcode");
            if (!StringUtils.isEmpty(errcode)) {
                String access_token = jsonObject.getString("access_token");

                // 拿到access_token继续请求
                // https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN
                request = new Request.Builder()
                        .url(url)
                        .build();
                Response r = client.newCall(request).execute();
                String s = r.body().string();

                // 然后解析, 如果没出错则直接返回用户信息
                // 最好的做法时，此时新建一个页面页面或html代码，直接返回。
                /*
                {
                   "openid":" OPENID",
                   " nickname": NICKNAME,
                   "sex":"1",
                   "province":"PROVINCE"
                   "city":"CITY",
                   "country":"COUNTRY",
                    "headimgurl":    "http://wx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/46",
                    "privilege":[
                    "PRIVILEGE1"
                    "PRIVILEGE2"
                    ],
                    "unionid": "o6_bmasdasdsad6_2sgVt7hMZOPfL"
                }
                 */
            } else {
                return "出错";
            }
            return rsp;
        }
    }
}
