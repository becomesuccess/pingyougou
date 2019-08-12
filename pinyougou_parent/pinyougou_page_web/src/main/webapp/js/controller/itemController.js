//创建控制层
app.controller("itemController",function ($scope,$http) {
	
	$scope.specificationItems={};
	
	//增减数量的方法
	$scope.addNum=function(x){
		$scope.num+=x;
		if($scope.num<1){
			$scope.num=1;
		}
	}
	
	//向选择集合中存入数据的方法
	
	$scope.addSpecification=function(key,value){
		
		$scope.specificationItems[key]=value;
		searchSku();
	}
	
	//判断当前选项是否是被选中的选项
	
	$scope.selectSpecification=function(key,value){
		
		if($scope.specificationItems[key]==value){
			return true;
		}else{
			return false;
		}
	}
	
	
	//定义默认变量
	$scope.sku={};
	
	$scope.loadSku=function(){
		console.log(skuList[0]);
		$scope.sku=skuList[0];
		$scope.specificationItems= JSON.parse(JSON.stringify($scope.sku.spec)) ;
	}
	
		//匹配spec的方法
	
	matchObject=function(map1,map2){
		
		for(var k in map1){
			if(map1[k]!=map2[k]){
				return false;
			}
		}
		
		for(var k in map2){
			if(map2[k]!=map1[k]){
				return false;
			}
		}
		return true;
	}
	
	
	//查看sku列表的记录能匹配上的
	
	searchSku=function(){
		
		for(var i=0;i<skuList.length;i++){
			if(matchObject($scope.specificationItems,skuList[i].spec)){
				$scope.sku=skuList[i];
				return;
			}
		}
		$scope.sku={id:0,title:'卖完啦',price:0};

	}

		//加入购物车的方法
		
	$scope.addToCart=function(){
		
		//alert("商品的id为："+$scope.sku.id);

		$http.get("http://localhost:9108/cart/addGoodsToCartList.do?itemId="
			+$scope.sku.id+"&num="+$scope.num,{'withCredentials':true}).success(
				function (response) {
					if(response.success){
						location.href="http://localhost:9108/cart.html";//跳转到购物车页面
					}else{
						alert(response.message);
					}
                }
		);
		
	}

	
});
