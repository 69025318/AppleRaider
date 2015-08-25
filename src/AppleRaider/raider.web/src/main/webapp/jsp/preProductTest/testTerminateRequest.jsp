<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<!-- 内测环境测试新BE解约接口 -->
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>内测环境测试新BE解约接口</title>
<script src="http://code.jquery.com/jquery-1.5.js"></script>
<script>

	function changeAccptmd() {
		var obj = document.getElementById("accptmdSelect"); //定位id
		var index = obj.selectedIndex; // 选中索引
		var value = obj.options[index].value; // 选中值
		document.getElementById("accptmd").value = value;

	}
</script>
</head>
<body>
	<form action="/bankuat/jsp/preProductTest/testTerminate.jsp">
		<table width="800px" cellpadding="2px" cellspacing="0px" border="1">
			<tr>
				<td style="width:20%">bankNo:</td><td><input style="width:400px" type="text" name="bankNo" value="" /></td>
			</tr>
			<tr>
				<td style="width:20%">appKind:</td><td><input style="width:400px" type="text" name="appKind" value="" /></td>
			</tr>
			<tr>
				<td style="width:20%">accountNo:</td><td><input style="width:400px" type="text" name="accountNo" value="" /></td>
			</tr>
			<tr>
				<td style="width:20%">accountName</td><td><input style="width:400px" type="text" name="accountName" value="" /></td>
			</tr>
			<tr>
				<td style="width:20%">idType</td><td><input style="width:400px" type="text" name="idType" value="0" /></td>
			</tr>
			<tr>
				<td style="width:20%">idNo:</td><td><input style="width:400px" type="text" name="idNo" value="" /></td>
			</tr>
			<tr>
				<td style="width:20%">accptmd:</td>
				<td>
					<input style="width:400px" type="text" name="accptmd" id="accptmd" value="2" />
					<select style="width:80px" id="accptmdSelect" onchange="changeAccptmd();">
						<option label="PC" value="2" />
						<option label="Mobile" value="M" />
						<option label="HOP" value="H" />
					</select>
				</td>
			</tr>
			<tr>
				<td style="width:20%">protocolNo:</td><td><input style="width:400px" type="text" name="protocolNo" value="" /></td>
			</tr>
			<tr>
				<td colspan="2"><input type="submit" value="提交"></td>
			</tr>
		</table>
	</form>
</body>
</html>