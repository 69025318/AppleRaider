<%@ page import="com.fund.etrading.ebankapp.base.dto.CommandDto" %>
<%@ page import="com.fund.etrading.ebankapp.base.bocom2.transaction.SingleVirement" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>交通银行银企直联</title>
</head>
<body>
	<!-- 业务逻辑 -->
	<%
	    // 获得参数
		request.setCharacterEncoding("UTF-8");
		String serialNo = request.getParameter("serialNo"); // 流水号
		String recAccNo = request.getParameter("recAccNo"); // 收款账号
		String recAccName = request.getParameter("recAccName"); // 收款户名
		String amount = request.getParameter("amount"); // 金额
		String subAgrNo = request.getParameter("subAgrNo"); // 付款签约编号(多域串)
		String payAccNo = request.getParameter("payAccNo"); // 付款账号(多域串)
		String payAccName = request.getParameter("payAccName"); // 付款账号户名(多域串)
		String feeName = request.getParameter("feeName"); // 缴费户名(多域串)
		
		// 封装数据
		CommandDto dto = new CommandDto();
		dto.setSerialNo(serialNo);
		dto.setRcvrAcctNo(recAccNo);
		dto.setRcvrAcctNm(recAccName);
		dto.setAmount(amount);
		dto.setSndrProtoNo(subAgrNo);
		dto.setSndrAcctNo(payAccNo);
		dto.setSndrAcctNm(payAccName);
		dto.setSndrName(feeName);
		
		// 调用方法
		SingleVirement singleVirement = new SingleVirement();
		singleVirement.pay(dto);
		out.print("success");
	%>
</body>
</html>