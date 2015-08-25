<%@page import="com.htffund.etrade.bankengine.model.ResendSmsResponse"%>
<%@page import="com.htffund.etrade.bankengine.model.ResendSmsRequest"%>
<%@page
	import="com.htffund.etrade.bankengine.biz.service.impl.BankEngineServiceImpl"%>
<%@page import="com.htffund.etrade.bankengine.service.BankEngineService"%>
<%@page import="com.htffund.etrade.bankengine.biz.util.SpringUtils"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<!-- 内测环境测试新BE, 执行重发短信指令页面-->
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>内测环境测试新BE, 执行重发短信指令页面</title>
</head>
<body>
	<%
		ResendSmsRequest resendSmsRequest = new ResendSmsRequest(
				request.getParameter("bankNo"),
				request.getParameter("serialNo"));

		BankEngineService bankEngineService = SpringUtils
				.getBean(BankEngineServiceImpl.class);
		ResendSmsResponse resendSmsResponse = bankEngineService.resendSms(resendSmsRequest);
	%>
	输入参数是：<%=resendSmsRequest%><br> 返回结果是：<%=resendSmsResponse%>
</body>
</html>