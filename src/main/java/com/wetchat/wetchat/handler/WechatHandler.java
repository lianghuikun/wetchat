package com.wetchat.wetchat.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wetchat.wetchat.entity.TextMessage;
import com.wetchat.wetchat.util.MessageUtil;
import com.wetchat.wetchat.util.SHA1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    /**
     * 回复微信消息，切记微信是POST请求
     * @param request
     * @param response
     * @return
     */
    @PostMapping
    public String receive(HttpServletRequest request, HttpServletResponse response) {
        try {
            Map<String, String> pool = MessageUtil.parseXml(request);
            String json = JSON.toJSONString(pool);
            TextMessage textMessage = JSON.parseObject(json, TextMessage.class);
            String msgType = textMessage.getMsgType();
            if (msgType.equals("text")) {
                // 如果是文本消息
                String content = textMessage.getContent();
                // 将文本消息原样返回做为回复
                TextMessage rspMessage = new TextMessage();
                // 交换接收和发送双方的身份
                rspMessage.setToUserName(textMessage.getFromUserName());
                rspMessage.setFromUserName(textMessage.getToUserName());
                // 当前时间戳
                rspMessage.setCreateTime(Instant.now().toEpochMilli());
                rspMessage.setMsgType("text");
                rspMessage.setContent(content);
                // msgId不需要设置
//                rspMessage.setMsgId();
                String writeXml = MessageUtil.textMessageToXml(rspMessage);
                return writeXml;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

  /*  @PostMapping
    public String receive(HttpServletRequest request, HttpServletResponse response){
        logger.info("----------------开始处理微信发过来的消息------------------");
        // 微信服务器POST消息时用的是UTF-8编码，在接收时也要用同样的编码，否则中文会乱码；
        request.setCharacterEncoding("UTF-8");
        // 在响应消息（回复消息给用户）时，也将编码方式设置为UTF-8，原理同上；
        response.setCharacterEncoding("UTF-8");
        String respXml = weixinCoreService.weixinMessageHandelCoreService(request, response);
        if (dataProcess.dataIsNull(respXml)){
            logger.error("-------------处理微信消息失败-----------------------");
            return null;
        }else {
            logger.info("----------返回微信消息处理结果-----------------------:"+respXml);
            return respXml;
        }

    }

    @Override
    public String weixinMessageHandelCoreService(HttpServletRequest request,
                                                 HttpServletResponse response) {

        logger.info("------------微信消息开始处理-------------");
        // 返回给微信服务器的消息,默认为null

        String respMessage = null;

        try {

            // 默认返回的文本消息内容
            String respContent = null;
            // xml分析
            // 调用消息工具类MessageUtil解析微信发来的xml格式的消息，解析的结果放在HashMap里；
            Map<String, String> map = weixinMessageUtil.parseXml(request);
            // 发送方账号
            String fromUserName = map.get("FromUserName");
            weixinMessageInfo.setFromUserName(fromUserName);
            System.out.println("fromUserName--->"+fromUserName);
            // 接受方账号（公众号）
            String toUserName = map.get("ToUserName");
            weixinMessageInfo.setToUserName(toUserName);
            System.out.println("toUserName----->"+toUserName);
            // 消息类型
            String msgType = map.get("MsgType");
            weixinMessageInfo.setMessageType(msgType);
            logger.info("fromUserName is:" +fromUserName+" toUserName is:" +toUserName+" msgType is:" +msgType);

            // 默认回复文本消息
            TextMessage textMessage = new TextMessage();
            textMessage.setToUserName(fromUserName);
            textMessage.setFromUserName(toUserName);
            textMessage.setCreateTime(new Date().getTime());
            textMessage.setMsgType(weixinMessageUtil.RESP_MESSAGE_TYPE_TEXT);
            textMessage.setFuncFlag(0);

            // 分析用户发送的消息类型，并作出相应的处理

            // 文本消息
            if (msgType.equals(weixinMessageUtil.REQ_MESSAGE_TYPE_TEXT)){
                respContent = "亲，这是文本消息！";
                textMessage.setContent(respContent);
                respMessage = weixinMessageUtil.textMessageToXml(textMessage);
            }

            // 图片消息
            else if (msgType.equals(weixinMessageUtil.REQ_MESSAGE_TYPE_IMAGE)) {
                respContent = "您发送的是图片消息！";
                textMessage.setContent(respContent);
                respMessage = weixinMessageUtil.textMessageToXml(textMessage);
            }

            // 语音消息
            else if (msgType.equals(weixinMessageUtil.REQ_MESSAGE_TYPE_VOICE)) {
                respContent = "您发送的是语音消息！";
                textMessage.setContent(respContent);
                respMessage = weixinMessageUtil.textMessageToXml(textMessage);
            }

            // 视频消息
            else if (msgType.equals(weixinMessageUtil.REQ_MESSAGE_TYPE_VIDEO)) {
                respContent = "您发送的是视频消息！";
                textMessage.setContent(respContent);
                respMessage = weixinMessageUtil.textMessageToXml(textMessage);
            }

            // 地理位置消息
            else if (msgType.equals(weixinMessageUtil.REQ_MESSAGE_TYPE_LOCATION)) {
                respContent = "您发送的是地理位置消息！";
                textMessage.setContent(respContent);
                respMessage = weixinMessageUtil.textMessageToXml(textMessage);
            }

            // 链接消息
            else if (msgType.equals(weixinMessageUtil.REQ_MESSAGE_TYPE_LINK)) {
                respContent = "您发送的是链接消息！";
                textMessage.setContent(respContent);
                respMessage = weixinMessageUtil.textMessageToXml(textMessage);
            }

            // 事件推送(当用户主动点击菜单，或者扫面二维码等事件)
            else if (msgType.equals(weixinMessageUtil.REQ_MESSAGE_TYPE_EVENT)) {

                // 事件类型
                String  eventType =map.get("Event");
                System.out.println("eventType------>"+eventType);
                // 关注
                if (eventType.equals(weixinMessageUtil.EVENT_TYPE_SUBSCRIBE)){
                    respMessage = weixinMessageModelUtil.followResponseMessageModel(weixinMessageInfo);
                }
                // 取消关注
                else if (eventType.equals(weixinMessageUtil.EVENT_TYPE_UNSUBSCRIBE)) {
                    weixinMessageModelUtil.cancelAttention(fromUserName);
                }
                // 扫描带参数二维码
                else if (eventType.equals(weixinMessageUtil.EVENT_TYPE_SCAN)) {
                    System.out.println("扫描带参数二维码");
                }
                // 上报地理位置
                else if (eventType.equals(weixinMessageUtil.EVENT_TYPE_LOCATION)) {
                    System.out.println("上报地理位置");
                }
                // 自定义菜单（点击菜单拉取消息）
                else if (eventType.equals(weixinMessageUtil.EVENT_TYPE_CLICK)) {

                    // 事件KEY值，与创建自定义菜单时指定的KEY值对应
                    String eventKey=map.get("EventKey");
                    System.out.println("eventKey------->"+eventKey);

                }
                // 自定义菜单（(自定义菜单URl视图)）
                else if (eventType.equals(weixinMessageUtil.EVENT_TYPE_VIEW)) {
                    System.out.println("处理自定义菜单URI视图");
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("系统出错");
            System.err.println("系统出错");
            respMessage = null;
        }
        finally{
            if (null == respMessage) {
                respMessage = weixinMessageModelUtil.systemErrorResponseMessageModel(weixinMessageInfo);
            }
        }

        return respMessage;
    }*/


}
