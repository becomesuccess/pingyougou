app.service("seckillGoodsSercie",function ($http) {

    /*
    查询秒杀商品列表
    * */
    this.findList=function () {
        return $http.get("seckillGoods/findList.do");

    }

    /*
    * 查询商品详细页
     * * */
    
    this.findOne=function (id) {
        return $http.get("seckillGoods/findOndFromRedis.do?id="+id);
    }

    /*
    * 提交订单
    * */

    this.submitOrder=function(seckillId){
        return $http.get('seckillOrder/submitOrder.do?seckillId='+seckillId);
    }

});