<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<!-- 内测环境测试新BE重发短信接口 -->
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>内测环境测试新BE重发短信接口</title>
</head>
<body>
	<form action="/bankuat/jsp/preProductTest/testResendSms.jsp">
		<table width="800px" cellpadding="2px" cellspacing="0px" border="1">
			<tr>
				<td style="width:20%">bankNo:</td><td><input style="width:400px" type="text" name="bankNo" value="" /></td>
			</tr>
			<tr>
				<td style="width:20%">serialNo:</td><td><input style="width:400px" type="text" name="serialNo" value="" /></td>
			</tr>
			
			<tr>
				<td colspan="2"><input type="submit" value="提交"></td>
			</tr>
		</table>
	</form>
</body>
</html>