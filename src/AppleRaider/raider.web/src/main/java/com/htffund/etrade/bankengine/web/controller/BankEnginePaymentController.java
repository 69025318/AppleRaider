package com.htffund.etrade.bankengine.web.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fund.common.util.PresentationUtil;
import com.fund.etrading.ebankapp.base.ab.api.ABConfig;
import com.fund.etrading.ebankapp.base.api.ConfigFactory;
import com.fund.etrading.ebankapp.base.api.LoggerFactory;
import com.fund.etrading.ebankapp.base.bocom.api.BOCOMConfig;
import com.fund.etrading.ebankapp.base.cp.pkg.PkgBodyBidRep;
import com.fund.etrading.ebankapp.base.cp.transaction.SingleVirement;
import com.fund.etrading.ebankapp.base.cp2.pkg.Pkg2SubscribeResult;
import com.fund.etrading.ebankapp.base.cp2.tools.SignMsgService;
import com.fund.etrading.ebankapp.service.ExecuteEngineService;
import com.fund99.etrade.result.Result;
import com.htffund.etrade.bankengine.BankEngineConstants;
import com.htffund.etrade.bankengine.biz.mq.MqSendUtils;
import com.htffund.etrade.bankengine.dao.BankResponsionMapper;
import com.htffund.etrade.bankengine.dao.service.BankCommandService;
import com.htffund.etrade.bankengine.model.BankRespDto;
import com.htffund.etrade.bankengine.model.BeMetaQMessage;
import com.htffund.etrade.bankengine.model.TransactionStatus;
import com.htffund.etrade.bankengine.model.db.BankCommand;
import com.htffund.etrade.bankengine.model.db.BankResponsion;


/**
 * 处理银行在B2C支付时，接收银行返回状态。成功后会发送metaQ信息
 *
 * @author wenchun
 * @since 1.2.0
 *
 */
@Controller
public class BankEnginePaymentController {

	@Autowired
	BankCommandService bankCommandService;

	@Autowired
	ExecuteEngineService executeEngineService;

	@Resource
	BankResponsionMapper bankResponsionMapper;

	Logger beLogger = LoggerFactory.getEBANKLogger();



	/**
	 * 农业银行
	 * 认/申购，银行返回结果处理
	 * @param request
	 * @param response
	 */
	@Deprecated
	@RequestMapping(value = "payment/abPayGetResponsion")
	public String paymentAb(HttpServletRequest request,HttpServletResponse response){
		String msg = request.getParameter("msg");
		beLogger.info("接受农行服务器通知返回：msg="+msg);

		if(StringUtils.isEmpty(msg)){
			return null;
		}

		String tMerchantPage = null;
		ABConfig abConfig = ConfigFactory.getABConfig();
		String bnkNo = null;
		if (abConfig != null) {
			try {
				bnkNo = abConfig.getBnkNo();	//获取银行代码
				tMerchantPage = abConfig.getPayResultMerchantURL();
			}
			catch (Exception e) {
				beLogger.error("获取农行配置出错",e);
			}
		}

		BankRespDto inPara = new BankRespDto();
		inPara.setOriginalMsg(msg);
		inPara.setBnkNo(bnkNo);
		inPara.setApKind(BankEngineConstants.APKIND_PURCHASE);
		Result<BankRespDto> outPara = executeEngineService.writeResponsion(inPara);
		String respStatus = outPara.getModel().getRespStatus();
		String errorMsg = outPara.getErrorMessage();
		tMerchantPage = PresentationUtil.urlAppendParam(tMerchantPage, "_st", respStatus);
		tMerchantPage = PresentationUtil.urlAppendParam(tMerchantPage, "_des", errorMsg);
		beLogger.debug("应答回写结果：respStatus="+respStatus);

		return "CloseDirectly";
	}


	/**
	 * 中行支付接收银行应答报文
	 * @param request
	 * @param response
	 */
	@RequestMapping("payment/bocPayGetResponsion")
	public String paymentBoc(HttpServletRequest request,HttpServletResponse response){
		String orderNo=request.getParameter("orderNo");
		beLogger.debug("serialNo="+orderNo);

	    Map<?,?> parameterMap=request.getParameterMap();
	    HashMap<String,String> parMap = changeCoding(parameterMap);
		String  holderName=bankCommandService.getSndrAcctNmBySerialNo(orderNo);   //临时方案
		beLogger.debug("holderName="+holderName);
		parMap.put("holderName",holderName);

		BankCommand currBankCommand = bankCommandService.getBankCommandBySerialNo(orderNo);
		beLogger.debug("获取银行指令currBankCommand="+currBankCommand);

		if(currBankCommand == null || ((BankEngineConstants.BANKCOMMAND_FINAL_STATUS).indexOf(currBankCommand.getTranSt()) <= -1)){
			BankRespDto inPara = new BankRespDto();
			inPara.setOriginalMsgMap(parMap);
			inPara.setBnkNo("104");
			inPara.setApKind(BankEngineConstants.APKIND_PURCHASE);
			Result<BankRespDto> outPara = executeEngineService.writeResponsion(inPara);
			String respStatus = outPara.getModel().getRespStatus();
			beLogger.debug("应答回写结果：respStatus="+respStatus);

			if(BankEngineConstants.CMD_RESP_PAYED.equals(respStatus) ||
					BankEngineConstants.CMD_RESP_NOT_PAYED.equals(respStatus)){
				sendMqMessage(orderNo);
			}
		}

		return "CloseDirectly";
	}

	/**
	 * 交行支付接收银行应答报文
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("payment/bocomPayGetResponsion")
	public String paymentBocom(HttpServletRequest request, HttpServletResponse response){
		String msg = request.getParameter("notifyMsg");
		beLogger.debug("交通银行主动应答返回：msg="+msg);

		if(StringUtils.isEmpty(msg)){
			return null;
		}

		String bnkNo = null;
		BOCOMConfig bocomConfig = ConfigFactory.getBOCOMConfig();
		if (bocomConfig != null) {
			try {
				bnkNo = bocomConfig.getBnkNo();	//获取银行代码
			}
			catch (Exception e) {
				beLogger.error("Controller:无法获取银行配置信息！",e);
			}
		}

		String serialNo = getBocomSerialNoFromMessage(msg);

		beLogger.debug("serialNo="+serialNo);

		BankCommand currBankCommand = bankCommandService.getBankCommandBySerialNo(serialNo);
		beLogger.debug("获取银行指令currBankCommand="+currBankCommand);

		if(currBankCommand == null || ((BankEngineConstants.BANKCOMMAND_FINAL_STATUS).indexOf(currBankCommand.getTranSt()) <= -1)){
			BankRespDto inPara = new BankRespDto();
			inPara.setOriginalMsg(msg);
			inPara.setBnkNo(bnkNo);
			inPara.setApKind(BankEngineConstants.APKIND_PURCHASE);
			Result<BankRespDto> outPara = executeEngineService.writeResponsion(inPara);
			String respStatus = outPara.getModel().getRespStatus();

			beLogger.debug("应答回写结果：respStatus="+respStatus);

			if(BankEngineConstants.CMD_RESP_PAYED.equals(respStatus) ||
					BankEngineConstants.CMD_RESP_NOT_PAYED.equals(respStatus)){
				//send mq message
				sendMqMessage(serialNo);
			}
		}

		return "CloseDirectly";
	}


	/**
	 * 银联支付接收银行应答报文
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("payment/cpPayGetResponsion")
	public String paymentCp(HttpServletRequest request,HttpServletResponse response){
		HashMap<String, String> dataMap = new HashMap<String, String>();
		dataMap.put("VerNum", request.getParameter("VerNum"));
		dataMap.put("FundDateTime", request.getParameter("FundDateTime"));
		dataMap.put("InstuId", request.getParameter("InstuId"));
		dataMap.put("TransType", request.getParameter("TransType"));
		dataMap.put("ReturnUrl", request.getParameter("ReturnUrl"));
		dataMap.put("EncMsg", request.getParameter("EncMsg"));
		dataMap.put("CheckValue", request.getParameter("CheckValue"));

		beLogger.debug("银联应答信息 :dataMap="+dataMap.toString());

		PkgBodyBidRep pkgBodyBidRep = null;
		try{
			SingleVirement signleVirement = new SingleVirement();
			pkgBodyBidRep  = signleVirement.analyseCPPkgEncCheckFromBank(request.getParameter("EncMsg"), request.getParameter("CheckValue"));
			beLogger.debug("分析银行后的信息：="+pkgBodyBidRep.getPackage());
		}catch(Exception e){
			beLogger.error("解密银行返回的信息失败，EncMsg="+request.getParameter("EncMsg"));
		}

		beLogger.debug("分析银行信息得到的SerialNo="+pkgBodyBidRep.getFundSeqId());

		BankCommand currBankCommand = bankCommandService.getBankCommandBySerialNo(pkgBodyBidRep.getFundSeqId());
		beLogger.debug("获取银行指令currBankCommand="+currBankCommand);

		if(currBankCommand == null || ((BankEngineConstants.BANKCOMMAND_FINAL_STATUS).indexOf(currBankCommand.getTranSt()) <= -1)){
			BankRespDto inPara = new BankRespDto();
			inPara.setOriginalMsgMap(dataMap);
			inPara.setBnkNo("999");   //银联支付
			inPara.setApKind(BankEngineConstants.APKIND_PURCHASE);
			Result<BankRespDto> outPara = executeEngineService.writeResponsion(inPara);
			String respStatus = outPara.getModel().getRespStatus();

			beLogger.debug("应答回写结果：respStatus="+respStatus);

			if(BankEngineConstants.CMD_RESP_PAYED.equals(respStatus) ||
					BankEngineConstants.CMD_RESP_NOT_PAYED.equals(respStatus)){
				//send mq message
				sendMqMessage(pkgBodyBidRep.getFundSeqId());
			}

		}
		return "CloseDirectly";
	}


	/**
	 * 银联支付接收银行应答报文
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("payment/cp2PayGetResponsion")
//	@ResponseBody
	public void paymentCp2(HttpServletRequest request,HttpServletResponse response) throws IOException{
		HashMap<String, String> dataMap = new HashMap<String, String>();
		dataMap.put("version", request.getParameter("version"));
		dataMap.put("fundTransTime", request.getParameter("fundTransTime"));
		dataMap.put("instuId", request.getParameter("instuId"));
		dataMap.put("fundMerId", request.getParameter("fundMerId"));
		dataMap.put("transType", request.getParameter("transType"));
		dataMap.put("encMsg", request.getParameter("encMsg"));
		dataMap.put("signMsg", request.getParameter("signMsg"));
		dataMap.put("resv1", request.getParameter("resv1"));
		dataMap.put("resv2", request.getParameter("resv2"));
		dataMap.put("resv3", request.getParameter("resv3"));
		dataMap.put("resv4", request.getParameter("resv4"));

		beLogger.debug("银联应答信息 :dataMap="+dataMap.toString());

		Pkg2SubscribeResult result = null;
		try{
			SignMsgService signService = new SignMsgService();
			result  = signService.decodeSubscribeMsg( request.getParameter("encMsg"), request.getParameter("signMsg"));
			beLogger.debug("分析银行后的信息：="+result.toString());
		}catch(Exception e){
			beLogger.error("解密银行返回的信息失败，EncMsg="+request.getParameter("EncMsg"));
		}
		beLogger.debug("分析银行信息得到的SerialNo="+result.getFundSeqId());

		BankCommand currBankCommand = bankCommandService.getBankCommandBySerialNo(result.getFundSeqId());
		beLogger.debug("获取银行指令currBankCommand="+currBankCommand);

		if(currBankCommand == null || ((BankEngineConstants.BANKCOMMAND_FINAL_STATUS).indexOf(currBankCommand.getTranSt()) <= -1)){
			BankRespDto inPara = new BankRespDto();
			inPara.setOriginalMsgMap(dataMap);
			inPara.setBnkNo("MMM");   //银联支付
			inPara.setApKind(BankEngineConstants.APKIND_PURCHASE);
			Result<BankRespDto> outPara = executeEngineService.writeResponsion(inPara);
			String respStatus = outPara.getModel().getRespStatus();

			beLogger.debug("应答回写结果：respStatus="+respStatus);

			if(BankEngineConstants.CMD_RESP_PAYED.equals(respStatus) ||
					BankEngineConstants.CMD_RESP_NOT_PAYED.equals(respStatus)){
				//send mq message
				sendMqMessage(result.getFundSeqId());
			}
		}
		response.getWriter().print("chinapayok");
	}


	/**
	 *
	 * 汇天下支付接收银行应答报文
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping("payment/ChinaPnrPayGetResponsion")
	public String paymentChinaPnr(HttpServletRequest request,HttpServletResponse response) throws Exception{
		//获取银行返回信息
		HashMap<String,String> parametetMap = new HashMap<String, String>();
		parametetMap.put("EncMsg", request.getParameter("EncMsg"));
		parametetMap.put("CheckValue", request.getParameter("CheckValue"));

		String[] parameters = null;
		//解密信息
		try{
			com.fund.etrading.ebankapp.base.chinapnr.transaction.SingleVirement chinaPnr = new com.fund.etrading.ebankapp.base.chinapnr.transaction.SingleVirement();
			String plainMsg = chinaPnr.verifyAndDecodeMsg(request.getParameter("EncMsg"), request.getParameter("CheckValue"));
			beLogger.info("解密后的信息="+plainMsg);
			parameters = plainMsg.split("\\|");
		}catch(Exception e){

			e.printStackTrace();
		}

		BankCommand currBankCommand = bankCommandService.getBankCommandBySerialNo(parameters[3]);
		beLogger.debug("获取银行指令currBankCommand="+currBankCommand);

		if(currBankCommand == null || ((BankEngineConstants.BANKCOMMAND_FINAL_STATUS).indexOf(currBankCommand.getTranSt()) <= -1)){
			BankRespDto inPara = new BankRespDto();
			inPara.setOriginalMsgMap(parametetMap);
			inPara.setBnkNo("013");   //汇付天下
			inPara.setApKind(BankEngineConstants.APKIND_PURCHASE);
			Result<BankRespDto> outPara = executeEngineService.writeResponsion(inPara);
			String respStatus = outPara.getModel().getRespStatus();

			beLogger.debug("应答回写结果：respStatus="+respStatus);

			if(BankEngineConstants.CMD_RESP_PAYED.equals(respStatus) ||
					BankEngineConstants.CMD_RESP_NOT_PAYED.equals(respStatus)){
				//send mq message
				sendMqMessage(parameters[3]);
			}
		}
		return "CloseDirectly";
	}




	private String getBocomSerialNoFromMessage(final String message){
		String[] responseAry = message.split("\\|");
		if(responseAry.length >= 2){
			return responseAry[1];
		}else{
			return null;
		}
	}


	/**
	 * 改变编码格式
	 * @param parameterMap
	 * @return
	 */
	private HashMap<String, String> changeCoding(Map<?, ?> parameterMap){
		HashMap<String,String> parMap=new HashMap<String, String>();
	    Iterator<?> it = parameterMap.keySet().iterator();
		while (it.hasNext()) {
			String k = (String) it.next();
			String v = ((String[]) parameterMap.get(k))[0];
		    try {
				v = new String(v.getBytes("gbk"), "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			parMap.put(k, v);
			beLogger.debug("中行支付请求返回参数  :"+k+"="+v);
		}
		return parMap;
	}


	private void sendMqMessage(String serialNo){
		beLogger.info("B2C扣款发送MetaQ信息，serialNo:" + serialNo);
		BankResponsion responsion = bankResponsionMapper.findLatestBankResponsion(serialNo);
		BankCommand bankCommand = bankCommandService.getBankCommandBySerialNo(serialNo);

		if(bankCommand!=null && BankEngineConstants.BANKCOMMAND_FINAL_STATUS.indexOf(bankCommand.getTranSt())>-1){
			BeMetaQMessage qMessage = new BeMetaQMessage();
			qMessage.setSerialNo(serialNo);
			qMessage.setRefAppNo(bankCommand.getRefAppNo());
			qMessage.setMerTranCode(bankCommand.getMerTranCo());
			qMessage.setBankDate(responsion.getBankDate()+responsion.getBankTime());
			qMessage.setInputDate(responsion.getInputDate()+responsion.getInputTime());
			qMessage.setResposeMessage(responsion.getMerRespMsg());
			qMessage.setErrorMessage(null);
			qMessage.setTransactionStatus(TransactionStatus.valueOf(bankCommand.getTranSt()));

			beLogger.info("B2C扣款发送MetaQ信息，" + qMessage);
			MqSendUtils.sendAsyncBankCmmdMsg(qMessage);
		}else if(bankCommand == null){
			beLogger.error("银行引擎异常，bankCommand为null。serialNo=" + serialNo);
		}
	}
}
