package com.pinyougou.cart.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeixinPayService;

import com.pinyougou.pojo.TbPayLog;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.IdWorker;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private WeixinPayService weixinPayService;

    @Reference
    private OrderService orderService;

    @RequestMapping("createNative")
    public Map createNative(){

        //获取当前登陆的用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        TbPayLog payLog = orderService.searchPayLogFromRedis(username);
        if (payLog!=null){
            IdWorker idWorker = new IdWorker();
            Map resutMap = weixinPayService.createNative(payLog.getOutTradeNo(), payLog.getTotalFee()+"");
            System.out.println(resutMap);
            return resutMap;
        }else {
            return  new HashMap();
        }

    }


    @RequestMapping("queryPayStatus")
    public Result queryPayStatus(String out_trade_no){

        Result result = null;

        int x = 0;
        while (true){
            //查询
            Map<String,String> map = weixinPayService.queryPayStatus(out_trade_no);

            // 此时分为三种情况
            //1. 当查询到的map为空
            if (map == null){
                result = new Result(false,"支付出错");
                break;
            }
            if ("SUCCESS".equals(map.get("trade_state"))){
                result = new Result(true,"支付成功");
                break;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            x++;
            if (x>100){
                result=new Result(false,"二维码超时");
                break;
            }
        }
        return result;

    }


}
