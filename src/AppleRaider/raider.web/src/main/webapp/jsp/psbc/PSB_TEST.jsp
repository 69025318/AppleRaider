<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>邮储通达测试</title>
</head>
<body>
<!-- 签约 -->
<p>签约</p>
<form action="PSB_ACTION.jsp" method="post">
	<table>
		<tr>
			<td>用户ID：</td>
			<td><input type="text" name="userId" id=""></td>
		</tr>
		<tr>
			<td>身份证号：</td>
			<td><input type="text" name="paperNo" id=""></td>
		</tr>
		<tr>
			<td>渠道号：</td>
			<td><input type="text" name="channelId" id="">(渠道号02个人网银，10手机银行)</td>
		</tr>
		<tr>
			<td>姓名：</td>
			<td><input type="text" name="AccName" id=""></td>
		</tr>
		<tr>
			<td>卡号：</td>
			<td><input type="text" name="CardNo" id=""></td>
		</tr>
	</table>
	<input type="hidden" name="tranAbbr" value="APSR">
	<input type="submit" value="提交" ><br/>
</form>
<br />
<br />

<!-- 解约 -->
<p>解约</p>
<form action="PSB_ACTION.jsp" method="post">
	<table>
		<tr>
			<td>支付协议号</td>
			<td><input type="text" name="signNo" value="" /></td>
		</tr>
	</table>
	<input type="hidden" name="tranAbbr" value="APSC">
	<input type="submit" value="提交" ><br/>
</form>
<br />
<br />
<!-- 支付申请 -->
<p>支付申请</p>
<form action="PSB_ACTION.jsp" method="post">
	<table>
		<tr>
			<td>交易流水号: </td>
			<td><input type="text" name="serialno" id="">(必填)</td>
		</tr>
		<tr>
			<td>支付金额: </td>
			<td><input type="text" name="tranAmt" id="">(必填)</td>
		</tr>
		<tr>
			<td>签约协议号: </td>
			<td><input type="text" name="signNo" id="">(必填)</td>
		</tr>
	</table>
	<input type="hidden" name="tranAbbr" value="APCP">
	<input type="submit" value="提交" ><br/>
</form>
<br />
<br />
<p>支付确认</p>
<!-- 支付确认 -->
<form action="PSB_ACTION.jsp" method="post">
	<table>
		<tr>
			<td>交易流水号: </td>
			<td><input type="text" name="serialno" id="">(必填,与支付原始订单一致)</td>
		</tr>
		<tr>
			<td>短信验证码: </td>
			<td><input type="text" name="checkCode" id="">(必填)</td>
		</tr>
		<tr>
			<td>交易金额: </td>
			<td><input type="text" name="tranAmt" id="">(必填,以元为单位,与原始支付订单一致)</td>
		</tr>
		<tr>
			<td>签约协议号: </td>
			<td><input type="text" name="signNo" id="">(必填)</td>
		</tr>
		<tr>
			<td>签约流水号: </td>
			<td><input type="text" name="verifySerialno" id="">(必填)</td>
		</tr>
	</table>
	<input type="hidden" name="tranAbbr" value="APSP">
	<input type="submit" value="提交" ><br/>
</form>
<br/>
<br />
<!-- 提现 -->
<p>提现</p>
<form action="PSB_ACTION.jsp" method="post">
<table>
		<tr>
			<td>交易流水号: </td>
			<td><input type="text" name="serialno" id="">(必填)</td>
		</tr>
		<tr>
			<td>支付金额: </td>
			<td><input type="text" name="tranAmt" id="">(必填)</td>
		</tr>
		<tr>
			<td>签约协议号: </td>
			<td><input type="text" name="signNo" id="">(必填)</td>
		</tr>
	</table>
	<input type="hidden" name="tranAbbr" value="TXCP">
	<input type="submit" value="提交" ><br/>
</form>
<br />
<br />
<p>交易订单查询</p>
<!-- 交易订单查询 -->
<form action="PSB_ACTION.jsp" method="post">
<table>
		<tr>
			<td>订单号: </td>
			<td><input type="text" name="serialno" id=""></td>
		</tr>
	</table>
	<input type="hidden" name="tranAbbr" value="APQR">
	<input type="submit" value="提交" ><br/>
</form>
<br />
<br />
<p></p>
<!-- 对账单下载 -->
<form action="PSB_ACTION.jsp" method="post">
<table>
		<tr>
			<td>清算日期: </td>
			<td><input type="text" name="osttDate" id="">(必填)</td>
		</tr>
		<tr>
			<td>对账文件类型: </td>
			<td><input type="text" name="setFType" id="">(必填,0-	全部,1-支付,2-退货,3-赎回)</td>
		</tr>
	</table>
	<input type="hidden" name="tranAbbr" value="IDFR">
	<input type="submit" value="提交" ><br/>
</form>
<br />
<br />
<p>退货测试</p>
<form action="PSB_ACTION.jsp" method="post">
<table>
		<tr>
			<td>serianNo: </td>
			<td><input type="text" name="serialNo" id="">(必填)</td>
		</tr>
		<tr>
			<td>signNo: </td>
			<td><input type="text" name="signNo" id="">(必填)</td>
		</tr>
		<tr>
			<td>orgSerialNo: </td>
			<td><input type="text" name="orgSerialNo" id="">(必填)</td>
		</tr>
	</table>
<input type="hidden" name="tranAbbr" value="APCR">
	<input type="submit" value="提交" ><br/>
</form>
</body>
</html>