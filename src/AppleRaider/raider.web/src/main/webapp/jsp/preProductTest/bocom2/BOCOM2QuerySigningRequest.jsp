<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>交通银行银企直联</title>
</head>
<body style="padding:20px;">
<header>
	<strong>交通银行-子协议查询接口测试</strong><br>
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
			<td>日期：</td>
			<td><input type="text" name="merDate"></td>
		</tr>
		<tr>
			<td>时间：</td>
			<td><input type="text" name="merTime"></td>
		</tr>
		<tr>
			<td>付款账号：</td>
			<td><input type="text" name="accNo"></td>
		</tr>
		<tr>
			<td>付款户名：</td>
			<td><input type="text" name="accNm"></td>
		</tr>
		<tr>
			<td>证件类型：</td>
			<td><input type="text" name="idIp"></td>
		</tr>
		<tr>
			<td>证件号码：</td>
			<td><input type="text" name="idNo"></td>
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

<footer>@2015/07/17</footer>
</body>

<script type="text/javascript" src="script/jquery.min.js"></script>
<script type="text/javascript">

	var doSend = function() {
		var formData = $('#do_form').serialize();
		$.ajax({
			type : 'post',
			url : 'BOCOM2QuerySigning.jsp',
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