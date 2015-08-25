<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<!-- 内测环境测试新BE支付接口 -->
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>内测环境测试新BE支付接口</title>
</head>
<body>
	<form action="testPay.jsp">
		<table width="800px" cellpadding="2px" cellspacing="0px" border="1">
			<tr>
				<td style="width:20%">bankNo:</td><td><input style="width:400px" type="text" name="bankNo" value="007" /></td>
			</tr>
			<tr>
				<td style="width:20%">merTranCode:</td><td><input style="width:400px" type="text" name="merTranCode" value="03" /></td>
			</tr>
			<tr>
				<td style="width:20%">routeCode:</td><td><input style="width:400px" type="text" name="routeCode" value="025" /></td>
			</tr>
			<tr>
				<td style="width:20%">capitalMode:</td><td><input style="width:400px" type="text" name="capitalMode" value="025" /></td>
			</tr>
			<tr>
				<td style="width:20%">productId:</td><td><input style="width:400px" type="text" name="productId" value="000330" /></td>
			</tr>
			<tr>
				<td style="width:20%">productName:</td><td><input style="width:400px" type="text" name="productName" value="汇添富现金宝货币市场基金" /></td>
			</tr>
			<tr>
				<td style="width:20%">currency:</td><td><input style="width:400px" type="text" name="currency" value="01" /></td>
			</tr>
			<tr>
				<td style="width:20%">amount:</td><td><input style="width:400px" type="text" name="amount" value="0.1" /></td>
			</tr>
			<tr>
				<td style="width:20%">refAppNo:</td><td><input style="width:400px" type="text" name="refAppNo" value="" /></td>
			</tr>
			<tr>
				<td style="width:20%">appKind:</td><td><input style="width:400px" type="text" name="appKind" value="922" /></td>
			</tr>
			<tr>
				<td style="width:20%">senderBankNo:</td><td><input style="width:400px" type="text" name="senderBankNo" value="007" /></td>
			</tr>
			<tr>
				<td style="width:20%">senderAccountNo:</td><td><input style="width:400px" type="text" name="senderAccountNo" value="2014043014687242" /></td>
			</tr>
			<tr>
				<td style="width:20%">senderAccountName:</td><td><input style="width:400px" type="text" name="senderAccountName" value="温春" /></td>
			</tr>
			<tr>
				<td style="width:20%">senderIdType:</td><td><input style="width:400px" type="text" name="senderIdType" value="0" /></td>
			</tr>
			<tr>
				<td style="width:20%">senderIdNo:</td><td><input style="width:400px" type="text" name="senderIdNo" value="440306198102050037" /></td>
			</tr>
			<tr>
				<td style="width:20%">senderProtocolNo:</td><td><input style="width:400px" type="text" name="senderProtocolNo" value="2014043014687242" /></td>
			</tr>
			<tr>
				<td style="width:20%">mobileNo:</td><td><input style="width:400px" type="text" name="mobileNo" value="" /></td>
			</tr>
			<tr>
				<td style="width:20%">currWorkingDate:</td><td><input style="width:400px" type="text" name="currWorkingDate" value="" /></td>
			</tr>
			<tr>
				<td style="width:20%">nextWorkingDate:</td><td><input style="width:400px" type="text" name="nextWorkingDate" value="" /></td>
			</tr>
			<tr>
				<td style="width:20%">accpTmd:</td><td><input style="width:400px" type="text" name="accpTmd" value="M" /></td>
			</tr>
			<tr>
				<td style="width:20%">prCardNo:</td><td><input style="width:400px" type="text" name="prCardNo" value="" /></td>
			</tr>
			<tr>
				<td style="width:20%">prCardPassword:</td><td><input style="width:400px" type="text" name="prCardPassword" value="" /></td>
			</tr>
			
			<tr>
				<td colspan="2"><input type="submit" value="提交"></submit></td>
			</tr>
		</table>
	</form>
</body>
</html>