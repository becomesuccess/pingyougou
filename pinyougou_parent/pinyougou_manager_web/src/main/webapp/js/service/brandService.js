//创建服务层
app.service("brandService",function ($http) {

//查询所有
    this.findAll=function () {
        return $http.get("../brand/findAll.do")
    };

//分页的方式在数据库查询数据
    this.findByPage=function (page,rows) {

        return $http.get("../brand/findByPage.do?page="+page+"&rows="+rows)

    };

//保存品牌，将新增和修改整合在一起

    this.update=function (entity) {
        return $http.post("../brand/update.do",entity)
    };


    this.add=function (entity) {
        return $http.post("../brand/add.do",entity)
    };

//通过id查询

    this.findById=function (id) {
        return  $http.get("../brand/findById.do?id="+id)
    };

//删除商品
    this.dele=function (selectIds) {
        return  $http.get("../brand/delete.do?ids="+selectIds)
    };

// 分页加上条件查询的方式在数据库查询数据

    this.search=function (page,rows,searchEntity) {

        return   $http.post("../brand/search.do?page="+page+"&rows="+rows,searchEntity)
    }

    //查询品牌列表，返回select2需要的数据

    this.selectOptionList=function () {
        return $http.get("../brand/selectOptionList.do");
    }

});
