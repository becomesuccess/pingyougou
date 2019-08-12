package com.pinyougou.pay.service;

import java.util.Map;

public interface WeixinPayService {

    //生成支付连接
    public Map createNative(String out_trade_no,String total_fee);


    //查询支付状态
    public Map queryPayStatus(String out_trade_no);


    //关闭订单，从微信
    public Map closePay(String out_trade_no);

}
