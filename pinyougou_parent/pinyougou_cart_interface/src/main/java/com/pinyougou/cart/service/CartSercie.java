package com.pinyougou.cart.service;

import com.pinyougou.pojogroup.Cart;

import java.util.List;

public interface CartSercie {

    //向购物车中添加商品
    public List<Cart>  addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num);


    //从rdis中查询购物车列表

    public List<Cart> findCartListFormRedis(String username);


    //向redis中添加购物车
    public void saveCartListToRedis(List<Cart> cartList,String username);


    //将cookie中的购物车和redis中购物车合并的方法
    public List<Cart> mergeCartList(List<Cart> cartList1,List<Cart> cartList2);

}
