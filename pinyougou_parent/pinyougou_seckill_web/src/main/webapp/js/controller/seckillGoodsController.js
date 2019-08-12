app.controller("seckillGoodsController",function ($scope,$location,seckillGoodsSercie,$interval) {

    //查询秒杀商品列表
    $scope.findList=function () {
        seckillGoodsSercie.findList().success(
            function (response) {
                $scope.list=response;
            }
        )
    }

    /*
    *  查询商品详细页
    * */
    $scope.findOne=function () {
      var  id = $location.search()["id"];
        seckillGoodsSercie.findOne(id).success(
            function (response) {
                $scope.entity=response;
                var allsecond=Math.floor((((new Date(response.endTime)).getTime())-new Date().getTime())/1000);
                time=$interval(function () {
                    allsecond=allsecond-1;
                   $scope.timeString = convertTimeString(allsecond);
                    if(allsecond<=0){
                        $interval.cancel(time);
                    }
                },1000);
            }
        )

    };



    //转换倒计时的方法
    convertTimeString=function (allsecond) {

        var days=Math.floor(allsecond/(60*60*24));//天
        var hours=Math.floor((allsecond-days*(60*60*24))/(60*60));//小时
        var minutes=Math.floor((allsecond-days*60*60*24-hours*60*60)/60);//分
        var second=allsecond-days*60*60*24-hours*60*60-minutes*60;//秒

        var timeStr="" ;

        if(days>0){
            timeStr =days+"天：";
        }
        return timeStr+hours+"时："+minutes+"分："+second+"秒";
    }

    /*
    * 提交订单
    * */
    $scope.submitOrder=function () {

        seckillGoodsSercie.submitOrder($scope.entity.id).success(
            function (response) {
                if (response.success){
                    alert("抢购成功，请在五分钟内完成支付！");
                    location.href="pay.html";
                }else {
                    alert(response.message);
                }
            }
        )

    }

});