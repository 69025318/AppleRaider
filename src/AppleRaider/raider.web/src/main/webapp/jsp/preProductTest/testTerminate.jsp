<%@page import="com.htffund.etrade.bankengine.model.TerminateResponse"%>
<%@page import="com.htffund.etrade.bankengine.model.TerminateRequest"%>
<%@page import="com.htffund.etrade.bankengine.model.Accptmd"%>
<%@page
	import="com.htffund.etrade.bankengine.biz.service.impl.BankEngineServiceImpl"%>
<%@page import="com.htffund.etrade.bankengine.service.BankEngineService"%>
<%@page import="com.htffund.etrade.bankengine.biz.util.SpringUtils"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<!-- 内测环境测试新BE, 执行解约指令页面-->
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>内测环境测试新BE, 执行解约指令页面</title>
</head>
<body>
	<%
		TerminateRequest terminateRequest = new TerminateRequest();
		terminateRequest.setBankNo(request.getParameter("bankNo"));
		terminateRequest.setAppKind(request.getParameter("appKind"));
		terminateRequest.setAccountNo(request.getParameter("accountNo"));
		terminateRequest.setAccountName(request.getParameter("accountName"));
		terminateRequest.setIdType(request.getParameter("idType"));
		terminateRequest.setIdNo(request.getParameter("idNo"));
		terminateRequest.setProtocolNo(request.getParameter("protocolNo"));

		String accptmdStr = request.getParameter("accptmd");
		Accptmd accptmd = Accptmd.HOP;
		if ("2".equals(accptmdStr)) {
			accptmd = Accptmd.PC;
		} else if ("M".equals(accptmdStr)) {
			accptmd = Accptmd.Mobile;
		}
		terminateRequest.setAccpTmd(accptmd);

		BankEngineService bankEngineService = SpringUtils
				.getBean(BankEngineServiceImpl.class);
		TerminateResponse terminateResponse = bankEngineService
				.terminate(terminateRequest);
	%>
	输入参数是：<%=terminateRequest%><br> 返回结果是：<%=terminateResponse%>
</body>
</html>