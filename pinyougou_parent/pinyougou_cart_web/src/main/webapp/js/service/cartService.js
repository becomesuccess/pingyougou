app.service('cartService',function($http){
//购物车列表
    this.findCartList=function(){
        return $http.get('cart/findCartList.do');
    }
    
    //添加商品
    this.addGoodsToCartList=function (itemId,num) {
        return $http.get("cart/addGoodsToCartList.do?itemId="+itemId+"&num="+num);
    }


    //计算合计数的方法

    this.sum=function (cartList) {

        var totalValue={totalNum:0,totalFee:0};

        for (var i=0;i<cartList.length;i++){
            var cart=cartList[i]
            for (var j=0;j<cart.tbOrderItemList.length;j++){
                totalValue.totalNum+=cart.tbOrderItemList[j].num;
                totalValue.totalFee+=cart.tbOrderItemList[j].totalFee;
            }
        }

        return totalValue;

    }

    //查询用户地址列表的方法

    this.findAddressList=function () {
        return $http.get("address/findAddressListByUserId.do")
    }

    //设置提交订单的方法
    this.submitOrder=function (order) {
        return $http.post("order/add.do",order);
    }
    
});