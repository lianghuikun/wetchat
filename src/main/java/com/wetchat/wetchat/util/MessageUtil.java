package com.wetchat.wetchat.util;

import com.alibaba.fastjson.JSON;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.wetchat.wetchat.entity.TextMessage;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 转换工具类
 */
public class MessageUtil {
    public static void main(String[] args) throws Exception {
        String str = "<xml>\n" +
                "<ToUserName><![CDATA[gh_866835093fea]]></ToUserName>\n" +
                "<FromUserName><![CDATA[ogdotwSc_MmEEsJs9-ABZ1QL_4r4]]></FromUserName>\n" +
                "<CreateTime>1478317060</CreateTime>\n" +
                "<MsgType><![CDATA[text]]></MsgType>\n" +
                "<Content><![CDATA[你好]]></Content>\n" +
                "<MsgId>6349323426230210995</MsgId>\n" +
                "</xml>";
        Map<String, String> pool = parseXml(str);
        System.out.println("-------->: " + JSON.toJSONString(pool));
        String json = JSON.toJSONString(pool);
        TextMessage temp = JSON.parseObject(json, TextMessage.class);
        System.out.println("---->" + JSON.toJSONString(temp));

        TextMessage textMessage = new TextMessage();
        textMessage.setToUserName("coco");
        textMessage.setFromUserName("123");
        textMessage.setCreateTime(123L);
        textMessage.setMsgType("text");
        textMessage.setMsgId(12L);

        String s = textMessageToXml(textMessage);
        System.out.println("--text->xml:" + s);
    }

    /**
     * 字符串xml转map
     *
     * @param str
     * @return
     * @throws Exception
     */
    public static Map<String, String> parseXml(String str) throws Exception {
        Map<String, String> pool = new HashMap<>();
        SAXReader reader = new SAXReader();
        Document document = reader.read(new ByteArrayInputStream(str.getBytes("UTF-8")));
        // 得到xml根元素
        Element rootElement = document.getRootElement();
        // 得到根元素的所有子节点
        List<Element> elementList = rootElement.elements();
        for (Element element : elementList) {
            pool.put(element.getName(), element.getText());
        }
        return pool;
    }

    /**
     * 从请求里获取xml输入流
     *
     * @param request
     * @return
     * @throws Exception
     */
    public static Map<String, String> parseXml(HttpServletRequest request) throws Exception {
        Map<String, String> pool = new HashMap<>();
        SAXReader reader = new SAXReader();
        InputStream inputStream = request.getInputStream();
        Document document = reader.read(inputStream);
        // 得到xml根元素
        Element rootElement = document.getRootElement();
        // 得到根元素的所有子节点
        List<Element> elementList = rootElement.elements();
        for (Element element : elementList) {
            pool.put(element.getName(), element.getText());
        }
        return pool;
    }

    /**
     * 文本转xml
     *
     * @param textMessage
     * @return
     */
    public static String textMessageToXml(TextMessage textMessage) {
        xstream.alias("xml", textMessage.getClass());
        return xstream.toXML(textMessage);
    }


    /**
     * 扩展xstream，使其支持CDATA块
     *
     * @date 2013-05-19
     */
    private static XStream xstream = new XStream(new XppDriver() {
        public HierarchicalStreamWriter createWriter(Writer out) {
            return new PrettyPrintWriter(out) {
                // 对所有xml节点的转换都增加CDATA标记
                boolean cdata = true;

                @SuppressWarnings("unchecked")
                public void startNode(String name, Class clazz) {
                    super.startNode(name, clazz);
                }

                protected void writeText(QuickWriter writer, String text) {
                    if (cdata) {
                        writer.write("<![CDATA[");
                        writer.write(text);
                        writer.write("]]>");
                    } else {
                        writer.write(text);
                    }
                }
            };
        }
    });
}
