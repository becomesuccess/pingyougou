package com.pinyougou.cart.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartSercie;
import com.pinyougou.pojogroup.Cart;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.CookieUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {


    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;


    @Reference(timeout = 6000)
    private CartSercie cartSercie;

    //查询cookie中的购物车列表方法
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(){

        String name = SecurityContextHolder.getContext().getAuthentication().getName();

        //从cookie中查询

        String cartListString = CookieUtil.getCookieValue(request, "CartList", "UTF-8");

        if (cartListString==null||cartListString.equals("")){
            cartListString="[]";
        }
        List<Cart> carts = JSON.parseArray(cartListString, Cart.class);

        if (!"anonymousUser".equals(name)){//如果用户已经登陆了

            //当用户登陆后会对购物车列表进行合并
            //1、从redis中查询数据
            List<Cart> cartList = cartSercie.findCartListFormRedis(name);

            //2、从cookie中查询数据，查询coolie中是否存在数据
            if(carts==null){//如果cookie中不存在数据，就不用合并数据，直接返回redis中的数据
                return cartList;
            }else {//如果cookie中存在数据

                //合并cookie中和redis中的数据
                List<Cart> cartListPlus = cartSercie.mergeCartList(cartList, carts);
                System.out.println("run mergeCartList");
                //将合并后的数据存入redis中
                cartSercie.saveCartListToRedis(cartListPlus,name);
                //合并后就清除cookie中数据
                util.CookieUtil.deleteCookie(request,response,"CartList");
                System.out.println("clear cookie");
                //返回数据

                return  cartListPlus;
            }

        }else {//如果用户没有登陆
            System.out.println("select from cookie");
            return carts;
        }


    }



    //向购物车列表中加入商品方法
    @RequestMapping("/addGoodsToCartList")
    public Result addGoodsToCartList(Long itemId, Integer num){

        response.setHeader("Access-Control-Allow-Origin","http://localhost:9105");
        response.setHeader("Access-Control-Allow-Credentials","true");

        try {
            //调用方法查询cookie中的购物车列表
            List<Cart> cartList = findCartList();
            //调用service方法向购物车列表中加入商品
            cartList= cartSercie.addGoodsToCartList(cartList, itemId, num);

            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            if (!"anonymousUser".equals(name)){//如果用户已经登陆了
                //将数据添加到redis中
                cartSercie.saveCartListToRedis(cartList,name);

            }else {//如果用户没有登陆,将对象存入cookie中
                //将对象转化成json字符串对象
                String jsonString = JSON.toJSONString(cartList);
                //将信息存入cookie中
                CookieUtil.setCookie(request,response,"CartList",jsonString,3600*24,"UTF-8");
                System.out.println("add to cookie");
            }
            return new Result(true,"添入购物车成功！");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"填入购物车失败！");
        }
    }

}
