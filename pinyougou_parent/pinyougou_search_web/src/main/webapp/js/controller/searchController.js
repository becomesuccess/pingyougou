app.controller("searchController",function ($scope, $http,$location,searchService) {

    //初始化搜索条件的格式

    $scope.searchMap={"keywords":"","category":"","brand":"","spec":{},"price":"","pageNum":1,"pageSize":20,"sortField":"","sort":""};

    //搜索
    $scope.search=function () {

        $scope.searchMap.pageNum = parseInt($scope.searchMap.pageNum);//将当前页面转换成数字，确保传过去的是一个数字
        searchService.search($scope.searchMap).success(
            function (response) {
                $scope.result=response;
                buildPageLabel();
            }
        )

    };



    buildPageLabel=function () {
        $scope.pageArray=[];
        var firstPage = 1;
        var sumPage=$scope.result.totalPages;
        var lastPage = sumPage;
        $scope.firstDos=true;
        $scope.lastDos=true;

        if ($scope.result.totalPages>5){//当总页数大于5页时
            if($scope.searchMap.pageNum<=3){//当前页码小于等于3的情况
                for (var i=1;i<=5;i++){
                    $scope.pageArray.push(i);
                }
                $scope.firstDos=false;
            }else if (($scope.searchMap.pageNum+2)>($scope.result.totalPages)){
                firstPage=$scope.result.totalPages-4;
                for (var i=firstPage;i<=lastPage;i++){
                    $scope.pageArray.push(i);
                }
                $scope.lastDos=false;
            }else {//以当前页为中心显示5页

                firstPage=$scope.searchMap.pageNum-2;
                lastPage=$scope.searchMap.pageNum+2;

                for (var i=firstPage;i<=lastPage;i++){
                    $scope.pageArray.push(i);
                }
            }
        }else {//当总页数小于等于5页时
            for (var i=1;i<=lastPage;i++){
                $scope.pageArray.push(i);
            }
            $scope.firstDos=false;
            $scope.lastDos=false;
        }
    };


    //定义一个向addSearchMap中添加条件的方法

    $scope.addSearchMap=function (key,value) {

        if(key == "brand"||key=="category" ||key == "price"){

            $scope.searchMap[key]=value;

        }else{
            $scope.searchMap.spec[key]=value;
        }

        $scope.search();

    }


    //定义一个向addSearchMap移除条件的方法

    $scope.removeSearchMap=function (key) {

        if(key == "brand"||key=="category" ||key == "price"){

            $scope.searchMap[key]="";

        }else{
           delete $scope.searchMap.spec[key];
        }
        $scope.search();

    }


    //页码查询

    $scope.findByPage=function (pageNumber) {
        if (pageNumber<1||pageNumber>$scope.result.totalPages){
            return;
        }

        $scope.searchMap.pageNum=pageNumber;

        $scope.search();
    }

    //判断当前是否为第一页

    $scope.isTopPage=function () {

        if ($scope.searchMap.pageNum == 1){
            return true;
        }else {
            return false
        }
    }

    //判断当前页是否是最后一页

    $scope.isEndPage=function () {

        if ($scope.searchMap.pageNum==$scope.result.totalPages){
                return true;
        }else {
            return false;
        }

    }

    //添加排序的功能
    $scope.sortSearch=function (sort, sortField) {
        $scope.searchMap.sort=sort;
        $scope.searchMap.sortField=sortField;
        $scope.search();
    }


    //隐藏品牌的功能
    $scope.keywordsIsBrand=function () {

        for (var i=0;i<$scope.result.brandList.length;i++){
            if ($scope.searchMap.keywords.indexOf($scope.result.brandList[i].text)>=0){
                return true;
            }
        }
        return false;
    }
    
    //接收首页传过来的参数
    
    $scope.loadKeywords=function () {
        $scope.searchMap.keywords=$location.search()['keywords'];
        $scope.search();
    }
});