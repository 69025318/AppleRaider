<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<!-- 内测环境测试新BE支付接口 -->
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>缓存管理页面</title>
</head>
<body>
	<form action="/bankEngine/cache/clearCache" method="post">
		<input type="submit" value="清空BE缓存（请在空闲时候进行操作）"/>
	</form>
</body>
</html>