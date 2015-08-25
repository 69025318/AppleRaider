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
		String serialNo = request.getParameter("serialNo"); // 指令流水号
		String acno = request.getParameter("acno"); // 账号
		String bnkOrgDate = request.getParameter("queryDate"); // 查询日期
		
		// 封装数据
		CommandDto dto = new CommandDto();
		dto.setSerialNo(serialNo);
		dto.setSndrAcctNo(acno);
		dto.setBnkOrgDate(bnkOrgDate);
		
		// 调用接口
		SingleVirement singleVirement = new SingleVirement();
		singleVirement.queryAccounts(dto);
		out.print("success");
	%>
</body>
</html>