<%@page import="com.fund.etrading.ebankapp.base.api.ExecuteCommand"%>
<%@page import="com.fund.etrading.ebankapp.base.psbc.api.PSBCExecuteCommand"%>
<%@page import="com.fund.etrading.ebankapp.base.dto.CommandDto"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.Date"%>
<%@page import="com.fund.etrading.ebankapp.base.dto.ReturnDto"%>
<%@page import="com.fund.etrading.ebankapp.base.api.QueryCommand"%>
<%@page import="com.fund.etrading.ebankapp.base.psbc.api.PSBCQueryCommand"%>
<%@page import="java.util.HashMap" %>
<%@page import="java.util.Iterator" %>
<%@page import="com.fund.etrading.ebankapp.base.psbc.tools.PSBCSignatureService" %>
<%@page import="com.fund.etrading.ebankapp.base.psbc.tools.XMLData" %>
<%@page import="com.thoughtworks.xstream.XStream" %>
<%@page import="com.fund.etrading.ebankapp.base.psbc.tools.HttpClientSend" %>
<%@page import="org.dom4j.io.SAXReader" %>
<%@page import="org.dom4j.Document" %>
<%@page import="org.dom4j.Node" %>
<%@page import="java.io.StringReader" %>






<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
	
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>邮储后台执行ACTION</title>
</head>
<body>
<%

request.setCharacterEncoding("UTF-8");
response.setCharacterEncoding("UTF-8");
String tranAbbr = request.getParameter("tranAbbr");
String merDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
String merTime = new SimpleDateFormat("HHmmss").format(new Date());


System.out.println(tranAbbr);

CommandDto dto = null;

if(tranAbbr.equalsIgnoreCase("APSR")){//网上签约
	
	dto = new CommandDto();
	
	String userId = request.getParameter("userId");
	String paperNo = request.getParameter("paperNo");
	String channelId = request.getParameter("channelId");
	String AccName = request.getParameter("AccName");
	String CardNo = request.getParameter("CardNo");
	
	dto.setBnkTranCo(tranAbbr);
	dto.setSerialNo(userId);
	dto.setSndrIdTp("01");//身份证
	dto.setSndrIdNo(paperNo);
	dto.setSndrResv2(channelId);
	dto.setSndrAcctNm(AccName);
	dto.setSndrAcctNo(CardNo);
	dto.setMerDate(merDate);
	dto.setMerTime(merTime);
	
}else if(tranAbbr.equalsIgnoreCase("APQR")){//订单查询
	
	dto = new CommandDto();
	
	String serialno = request.getParameter("serialno");
	
	dto.setQryTranCo(tranAbbr);
	dto.setBnkOrgSerialNo(serialno);
	dto.setMerDate(merDate);
	dto.setMerTime(merTime);
	
}else if(tranAbbr.equalsIgnoreCase("APCP")){//支付
	
	
	dto = new CommandDto();

	String tranAmt = request.getParameter("tranAmt");
	String signNo = request.getParameter("signNo");
	String serialno = request.getParameter("serialno");
	
	dto.setBnkTranCo(tranAbbr);
	dto.setSerialNo(serialno);
	dto.setAmount(tranAmt);
	dto.setSndrProtoNo(signNo);
	dto.setMerDate(merDate);
	dto.setMerTime(merTime);
	
}else if(tranAbbr.equalsIgnoreCase("APSP")){//支付短信确认
	
	
	dto = new CommandDto();
	
	String serialno = request.getParameter("serialno");
	String checkCode = request.getParameter("checkCode");
	String signNo = request.getParameter("signNo");
	String tranAmt = request.getParameter("tranAmt");
	String verifySerialno = request.getParameter("verifySerialno");

	dto.setBnkTranCo(tranAbbr);
	dto.setSerialNo(serialno);
	dto.setMerDate(merDate);
	dto.setMerTime(merTime);
	dto.setAmount(tranAmt);
	dto.setSndrResv1(checkCode);
	dto.setSndrProtoNo(signNo);
	dto.setBnkOrgSerialNo(verifySerialno);
	
}else if(tranAbbr.equalsIgnoreCase("TXCP")){//提现、赎回交易
	
	dto = new CommandDto();
	
	String tranAmt = request.getParameter("tranAmt");
	String signNo = request.getParameter("signNo");
	String serialno = request.getParameter("serialno");
	
	dto.setBnkTranCo(tranAbbr);
	dto.setSerialNo(serialno);
	dto.setMerDate(merDate);
	dto.setMerTime(merTime);
	dto.setAmount(tranAmt);
	dto.setSndrProtoNo(signNo);
	
}else if(tranAbbr.equalsIgnoreCase("IDFR")){
	
	dto = new CommandDto();
	
	String osttDate = request.getParameter("osttDate");
	String setFType = request.getParameter("setFType");
	
	dto.setBnkTranCo(tranAbbr);
	dto.setBnkOrgDate(osttDate);
	dto.setRcvrResv1(setFType);

}else if(tranAbbr.equalsIgnoreCase("APSC")){
	dto = new CommandDto();
	
	String signNo = request.getParameter("signNo");
	
	dto.setBnkTranCo(tranAbbr);
	dto.setSndrProtoNo(signNo);
	dto.setMerDate(merDate);
	dto.setMerTime(merTime);
	
}else if(tranAbbr.equalsIgnoreCase("APCR")){
	String signNo = request.getParameter("signNo");
	String serialNo = request.getParameter("serialNo");
	String orgSerialNo = request.getParameter("orgSerialNo");
	
	StringBuffer sbf = new StringBuffer();
	sbf.append("TranAbbr=APCR|");
	sbf.append("MercDtTm=" + merDate + merTime + "|");
	sbf.append("SignNo="+ signNo +"|");
	sbf.append("TermSsn=" + serialNo + "|");
	sbf.append("OSttDate="+ merDate +"|");
	sbf.append("OAcqSsn="+ orgSerialNo +"|");
	sbf.append("MercCode=1100529310009000186|");
	sbf.append("TermCode=|");
	sbf.append("TranAmt=12.00|");
	sbf.append("MerBackUrl=|");
	sbf.append("Remark1=|");
	sbf.append("Remark2=");
	String Plain = sbf.toString();
	
	String signature = PSBCSignatureService.merchantSignData(Plain);
	// 组织报文
		String head = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		XMLData xd = new XMLData();
		XStream stream = new XStream();
		xd.setPlain(Plain);
		xd.setSignature(signature);
		xd.setTransName("APCR");
		stream.alias("packet", XMLData.class);
		StringBuffer sb = new StringBuffer();
		sb.append(head);
		sb.append(stream.toXML(xd));
		String sendMsg = sb.toString();
		HttpClientSend httpSend = new HttpClientSend();
		String resultXml = httpSend.send(sendMsg, "http://103.22.255.201:8443/psbcpay/main");
		SAXReader reader = new SAXReader();
		try {
		    Document doc = reader.read(new StringReader(resultXml));
		    Node n = doc.selectSingleNode("//ErrorCode");
		    if (n == null) {
				String retTransName = doc.selectSingleNode("//transName").getText();
				String retPlain = doc.selectSingleNode("//Plain").getText();
				String retSignature = doc.selectSingleNode("//Signature").getText();
			
				
				boolean sRes = PSBCSignatureService.merchantVerify(retPlain, retSignature);
				
				if (sRes) {
				    String[] arr = retPlain.split("\\|");
				    String RespCode = ""; // 返回码
				    String RespMsg = ""; //返回信息
				    for (int i = 0; i < arr.length; i++) {
						if ("RespCode".equals(arr[i].split("=")[0])) {
						    RespCode=arr[i].split("=")[1];
						}
						
						if ("RespMsg".equals(arr[i].split("=")[0])) {
							RespMsg=arr[i].split("=")[1];
						}
				    }
				    
				    if ("00000000".equals(RespCode)) {
				    	response.getWriter().print("response CODE:"+RespCode);
				    } else {
				    	response.getWriter().print("response CODE:"+RespCode);
				    }
				} else {
				
					
				}
		    } else {


		    }
		} catch (Exception e) {


		}
	return;
}

ReturnDto rdto = null;
if(tranAbbr.equalsIgnoreCase("APQR")){
	PSBCQueryCommand qc = new PSBCQueryCommand(dto);
	rdto = qc.query();
}else{
	PSBCExecuteCommand psbc = new PSBCExecuteCommand(dto);
	rdto = psbc.execute();
}
%>
<%
if(rdto!=null){
	if(tranAbbr.equalsIgnoreCase("APSR")){
		HashMap formMap = rdto.getTranMsg().getFormBean();
		StringBuffer sb = new StringBuffer();
		Iterator iter = formMap.keySet().iterator();
		String key = null;
		while (iter.hasNext()) {
		    key = (String) iter.next();
		    sb.append("<input type=\"hidden\" name=\"").append(key).append("\" value=\"")
			    .append(formMap.get(key)).append("\" \\>").append("\r\n");
		}
		%>
		<form id="myForm" name="myForm" action="<%=rdto.getTranMsg().getRedUrl()%>" method="post" target="_blank">
			<%=sb.toString() %>
		</form>
		
	<%
		response.getWriter().print("response CODE:"+rdto.getTranMsg().getRedUrl()+"\r\n response MSG:"+rdto.getTranSt().getTranSt());
	
	}else{
		
		response.getWriter().print("response CODE:"+rdto.getResp().getRespCo()+"\r\n response MSG:"+rdto.getResp().getRespMsg());
	}
}else{
	response.getWriter().print("ReturnDto is null!");
}
%>
</body>
</html>