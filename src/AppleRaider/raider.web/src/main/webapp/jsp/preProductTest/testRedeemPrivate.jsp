<%@page import="com.htffund.etrade.bankengine.model.RedeemPrivateResponse"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.htffund.etrade.bankengine.model.RedeemPrivateRequest"%>
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
		RedeemPrivateRequest redeemRequest = new RedeemPrivateRequest();
		redeemRequest.setBankNo(request.getParameter("bankNo"));
		redeemRequest.setMerTranCode(request.getParameter("merTranCode"));
		
		redeemRequest.setProductId(request.getParameter("productId"));
		redeemRequest.setProductName(request.getParameter("productName"));
		redeemRequest.setProductType(request.getParameter("productType"));
		redeemRequest.setCurrency(request.getParameter("currency"));
		
		String amount = request.getParameter("amount");
		redeemRequest.setAmount(amount==null?BigDecimal.ZERO:new BigDecimal(amount));
		
		redeemRequest.setRefAppNo(request.getParameter("refAppNo"));
		redeemRequest.setAppKind(request.getParameter("appKind"));
		redeemRequest.setReceiverBankNo(request.getParameter("receiverBankNo"));
		redeemRequest.setReceiverAccountNo(request.getParameter("receiverAccountNo"));
		redeemRequest.setReceiverAccountName(request.getParameter("receiverAccountName"));
		redeemRequest.setReceiverIdType(request.getParameter("receiverIdType"));
		redeemRequest.setReceiverIdNo(request.getParameter("receiverIdNo"));
		redeemRequest.setReceiverCity(request.getParameter("receiverCity"));
		redeemRequest.setReceiverProtocolNo(request.getParameter("receiverProtocolNo"));
		redeemRequest.setAccountId(request.getParameter("accountId"));
		redeemRequest.setRouteCode(request.getParameter("routeCode"));
		redeemRequest.setCapitalMode(request.getParameter("capitalMode"));
		redeemRequest.setHuiLu(request.getParameter("huiLu"));
		
		BankEngineService bankEngineService = SpringUtils.getBean(BankEngineServiceImpl.class);
		RedeemPrivateResponse redeemResponse = bankEngineService.redeemPrivate(redeemRequest);
	%>
	输入参数是：<%=redeemRequest %><br>
	返回结果是：<%=redeemResponse %>
</body>
</html>