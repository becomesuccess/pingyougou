package com.pinyougou.seckill.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import entity.Result;
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
    private SeckillOrderService seckillOrderService;

    @RequestMapping("createNative")
    public Map createNative(){

        //获取当前登陆的用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        TbSeckillOrder seckillOrder = seckillOrderService.searchOrderFromRedisByUserId(username);
        System.out.println(seckillOrder.getMoney());
        if (seckillOrder!=null){
            Map resutMap = weixinPayService.createNative(seckillOrder.getId()+"", (long)(seckillOrder.getMoney().doubleValue()*100)+"");
            System.out.println(resutMap);
            return resutMap;
        }else {
            return  new HashMap();
        }

    }


    @RequestMapping("queryPayStatus")
    public Result queryPayStatus(String out_trade_no){

        Result result = null;

        //获取当前登陆的用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

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
                //保存订单
                seckillOrderService.saveOrderFromRedisToDb(username,Long.valueOf(out_trade_no),map.get("transaction_id"));
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
                Map closeMap = weixinPayService.closePay(out_trade_no);
                if ("FAIL".equals(closeMap.get("return_code"))){//取消订单返回结果为失败
                    if ("ORDERPAID".equals(closeMap.get("err_code"))){//错误代码为已支付
                        //此时执行保存订单操作
                        seckillOrderService.saveOrderFromRedisToDb(username,Long.valueOf(out_trade_no),map.get("transaction_id"));
                    }else {
                        result=new Result(false,(String) closeMap.get("err_code"));
                    }
                }else {//取消订单成功
                    System.out.println("over time delete order");
                    seckillOrderService.deleteOrderFromRedis(username,Long.valueOf(out_trade_no));
                }
                break;
            }
        }
        return result;

    }


}
