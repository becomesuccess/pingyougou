app.controller("contentController",function ($scope,$http,contentService) {

    $scope.contentList=[];//广告集合

    //根据广告的分类来查询目录
    $scope.findByCategoryId=function (categoryId) {

        contentService.findByCategoryId(categoryId).success(
            function (response) {
                $scope.contentList[categoryId]=response;

            }
        )
    }

    //传递搜索的参数

    $scope.search=function () {
        location.href="http://localhost:9104#?keywords="+$scope.keywords;
    }

});
