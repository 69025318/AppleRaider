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
		String rcv_bank_name = request.getParameter("rcv_bank_name"); // 收款方行名
		String rcv_acno = request.getParameter("rcv_acno"); // 收款人账号
		String rcv_acname = request.getParameter("rcv_acname"); // 收款人户名
		String amount = request.getParameter("amount"); // 金额
		String summary = request.getParameter("summary"); // 附言
		
		// 封装数据
		CommandDto dto = new CommandDto();
		dto.setSerialNo(serialNo);
		dto.setRcvrAcctNo(rcv_acno);
		dto.setRcvrAcctNm(rcv_acname);
		dto.setRcvrBnkBranchname(rcv_bank_name);
		dto.setAmount(amount);
		dto.setTranRemark(summary);
		
		// 调用接口
		SingleVirement singleVirement = new SingleVirement();
		singleVirement.transfer(dto);
		out.print("success");
	%>
</body>
</html>