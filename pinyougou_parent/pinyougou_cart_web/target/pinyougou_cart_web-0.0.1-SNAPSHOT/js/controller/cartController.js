app.controller('cartController',function($scope,cartService){
//查询购物车列表
    $scope.findCartList=function(){
        cartService.findCartList().success(
            function(response){
                $scope.cartList=response;
                $scope.totalValue = cartService.sum($scope.cartList);
            }
        );
    }

    //添加商品的方法
    $scope.addGoodsToCartList=function (itemId, num) {
        cartService.addGoodsToCartList(itemId,num).success(
            function (response) {
                if(response.success){
                    $scope.findCartList();
                }else {
                    alert(response.message);
                }
            }
        )
    }

    //查询用户的收货地址列表

    $scope.findAddressList=function () {
        cartService.findAddressList().success(
            function (response) {
                $scope.addressList=response;
                for (var i=0;i<$scope.addressList.length;i++){
                    if ($scope.addressList[i].isDefault=="1"){
                            $scope.address=$scope.addressList[i];
                            break;
                    }

                }
            }
        )
    }

    //选择地址的方法
    $scope.selectAddress=function (address) {
        $scope.address=address;
    }

    //判断是否被选中的方法
    $scope.isSelected=function (address) {
        if($scope.address==address){
                return true;
        }else{
            return false;
        }
    }

    //定义一个支付方式的变量
    $scope.order={paymentType:'1'};

    //定义一个选择支付方式的方法
    $scope.selectedpaymentType=function (type) {
        $scope.order.paymentType=type;
    }

    //提交订单
    $scope.submitOrder=function () {
        $scope.order.receiverAreaName=$scope.address.address;//设置地址
        $scope.order.receiverMobile=$scope.address.mobile;//设置联系人电话
        $scope.order.receiver=$scope.address.contact;//设置联系人

        cartService.submitOrder( $scope.order).success(
            function (response) {
                if (response.success){

                    if ($scope.order.paymentType==1){//如果是微信支付
                            location.href="pay.html";
                    }else{//如果是货到付款
                        location.href="paysuccess.html";
                    }
                }else{
                    alert(response.message);
                }
            }
        )
    }

});