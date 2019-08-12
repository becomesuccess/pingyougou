package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.pay.service.WeixinPayService;
import org.springframework.beans.factory.annotation.Value;
import util.HttpClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class WeixinPayServiceImpl implements WeixinPayService {

    @Value("${appid}")
    private String appid;

    @Value("${partner}")
    private String partner;

    @Value("${partnerkey}")
    private String partnerkey;

    @Override
    public Map createNative(String out_trade_no, String total_fee) {
        //1. 封装数据
        Map param = new HashMap();
        param.put("appid",appid);//存入公众号id
        param.put("mch_id",partner);//存入商户号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//存入随机字符串
        param.put("body","品优购网上商城");//商品的简单描述
        param.put("out_trade_no",out_trade_no);//商品订单号
        param.put("total_fee",total_fee);//金额
        param.put("spbill_create_ip","127.0.0.1");//终端地址
        param.put("notify_url","http://www.baidu.com");//回调url
        param.put("trade_type","NATIVE");//交易类型
        try {
            //2. 生成要传递的数据
            String paramXml = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println(paramXml);

            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(paramXml);
            httpClient.post();
            //3. 获得结果
            String content = httpClient.getContent();
            System.out.println(content);
            Map<String, String> mapResult = WXPayUtil.xmlToMap(content);
            Map map = new HashMap();
            map.put("code_url",mapResult.get("code_url"));//存入支付连接
            map.put("out_trade_no",out_trade_no);//存入订单号
            map.put("total_fee",total_fee);//存入订单金额

            return map;

        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }
    }

    @Override
    public Map queryPayStatus(String out_trade_no) {

        Map param = new HashMap();
        //1。 存入数据
        param.put("appid",appid);//存入公众号id
        param.put("mch_id",partner);//商户号
        param.put("out_trade_no",out_trade_no);//订单号
        param.put("nonce_str",WXPayUtil.generateNonceStr());//生成随机字符串

        try {
           //生成带签名的xml
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);

            //发送请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParam);
            httpClient.post();

            //获得结果
            String content = httpClient.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(content);

            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Map closePay(String out_trade_no) {

        Map param = new HashMap();
        //1、存入数据
        param.put("appid",appid);//存入公众号id
        param.put("mch_id",partner);//商户号
        param.put("out_trade_no",out_trade_no);//订单号
        param.put("nonce_str",WXPayUtil.generateNonceStr());//生成随机字符串

        try {
            //生成带签名的xml
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);

            //发送请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/closeorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParam);
            httpClient.post();

            //获得结果
            String content = httpClient.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(content);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
