package com.pinyougou.user.cart.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartSercie;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
@Service
public class CartServiceImpl implements CartSercie {


    @Autowired
    private TbItemMapper itemMapper;


    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {

        //1 根据商品的id查询出商品的具体信息

        TbItem item = itemMapper.selectByPrimaryKey(itemId);

        if (item==null){
            throw new RuntimeException("商品不存在！");
        }

        if (!"1".equals(item.getStatus())){
            throw new RuntimeException("商品不合法！");
        }
        //2 根据商品的具体信息查询出商家的id
         String sellerId = item.getSellerId();
        //3 根据商家的id查询传入的购物车列表中是否有该商家的购物车
        Cart cart = searchCartBySellerId(cartList, sellerId);

        if (cart == null){//4 如果购物车列表中不存在该商家的购物车
            //4.1 创建该商家的购物车对象
            Cart newCart = new Cart();
            newCart.setSellerId(sellerId);
            newCart.setSellerName(item.getSeller());

            //创建商品订单对象
            TbOrderItem orderItem = createOrderItem(item, num);
            List orderItemList = new ArrayList();
            orderItemList.add(orderItem);

            //将集合对象存入购物车对象中。
            newCart.setTbOrderItemList(orderItemList);

            //4.2 在购物车中加入要添加的商品
            cartList.add(newCart);

        }else { //5 购物车列表中存在该商家的购物车对象

            TbOrderItem orderItem = searchOrderItemByItemId(cart, itemId);

            if (orderItem==null){ //5.1此时又分为两种情况，第一种是该商家的购物车中不存在该商品
                //5.2 在该商家的购物中加入该商品
               orderItem = createOrderItem(item,num);
               cart.getTbOrderItemList().add(orderItem);

            }else {
                //5.3如果该商家的购物车中已经存在了该商品，那么就对该商品的数量进行改变
                orderItem.setNum(orderItem.getNum()+num);
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()) );

                //如果该购物明细的数量小于等于0的时候就移除该购物明细
                if (orderItem.getNum()<=0){
                    cart.getTbOrderItemList().remove(orderItem);
                }

                //如果该商家的购物车中的购物明细为0
                if (cart.getTbOrderItemList().size()==0){
                    cartList.remove(cart);
                }

            }

        }


         return cartList;
    }

    @Autowired
    private RedisTemplate redisTemplate;

    //从redis中查找购物车对象
    @Override
    public List<Cart> findCartListFormRedis(String username) {

        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        System.out.println("find cartList from redis:"+username);
        if (cartList==null){
            cartList=new ArrayList<>();
        }

        return cartList;
    }

    //向redis中添加购物车列表对象
    @Override
    public void saveCartListToRedis(List<Cart> cartList, String username) {

        redisTemplate.boundHashOps("cartList").put(username,cartList);
        System.out.println("put cartList to redise:"+username);
    }



    //将查询购购物车列表中是否有该商家的购物车的方法抽取出来

    private Cart searchCartBySellerId(List<Cart> cartList,String sellerId){

        for (Cart cart : cartList) {
            if (cart.getSellerId().equals(sellerId)){
                return cart;
            }
        }

        return null;

    }


    //根据sku查询购物车对象中是否存在该明细

    private TbOrderItem searchOrderItemByItemId(Cart cart,Long itemId){

        List<TbOrderItem> orderItemList = cart.getTbOrderItemList();
        for (TbOrderItem orderItem : orderItemList) {

            if (orderItem.getItemId().longValue()==itemId.longValue()){
                return orderItem;
            }

        }

        return null;

    }


   //添加购物明细的方法

    private TbOrderItem createOrderItem(TbItem item,Integer num){

        if (num == 0){
            throw new RuntimeException("数量非法！");
        }

        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());//存入商品id
        orderItem.setNum(num);//存入商品数量
        orderItem.setPrice(item.getPrice());//存入商品价格
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));//存入总价
        orderItem.setTitle(item.getTitle());//存入商品标题
        orderItem.setPicPath(item.getImage());//存入图片地址
        orderItem.setItemId(item.getId());//
        return orderItem;
    }

    //合并cookie中的购物车和redis中的购物车
    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {

        for (int i=0;i<cartList2.size();i++){
            Cart cart = cartList2.get(i);
            for (int j=0;j<cart.getTbOrderItemList().size();j++){
                 cartList1 = addGoodsToCartList(cartList1, cart.getTbOrderItemList().get(j).getItemId(), cart.getTbOrderItemList().get(j).getNum());
            }
        }
        return cartList1;
    }
}
