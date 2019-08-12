//创建控制层
app.controller("brandController",function ($scope,$http,$controller,brandService) {

    $controller("baseController",{$scope:$scope});

    //查询列表
    $scope.findAll=function () {
        brandService.findAll().success(
            function (response) {
                $scope.list=response;
            });
    };

    //分页的方式在数据库查询数据
    $scope.fingByPage=function (page,rows) {
        brandService.findByPage(page,rows).success(

            function (response) {
                $scope.list=response.rows;
                $scope.paginationConf.totalItems=response.total;

            }

        )
    };


    //保存品牌，将新增和修改整合在一起
    $scope.save=function () {

        //定义一个方法名

        var object=null;

        if ($scope.entity.id != null){
            object =brandService.update($scope.entity);
        }else {
            object=brandService.add($scope.entity);
        }

        object.success(

            function (response) {

                if(response.success){
                    $scope.reloadList();//重新加载
                }else {
                    alert(response.message)
                }

            }

        )

    };


    /*通过id查询*/
    $scope.findById=function (id) {

        brandService.findById(id).success(

            function (response) {
                $scope.entity=response;
            }

        )

    };


    //删除品牌的方法

    $scope.dele=function(){

        brandService.dele($scope.selectIds).success(

            function (response) {

                if (response.success){
                    $scope.reloadList();//重新加载
                }else {
                    alert(response.message);
                }

            }

        )

    }


    $scope.searchEntity={};

    //分页加上条件查询的方式在数据库查询数据
    $scope.search=function (page,rows) {
        brandService.search(page,rows,$scope.searchEntity).success(

            function (response) {
                $scope.list=response.rows;
                $scope.paginationConf.totalItems=response.total;
            }
        )
    }

});
