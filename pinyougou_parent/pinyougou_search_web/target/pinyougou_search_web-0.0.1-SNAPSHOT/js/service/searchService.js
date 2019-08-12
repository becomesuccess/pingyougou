app.service("searchService",function ($http) {

    this.search=function (searchMap) {
        return $http.post("itemSearchController/itemSearch.do",searchMap)
    }

});