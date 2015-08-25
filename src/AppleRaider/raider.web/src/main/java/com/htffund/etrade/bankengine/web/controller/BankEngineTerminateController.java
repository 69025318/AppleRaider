package com.htffund.etrade.bankengine.web.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fund.etrading.ebankapp.base.ab.api.ABConfig;
import com.fund.etrading.ebankapp.base.api.ConfigFactory;
import com.fund.etrading.ebankapp.base.api.LoggerFactory;
import com.fund.etrading.ebankapp.service.ExecuteEngineService;
import com.fund99.etrade.result.Result;
import com.hitrust.trustpay.client.TrxResponse;
import com.htffund.etrade.bankengine.BankEngineConstants;
import com.htffund.etrade.bankengine.biz.mq.MqSendUtils;
import com.htffund.etrade.bankengine.biz.util.BEConverter;
import com.htffund.etrade.bankengine.dao.BankCommandMapper;
import com.htffund.etrade.bankengine.dao.BankResponsionMapper;
import com.htffund.etrade.bankengine.dao.service.BankCommandService;
import com.htffund.etrade.bankengine.model.BankRespDto;
import com.htffund.etrade.bankengine.model.BeSignMetaQMessage;
import com.htffund.etrade.bankengine.model.TransactionStatus;
import com.htffund.etrade.bankengine.model.db.BankCommand;
import com.htffund.etrade.bankengine.model.db.BankResponsion;

@Controller
public class BankEngineTerminateController {

	private final static String CLOSE_DIRECTLY = "CloseDirectly";

	@Autowired
	ExecuteEngineService executeEngineService;

	@Resource
	BankCommandMapper bankCommandMapper;

	@Autowired
	BankCommandService bankCommandService;

	@Resource
	BankResponsionMapper bankResponsionMapper;

	Logger logger = LoggerFactory.getEBANKLogger();


	/**农行开户接收银行应答报文
	 * @param request
	 * @param response
	 *
	 */
	@RequestMapping("terminate/abTerminateGetResponsion")
	public String abTerminateGetResponsion(HttpServletRequest request, HttpServletResponse response){
			logger.info("进入解约servlet");

			String msg = request.getParameter("msg");
			logger.debug("农行解约返回msg:"+msg);

			ABConfig abConfig = ConfigFactory.getABConfig();
			String bnkNo = null;
			String bOrderId="";
			String bCardNo = "";
			String bIdNo = "";
			String bIdTp = "";
			String bRetMsg = "";
			TrxResponse tResponse = null;
			if (msg != null) {
				try {
					tResponse  = new TrxResponse(msg);
					bOrderId=tResponse.getOrderID();
					bCardNo = tResponse.getBankCardNo();
					bIdNo = tResponse.getCertificateNo();
					bIdTp = tResponse.getCertificateType();
					bRetMsg = tResponse.getErrorMessage();

					bnkNo = abConfig.getBnkNo();	//获取银行代码
					logger.debug("流水号serialNo="+bOrderId+" 银行号bankNo="+bnkNo+";cardNo="+bCardNo+";bIdno="+bIdNo+";bIdTp="+bIdTp+";bRetMsg="+bRetMsg);
				}catch(Exception e){
					logger.debug("取农行返回信息异常");
					return CLOSE_DIRECTLY;
				}
			}

			if(ifNoNeedToProcess(bOrderId)){
				return CLOSE_DIRECTLY;
			}

			BankRespDto inPara = new BankRespDto();
			inPara.setOriginalMsg(msg);
			inPara.setBnkNo(bnkNo);
			inPara.setApKind(BankEngineConstants.APKIND_UNSIGN);
			Result<BankRespDto> outPara = executeEngineService.writeResponsion(inPara);
			String respStatus = outPara.getModel().getRespStatus();
			logger.debug("应答回写结果：respStatus="+respStatus);

			sendMetaQAndUpdateBankCommandByRespStatus(respStatus,bOrderId,bOrderId);

			return CLOSE_DIRECTLY;
	}

	private void sendMetaQAndUpdateBankCommandByRespStatus(String respStatus, String serialNo, String protocolNo){
		if(BankEngineConstants.CMD_RESP_VERIFIED.equals(respStatus)){
			sendMetaQAndUpdateBankCommand(serialNo, protocolNo, TransactionStatus.Y);

		}else if(BankEngineConstants.CMD_RESP_NOT_VERIFIED.equals(respStatus)){
			sendMetaQAndUpdateBankCommand(serialNo, protocolNo, TransactionStatus.F);
		}
	}

	private void sendMetaQAndUpdateBankCommand(String serialNo,String protocolNo,TransactionStatus tranStatus){
		sendMqMessage(serialNo,protocolNo,tranStatus);

		bankCommandMapper.updateBankCommandtranStForOpen(tranStatus.toString(),BankEngineConstants.BANKCOMMAND_LOCKST_N, serialNo);
	}


	/**
	 * 发送MQ消息
	 * @param serialNo
	 */
	private void sendMqMessage(String serialNo,String protocolNo,TransactionStatus tranStatus){
		logger.info("解约发送MetaQ信息，serialNo:" + serialNo);
		BankResponsion responsion = bankResponsionMapper.findLatestBankResponsion(serialNo);
		BankCommand bankCommand = bankCommandService.getBankCommandBySerialNo(serialNo);

		if(bankCommand!=null){

			if(BankEngineConstants.BANKCOMMAND_FINAL_STATUS.indexOf(tranStatus.toString()) <= -1){
				logger.info("绑卡无需发送MetaQ信息，因为状态不正常。serialNo:" + serialNo);
				return;
			}

			BeSignMetaQMessage qMessage = new BeSignMetaQMessage();
			qMessage.setSerialNo(serialNo);
			if(responsion != null){
				qMessage.setMerchantRespCo(responsion.getMerRespCode());
				qMessage.setMerchantRespMsg(responsion.getMerRespMsg());
			}else{
				qMessage.setMerchantRespCo(null);
				qMessage.setMerchantRespMsg(null);
			}
			qMessage.setProtocolNo(protocolNo);
			qMessage.setAppKind(bankCommand.getRefAppKind());
			qMessage.setTransactionStatus(tranStatus);
			qMessage.setAccountNo(bankCommand.getSndrAcctNo());
			qMessage.setAccountName(bankCommand.getSndrAcctName());
			qMessage.setIdNo(bankCommand.getSndrIdNo());


			qMessage.setAccptmd(BEConverter.appSourceToAccptmd(bankCommand.getAppSource()));


			logger.info("解约发送MetaQ信息，" + qMessage);
			MqSendUtils.sendAsyncBankCmmdMsgForAuth(qMessage);
		}else if(bankCommand == null){
			logger.error("银行引擎异常，bankCommand为null。serialNo=" + serialNo);
		}
	}

	private boolean ifNoNeedToProcess(String serialNo){
		BankCommand bankCommand = bankCommandMapper.getBankCommandBySerialNo(serialNo);
		return ifNoNeedToProcess(bankCommand);
	}

	private boolean ifNoNeedToProcess(final BankCommand bankCommand){
		if(bankCommand==null){
			return false;
		}else{
			return BankEngineConstants.BANKCOMMAND_FINAL_STATUS.indexOf(bankCommand.getTranSt()) > -1;
		}
	}

}
