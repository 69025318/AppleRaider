<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>配置修改</title>
</head>
<body>
	<form action="/bank/bankEngine/config/changeConfig" method="post">
		<p>1代表验证，0代表不验证</p><input type="text" id="isOpen" name="isOpen" value="" size=10>
		<input type="submit" value="更改设置"/>
	</form>
</body>
</html>