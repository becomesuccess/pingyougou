 //控制层 
app.controller('goodsController' ,function($scope,$controller,$location,goodsService,uploadService,itemCatService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(){

		var id=$location.search()['id'];

		if(id==null){
			return
		}
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;
				//向富文本编辑器添加内容
				editor.html($scope.entity.goodsDesc.introduction);
				//图片的获取
				$scope.entity.goodsDesc.itemImages=JSON.parse($scope.entity.goodsDesc.itemImages);

				//扩展属性调整
				$scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);

				//规格选项转换
				$scope.entity.goodsDesc.specificationItems =JSON.parse($scope.entity.goodsDesc.specificationItems);

				//转换规格选项中的spec
				for(var i=0;i<$scope.entity.itemList.length;i++){

                    $scope.entity.itemList[i].spec=JSON.parse($scope.entity.itemList[i].spec);
				}
			}
		);
	};

	
	//保存 
	$scope.save=function(){

	    $scope.entity.goodsDesc.introduction=editor.html();
	    var object = null;
	    if($scope.entity.goods.id != null){
	    	object = goodsService.update( $scope.entity)//有id是调用修改方法
		}else {
	    	object = goodsService.add( $scope.entity )//没有id调用增加方法
		}
        object.success(
			function(response){
				if(response.success){
					//重新查询 
		        	alert("保存成功");
					//清空列表
					location.href="goods.html";

				}else{
					alert(response.message);//清空富文本编辑器
				}
			}		
		);				
	};
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

	//上传文件到图片服务器

	$scope.uploadFile=function () {

		uploadService.uploadFile().success(
			function (response) {
				if(response.success){
					$scope.image_entity.url = response.message;//上传成功返回url
				}else {
					alert(response.message)
				}

            }
		).error(
			function () {
                alert("上传发生错误")
            }
		)

    }

    //定义使用的变量

    $scope.entity ={goods:{},goodsDesc:{itemImages:[],specificationItems:[]}};

    //上传图片到显示列表

    $scope.upload_image_entity=function () {

	    $scope.entity.goodsDesc.itemImages.push($scope.image_entity);

    }

    //上传图片到显示列表

    $scope.removeImage=function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index,1);

    }

    //查询一级目录
	$scope.selectItemCat1List=function () {

    	itemCatService.findByParentId(0).success(
    		function (respnose) {
    		$scope.itemCat1List = respnose;
            }
		)
    }

    //查询二级目录
    $scope.$watch("entity.goods.category1Id",function (newValue, oldValue) {
        itemCatService.findByParentId(newValue).success(
            function (respnose) {
                $scope.itemCat2List = respnose;
            }
        )
    });

    //查询三级目录
    $scope.$watch("entity.goods.category2Id",function (newValue, oldValue) {
        itemCatService.findByParentId(newValue).success(
            function (respnose) {
                $scope.itemCat3List = respnose;
            }
        )
    });

    //查询模板id
    $scope.$watch("entity.goods.category3Id",function (newValue, oldValue) {
        itemCatService.findOne(newValue).success(
            function (respnose) {
                $scope.entity.goods.typeTemplateId = respnose.typeId;
            }
        )
    });

    //查询品牌
    $scope.$watch("entity.goods.typeTemplateId",function (newValue, oldValue) {
        typeTemplateService.findOne(newValue).success(
            function (respnose) {
                $scope.typeTemplate = respnose;
                //转换成json
                $scope.typeTemplate.brandIds=JSON.parse($scope.typeTemplate.brandIds);

                //查询扩展属性
				if($location.search()['id'] == null){//如果是有id就是在修改属性，如果没有id就是在增加商品，执行这个语句
                    $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems)
				}

            }
        )

        typeTemplateService.findSpecList(newValue).success(

        	function (reponse) {

                $scope.specList = reponse;
            }

		)

    });

    //定义一个将勾选扩展属性将值写入集合中的方法

	$scope.updateSpecAttribute=function ($event,name,value) {
		var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,"attributeName",name);


		if(object != null){
			//查询到容器中已经存在该属性，直接存入数据

			if ($event.target.checked){
				//此时是钩中选项
                object.attributeValue.push(value)
			}else{
				//此时是取消选中
                object.attributeValue.splice(object.attributeValue.indexOf(value),1);
				if(object.attributeValue.length == 0){
                    $scope.entity.goodsDesc.specificationItems.splice(
                    	$scope.entity.goodsDesc.specificationItems.indexOf(object),1)
				}
			}
		}else{
			//查询到容器中没用该属性，要添加该属性

            $scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]})
		}


    }


    //添加sku列表
    $scope.createItemList=function () {
		//初始化变量
		$scope.entity.ItemList=[{spec:{},price:0,num:999,status:"0",isDefault:"0"}];
		var items=$scope.entity.goodsDesc.specificationItems;

		//向变量中加入数据
		for (var i=0;i<items.length;i++){
            $scope.entity.ItemList=addClumn($scope.entity.ItemList,items[i].attributeName,items[i].attributeValue)
		}
    }

		//增加栏
	addClumn=function (list,clumnName,clumnValue){

		var newList = [];

		for(var i=0;i<list.length;i++){
			var oldRow=list[i];
			for(var j=0;j<clumnValue.length;j++){
				var newRow = JSON.parse(JSON.stringify(list[i]));
				newRow.spec[clumnName]=clumnValue[j];
                newList.push(newRow);
			}
		}
		return newList;
    }

    //定义一个与状态相关的变量
	$scope.goodStatus=["未审核","已审核","审核未通过","已关闭"];


	$scope.itemCatList=[];
	//查询分级目录
	$scope.findItemCatList=function () {
        itemCatService.findAll().success(
        	function (response) {
        		for (var i=0;i<response.length;i++){
                    $scope.itemCatList[response[i].id]=response[i].name;
				}
            }
		)

    }
	
    //显示规格选项勾选情况的方法
	$scope.findChecked=function (specName,optionName) {

		var items =$scope.entity.goodsDesc.specificationItems
		var list = $scope.searchObjectByKey(items,"attributeName",specName);

		if (list != null){
			 if(list.attributeValue.indexOf(optionName)>=0){
			 	return true;
			 }else {
			 	return false;
			 }
		}else{
			return false;
		}
    }
    
});	
