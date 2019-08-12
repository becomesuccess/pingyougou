 //控制层 
app.controller('userController' ,function($scope,$controller   ,userService){	

	//用户注册的方法
	$scope.reg=function () {

		if($scope.entity.password != $scope.password){
			alert("两次输入的密码不一致！");
			return;
		}

		userService.add($scope.entity,$scope.code).success(
			function (response) {
				alert(response.message);
            }
		)

    }

    //发送验证码的方法

	$scope.sendCode=function () {

		if ($scope.entity.phone==null||$scope.entity.phone==""){
			alert("手机号码不能为空");
		}

		userService.sendSms($scope.entity.phone).success(
			function (response) {
				alert(response.message)
            }
		)
    }
    
});	
