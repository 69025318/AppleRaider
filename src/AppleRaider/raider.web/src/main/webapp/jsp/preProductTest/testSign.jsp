<%@page import="com.htffund.etrade.bankengine.model.SignResponse"%>
<%@page import="com.htffund.etrade.bankengine.model.SignRequest"%>
<%@page
	import="com.htffund.etrade.bankengine.biz.service.impl.BankEngineServiceImpl"%>
<%@page import="com.htffund.etrade.bankengine.service.BankEngineService"%>
<%@page import="com.htffund.etrade.bankengine.biz.util.SpringUtils"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<!-- 内测环境测试新BE, 执行签约指令页面-->
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>内测环境测试新BE, 执行签约指令页面</title>
</head>
<body>
	<%
		SignRequest signRequest = new SignRequest();
		signRequest.setBankNo(request.getParameter("bankNo"));
		signRequest.setAuthCode(request.getParameter("authCode"));
		signRequest.setAccountNo(request.getParameter("accountNo"));
		signRequest.setAccountName(request.getParameter("accountName"));
		signRequest.setIdType(request.getParameter("idType"));
		signRequest.setIdNo(request.getParameter("idNo"));
		signRequest.setVerifySerialNo(request
				.getParameter("verifySerialNo"));

		BankEngineService bankEngineService = SpringUtils
				.getBean(BankEngineServiceImpl.class);
		SignResponse signResponse = bankEngineService.sign(signRequest);
	%>
	输入参数是：<%=signRequest%><br> 返回结果是：<%=signResponse%>
</body>
</html>