app.service("contentService",function($http) {

    this.findByCategoryId=function (CategoryId) {
        return $http.get("content/findByCategoryId.do?categoryId="+CategoryId);
    }
});