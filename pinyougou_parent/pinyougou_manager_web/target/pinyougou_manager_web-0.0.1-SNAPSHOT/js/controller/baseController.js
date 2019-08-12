app.controller("baseController",function ($scope,brandService) {

    //分页控件配置
    $scope.paginationConf = {
        currentPage: 1,
        totalItems: 10,
        itemsPerPage: 10,
        perPageOptions: [10, 20, 30, 40, 50],
        onChange: function(){
            $scope.reloadList();//重新加载
        }
    };

    $scope.reloadList=function () {
        $scope.search( $scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage)
    }

    //先获取要删除的id数组

    $scope.selectIds = [];

    //获取id数组的方法

    $scope.updateSelections=function ($event,id) {
        if($event.target.checked){
            $scope.selectIds.push(id);
        }else {
            var index = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(index,1);
        }

    };
    
    //将json转换成易读的字符串
    
    $scope.jsonToString=function (jsonstring,key) {
        var json=JSON.parse(jsonstring);
        var string="";
        for (var i=0;i<json.length;i++){
            if(i>0){
                string +=",";
            }
            string +=(json[i][key]);
        }
        return string;
    }
});