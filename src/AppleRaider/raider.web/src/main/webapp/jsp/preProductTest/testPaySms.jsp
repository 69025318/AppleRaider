<%@page import="com.htffund.etrade.bankengine.model.PaySmsRequest"%>
<%@page import="com.htffund.etrade.bankengine.model.PaySmsResponse"%>
<%@page import="com.htffund.etrade.bankengine.biz.service.impl.BankEngineServiceImpl"%>
<%@page import="com.htffund.etrade.bankengine.service.BankEngineService"%>
<%@page import="com.htffund.etrade.bankengine.biz.util.SpringUtils"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
<%
	PaySmsRequest paySmsRequest = new PaySmsRequest();

	paySmsRequest.setBankNo(request.getParameter("bankNo"));
	paySmsRequest.setAuthCode(request.getParameter("authCode"));
	paySmsRequest.setPaySerialNo(request.getParameter("paySerialNo"));
	
	BankEngineService bankEngineService = SpringUtils.getBean(BankEngineServiceImpl.class);
	PaySmsResponse payResponse = bankEngineService.paySms(paySmsRequest);
%>
输入参数是：<%=paySmsRequest %><br>
	返回结果是：<%=payResponse %>
</body>
</html>