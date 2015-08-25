<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>交行银企直联</title>
</head>
<body style="padding:20px;">
<header>
	<strong>交行-交易明细查询接口测试</strong><br>
	<a href="BOCOM2InterfaceTest.jsp">home</a>
</header>

<div style="margin:50px;">
	<form id="do_form">
	<table>
		<tr>
			<td>流水号：</td>
			<td><input type="text" name="serialNo"></td>
		</tr>
		<tr>
			<td>账号：</td>
			<td><input type="text" name="acno"></td>
		</tr>
		<tr>
			<td>查询日期：</td>
			<td><input type="text" name="queryDate"></td>
		</tr>
		<tr>
			<td><input type="button" value="提交" onclick="doSend();"></td>
			<td><input type="reset" value="重置"></td>
		</tr>
	</table>
	</form>
	
	<div style="margin-top:20px;">
		<p style="color:red">返回结果：	</p>
		<textarea id="result_textarea" rows="20" cols="38" ></textarea>
	</div>
</div>

<footer>@2015/05/05</footer>
</body>

<script type="text/javascript" src="script/jquery.min.js"></script>
<script type="text/javascript">

	var doSend = function() {
		var formData = $('#do_form').serialize();
		$.ajax({
			type : 'post',
			url : 'BOCOM2QueryAccountByHistory.jsp',
			data : formData,
			success : function(result) {
				$('#result_textarea').text(result);
			},
			error : function(XMLHttpRequest, errorMessage, errorThrown) {
				$('#result_textarea').text(XMLHttpRequest.statusText + ' : ' + XMLHttpRequest.status);
			}
		});
	};
	
</script>
</html>