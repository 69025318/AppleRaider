<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>交行银企直联</title>
</head>
<body style="padding:20px;">
<header>
	<strong>交行-批量代收接口测试</strong><br>
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
			<td>收款方协议号：</td>
			<td><input type="text" name="cagrNo"></td>
		</tr>
		<tr>
			<td>收款账号：</td>
			<td><input type="text" name="recAccNo"></td>
		</tr>
		<tr>
			<td>收款户名：</td>
			<td><input type="text" name="recAccName"></td>
		</tr>
		<tr>
			<td>金额：</td>
			<td><input type="text" name="amount"></td>
		</tr>
		<tr>
			<td>付款签约编号：</td>
			<td><input type="text" name="subAgrNo"></td>
		</tr>
		<tr>
			<td>付款账号：</td>
			<td><input type="text" name="payAccNo"></td>
		</tr>
		<tr>
			<td>付款账号户名：</td>
			<td><input type="text" name="payAccName"></td>
		</tr>
		<tr>
			<td>缴费户名：</td>
			<td><input type="text" name="feeName"></td>
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
			url : 'BOCOM2BatchDeduction.jsp',
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