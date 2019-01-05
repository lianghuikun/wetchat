package com.wetchat.wetchat;

import com.alibaba.fastjson.JSONObject;
import com.wetchat.wetchat.util.SHA1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 回复消息的流程：
 * 消息-》微信服务器-》微信服务器转发到我的服务器-》我的服务器返回给微信服务器
 */
@RestController
@RequestMapping("/wechatHandler")
public class WechatHandler {

    private static Logger logger = LoggerFactory.getLogger(WechatHandler.class);
    private static final String WECHAT_TOKEN = "KING";

    /**
     * 微信服务器调用我们服务器看是否能调通
     *
     * @param signature
     * @param timestamp
     * @param nonce
     * @param echostr
     * @return
     */
    @GetMapping("/checkSignature")
    public String checkSignature(String signature,
                                 String timestamp,
                                 String nonce,
                                 String echostr) {

        /**
         *
         * http://localhost/wechatHandler/checkSignature?signature=c01a0c4ff9682c2a4586819dece7474189c1877b&timestamp=1546696329&nonce=535158386&echostr=123
         * : ---入参:[signature=?,timestamp=?,nonce=?,echostr=?]---->:c01a0c4ff9682c2a4586819dece7474189c1877b
         * ---排序前---->:["KING","1546696329","535158386"]
         * ---排序后---->:["1546696329","535158386","KING"]
         */

        // 1、将token   timestamp   nonce 按字典序排列，也就是按照字母排序
        List<String> list = Arrays.asList(WECHAT_TOKEN, timestamp, nonce);
        logger.info("---排序前---->:" + JSONObject.toJSONString(list));
        Collections.sort(list);
        logger.info("---排序后---->:" + JSONObject.toJSONString(list));
        // 2、需要将排序好的三个字符串拼接
        String str = "";
        for (String s : list) {
            str += s;
        }
        // 3、将拼接好的字符串进行SHA1加密
        String realSignature = SHA1.encode(str);
        // 如果校验失败了，则应该加密之后再取16进制的值
        if (signature.equals(realSignature)) {
            /*
             *  如果校验成功，直接返回
             *  校验的目的是，确认是微信发过来的消息
             */
            return echostr;
        } else {
            // 如果失败了，随便发个字符串, 或返回403错误都是可以的
            return "abc";
        }
        // 4、此时去微信界面配置，应该可以配置成功
//        String realSignature = SHA1.encode(str);
//        return signature.equals(realSignature);
    }

}
