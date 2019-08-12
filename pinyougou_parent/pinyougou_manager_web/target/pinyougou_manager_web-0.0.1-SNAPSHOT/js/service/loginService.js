app.service('loginService',function($http){
//读取登录人名称
    this.loginNameDo=function(){
        return $http.get('../login/showName.do');
    }
});