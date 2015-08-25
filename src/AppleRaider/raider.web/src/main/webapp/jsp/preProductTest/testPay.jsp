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
<!-- 内测环境测试新BE, 执行支付指令页面-->
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>内测环境测试新BE, 执行支付指令页面</title>
</head>
<body>
	<%
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		PayRequest payRequest = new PayRequest();
		payRequest.setBankNo(request.getParameter("bankNo"));
		payRequest.setMerTranCode(request.getParameter("merTranCode"));
		payRequest.setRouteCode(request.getParameter("routeCode"));
		payRequest.setCapitalMode(request.getParameter("capitalMode"));
		payRequest.setProductId(request.getParameter("productId"));
		payRequest.setProductName(request.getParameter("productName"));
		payRequest.setCurrency(request.getParameter("currency"));
		
		String amount = request.getParameter("amount");
		payRequest.setAmount(amount==null?BigDecimal.ZERO:new BigDecimal(amount));
		
		payRequest.setRefAppNo(request.getParameter("refAppNo"));
		payRequest.setAppKind(request.getParameter("appKind"));
		payRequest.setSenderBankNo(request.getParameter("senderBankNo"));
		payRequest.setSenderAccountNo(request.getParameter("senderAccountNo"));
		payRequest.setSenderAccountName(request.getParameter("senderAccountName"));
		payRequest.setSenderIdType(request.getParameter("senderIdType"));
		payRequest.setSenderIdNo(request.getParameter("senderIdNo"));
		payRequest.setSenderProtocolNo(request.getParameter("senderProtocolNo"));
		payRequest.setMobileNo(request.getParameter("mobileNo"));
		
		String currWorkingDate = request.getParameter("currWorkingDate");
		payRequest.setCurrWorkingDate(StringUtils.isEmpty(currWorkingDate)? null: sdf.parse(currWorkingDate));
		
		String nextWorkingDate = request.getParameter("nextWorkingDate");
		payRequest.setNextWorkingDate(StringUtils.isEmpty(nextWorkingDate)? null: sdf.parse(nextWorkingDate));
		
		payRequest.setAccpTmd(request.getParameter("accpTmd"));
		payRequest.setPrCardNo(request.getParameter("prCardNo"));
		payRequest.setPrCardPassword(request.getParameter("prCardPassword"));
		
		BankEngineService bankEngineService = SpringUtils.getBean(BankEngineServiceImpl.class);
		PayResponse payResponse = bankEngineService.pay(payRequest);
	%>
	输入参数是：<%=payRequest %><br>
	返回结果是：<%=payResponse %>
</body>
</html>