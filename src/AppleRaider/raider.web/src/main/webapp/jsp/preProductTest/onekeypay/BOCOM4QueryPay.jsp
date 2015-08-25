<%@ page import="com.fund.etrading.ebankapp.base.dto.CommandDto" %>
<%@ page import="com.fund.etrading.ebankapp.base.dto.ReturnDto" %>
<%@ page import="com.fund.etrading.ebankapp.base.bocom4.transaction.QueryAccount" %>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>交行一键支付</title>
</head>
<body>
	<%
		// 获得参数
		request.setCharacterEncoding("UTF-8");
		String serialNo = request.getParameter("serialNo"); // 流水号
		
		// 封装数据
		CommandDto dto = new CommandDto();
		dto.setSerialNo(serialNo);
		
		// 调用方法
		QueryAccount queryAccountNew = new QueryAccount();
		ReturnDto returnDto = queryAccountNew.queryPay(dto);

		// 向页面输出返回报文
		out.print(returnDto.toString());
	%>
</body>
</html>