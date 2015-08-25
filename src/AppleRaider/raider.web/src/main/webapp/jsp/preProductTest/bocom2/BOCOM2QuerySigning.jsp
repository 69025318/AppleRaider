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
		String accNo = request.getParameter("accNo"); // 付款账号
		String accNm = request.getParameter("accNm"); // 付款户名
		String idIp = request.getParameter("idIp"); // 证件类型
		String idNo = request.getParameter("idNo"); // 证件号码
		String merDate = request.getParameter("merDate"); // date
		String merTime = request.getParameter("merTime"); // time
		
		// 封装数据
		CommandDto dto = new CommandDto();
		dto.setSerialNo(serialNo);
		dto.setSndrAcctNo(accNo);
		dto.setSndrAcctNm(accNm);
		dto.setSndrIdTp(idIp);
		dto.setSndrIdNo(idNo);
		dto.setMerDate(merDate);
		dto.setMerTime(merTime);
		
		// 调用接口
		SingleVirement singleVirement = new SingleVirement();
		singleVirement.verify(dto);
		out.print("success");
	%>
</body>
</html>