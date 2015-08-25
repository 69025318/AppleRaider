<%@ page import="com.fund.etrading.ebankapp.base.dto.CommandDto" %>
<%@ page import="com.fund.etrading.ebankapp.base.bocom2.transaction.QueryAccount" %>
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
		String serialNo = request.getParameter("serialNo"); // 指令流水号
		
		// 封装数据
		CommandDto dto = new CommandDto();
		dto.setSerialNo(serialNo);
		
		// 调用接口
		QueryAccount queryAccount = new QueryAccount();
		queryAccount.queryPay(dto);
		out.print("success");
	%>
</body>
</html>