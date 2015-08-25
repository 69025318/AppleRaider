<%@page import="com.htffund.etrade.bankengine.model.Accptmd"%>
<%@page import="com.htffund.etrade.bankengine.model.VerifyResponse"%>
<%@page import="com.htffund.etrade.bankengine.model.VerifyRequest"%>
<%@page
	import="com.htffund.etrade.bankengine.biz.service.impl.BankEngineServiceImpl"%>
<%@page import="com.htffund.etrade.bankengine.service.BankEngineService"%>
<%@page import="com.htffund.etrade.bankengine.biz.util.SpringUtils"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<!-- 内测环境测试新BE, 执行支付指令页面-->
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>内测环境测试新BE, 执行支付指令页面</title>
</head>
<body>
	<%
		VerifyRequest verifyRequest = new VerifyRequest();
		verifyRequest.setBankNo(request.getParameter("bankNo"));
		verifyRequest.setAppKind(request.getParameter("appKind"));
		verifyRequest.setAccountNo(request.getParameter("accountNo"));
		verifyRequest.setAccountName(request.getParameter("accountName"));
		verifyRequest.setIdType(request.getParameter("idType"));
		verifyRequest.setIdNo(request.getParameter("idNo"));
		verifyRequest.setMobileNo(request.getParameter("mobileNo"));

		String accptmdStr = request.getParameter("accptmd");
		Accptmd accptmd = Accptmd.HOP;
		if ("2".equals(accptmdStr)) {
			accptmd = Accptmd.PC;
		} else if ("M".equals(accptmdStr)) {
			accptmd = Accptmd.Mobile;
		}
		verifyRequest.setAccptmd(accptmd);

		BankEngineService bankEngineService = SpringUtils
				.getBean(BankEngineServiceImpl.class);
		VerifyResponse verifyResponse = bankEngineService
				.verify(verifyRequest);
	%>
	输入参数是：<%=verifyRequest%><br> 返回结果是：<%=verifyResponse%>
</body>
</html>