<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="com.htffund.etrade.bankengine.model.PayResponse"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.htffund.etrade.bankengine.model.PayRequest"%>
<%@page import="com.htffund.etrade.bankengine.biz.service.impl.BankEngineServiceImpl"%>
<%@page import="com.htffund.etrade.bankengine.service.BankEngineService"%>
<%@page import="com.htffund.etrade.bankengine.biz.util.SpringUtils"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<!-- 内测环境测试新BE, 执行支付B2C到银行的连接-->
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>内测环境测试新BE, 执行支付B2C到银行的连接</title>
<script type="text/javascript">
	function confirm(){
		var myForm = document.getElementById("myForm");
		myForm.action = document.getElementById("redUrl").value;
		myForm.method = document.getElementById("commandType").value;
		myForm.innerHTML = document.getElementById("formBean").value;
		myForm.submit();
	}
  </script>
</head>
<body>
  	<table>
		<tr>
			<td>redUrl:</td>
			<td><input type="text" id="redUrl" vlaue="" style="width:400px"/></td>
		</tr>
		<tr>
			<td>commandType:</td>
			<td><input type="text" id="commandType" vlaue="POST"/></td>
		</tr>
		<tr>
			<td>formBean:</td>
			<td><textarea rows="10" cols="80" id="formBean"></textarea>
			</td>
		</tr>
	</table>
	<form id="myForm" action="" method="">
	</form>

	<input type="button" value="提交" onclick="confirm()" />
 </body>
</html>