package com.htffund.etrade.bankengine.web.controller;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.annotation.Resource;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.allinpay.fps.bean.Response;
import com.allinpay.fps.bean.TxInfo;
import com.fund.etrading.ebankapp.base.ab.api.ABConfig;
import com.fund.etrading.ebankapp.base.allin.api.AllinConfig;
import com.fund.etrading.ebankapp.base.allin.transaction.AllinTx;
import com.fund.etrading.ebankapp.base.api.ConfigException;
import com.fund.etrading.ebankapp.base.api.ConfigFactory;
import com.fund.etrading.ebankapp.base.api.EBankParamConstant;
import com.fund.etrading.ebankapp.base.api.LoggerFactory;
import com.fund.etrading.ebankapp.base.api.ReturnProxy;
import com.fund.etrading.ebankapp.base.bocom.api.BOCOMConfig;
import com.fund.etrading.ebankapp.base.ccb.transaction.CCBSign;
import com.fund.etrading.ebankapp.base.ceb.api.CEBConfig;
import com.fund.etrading.ebankapp.base.ceb.tools.CebMerchantSign;
import com.fund.etrading.ebankapp.base.ceb.tools.XmlDoc;
import com.fund.etrading.ebankapp.base.cmbc.api.CMBCConfig;
import com.fund.etrading.ebankapp.base.cmbc.api.CMBCEncrypt;
import com.fund.etrading.ebankapp.base.cp.person.Person;
import com.fund.etrading.ebankapp.base.cp.pkg.PkgBodyRegisterRep;
import com.fund.etrading.ebankapp.base.cp2.pkg.Pkg2BidResult;
import com.fund.etrading.ebankapp.base.cp2.tools.SignMsgService;
import com.fund.etrading.ebankapp.base.dto.ResponsionDto;
import com.fund.etrading.ebankapp.base.dto.ReturnDto;
import com.fund.etrading.ebankapp.base.ecitic.api.EciticConfig;
import com.fund.etrading.ebankapp.base.ecitic.pkg.SignRes;
import com.fund.etrading.ebankapp.base.ecitic.tools.EciticSignTool;
import com.fund.etrading.ebankapp.base.gdb.api.GDBConfig;
import com.fund.etrading.ebankapp.base.gdb.api.XmlParse;
import com.fund.etrading.ebankapp.base.gdb.api.encrypt.DirectPaySignUtilWithApache;
import com.fund.etrading.ebankapp.base.spdb2.api.SPDB2Config;
import com.fund.etrading.ebankapp.base.spdb2.transaction.SPDB2Sign;
import com.fund.etrading.ebankapp.service.ExecuteEngineService;
import com.fund99.etrade.result.Result;
import com.hitrust.trustpay.client.TrxResponse;
import com.htffund.etrade.bankengine.BankEngineConstants;
import com.htffund.etrade.bankengine.biz.mq.MqSendUtils;
import com.htffund.etrade.bankengine.biz.service.impl.BankEngineCommonService;
import com.htffund.etrade.bankengine.biz.service.other.CmbBankCommandService;
import com.htffund.etrade.bankengine.biz.util.BEConverter;
import com.htffund.etrade.bankengine.biz.util.BEStringUtils;
import com.htffund.etrade.bankengine.dao.BankCommandMapper;
import com.htffund.etrade.bankengine.dao.BankCustInfoMapper;
import com.htffund.etrade.bankengine.dao.BankIdtpMapper;
import com.htffund.etrade.bankengine.dao.BankResponsionMapper;
import com.htffund.etrade.bankengine.dao.service.BankCardInfoSignService;
import com.htffund.etrade.bankengine.dao.service.BankCheckSignService;
import com.htffund.etrade.bankengine.dao.service.BankCommandService;
import com.htffund.etrade.bankengine.model.BankRespDto;
import com.htffund.etrade.bankengine.model.BeSignMetaQMessage;
import com.htffund.etrade.bankengine.model.CmbEnableProtocolRequest;
import com.htffund.etrade.bankengine.model.CmbEnableProtocolResponse;
import com.htffund.etrade.bankengine.model.TransactionStatus;
import com.htffund.etrade.bankengine.model.db.BankCheckSign;
import com.htffund.etrade.bankengine.model.db.BankCommand;
import com.htffund.etrade.bankengine.model.db.BankCustInfo;
import com.htffund.etrade.bankengine.model.db.BankResponsion;
import com.lsy.baselib.crypto.exception.ECCryptoProcessorException;
import com.thoughtworks.xstream.XStream;


@Controller
public class BankEngineAuthenticateController {

	private final static String CLOSE_DIRECTLY = "CloseDirectly";

	@Autowired
	BankCommandService bankCommandService;

	@Resource
	BankCommandMapper bankCommandMapper;

	@Autowired
	BankEngineCommonService bankengineCommonService;

	@Autowired
	ExecuteEngineService executeEngineService;

	@Resource
	BankIdtpMapper bankIdtpMapper;

	@Resource
	BankResponsionMapper bankResponsionMapper;

	@Resource
	BankCheckSignService bankCheckSignService;

	@Resource
	BankCustInfoMapper bankCustInfoMapper;

	@Resource
	CmbBankCommandService  cmbBankCommandService;

	@Resource
	BankCardInfoSignService bankCardInfoSignService;

	Logger logger = LoggerFactory.getEBANKLogger();



	/** 交行一键支付-签约-接收银行应答报文
	 * @param request
	 * @param reponse
	 */
	@RequestMapping("authenticate/bocom4AuthRegGetResponsion")
	public String bocom4AuthRegGetResponsion(HttpServletRequest request,HttpServletResponse reponse){

		// 通知格式为：商户号|商户协议号|银行协议号|订单日期|付款账号类型 0借记卡 2贷记卡|经过掩码处理的卡号|经过掩码处理的手机号|商户备注|银行备注|签名
		// 通知内容为：301310063009501|10069|BOC100000000283119|20150512|0|622262*********3875|158****2809|商户备注|6222620910010163875|xxxxx

		logger.info("交行一键支付-进入回调servlet");
		try {
			request.setCharacterEncoding("GBK");
		} catch (UnsupportedEncodingException e) {
			logger.error("交行一键支付-签约回调异常：", e);
			return CLOSE_DIRECTLY;
		}

		String  notifyMsg = request.getParameter("notifyMsg"); //获取银行通知结果
		logger.info("交行一键支付-签约应答报文:" + notifyMsg);
		String merAgreeNo = "";
		String agreeNo = "";
		HashMap map = new HashMap();

		// 解析报文
		try {
			String[] stringArray = notifyMsg.split("\\|");
			String merID = stringArray[0]; // 商户号
			merAgreeNo =stringArray[1]; // 商户协议号
			agreeNo = stringArray[2]; // 银行协议号
			String orderDate =stringArray[3]; // 订单日期
			String codeType = stringArray[4]; // 付款账号类型
			String codeNum =stringArray[5]; // 经过掩码处理的卡号
			String phone = stringArray[6]; // 经过掩码处理的手机号
			String merComment =stringArray[7]; // 商户备注
			String bankComment = stringArray[8]; // 银行备注
			String signData =stringArray[9]; // 签名
			logger.debug("商户号：merID=" + merID);
			logger.debug("商户协议号：merAgreeNo=" + merAgreeNo);
			logger.debug("银行协议号：agreeNo=" + agreeNo);
			logger.debug("订单日期：orderDate=" + orderDate);
			logger.debug("付款账号类型：codeType=" + codeType);
			logger.debug("卡号：codeNum=" + codeNum);
			logger.debug("手机号码：phone=" + phone);
			logger.debug("商户备注：merComment=" + merComment);
			logger.debug("银行备注：bankComment=" + bankComment);
			logger.debug("签名：signData=" + signData);

			map.put("merID", merID);
			map.put("merAgreeNo", merAgreeNo);
			map.put("agreeNo", agreeNo);
			map.put("orderDate", orderDate);
			map.put("codeType", codeType);
			map.put("codeNum", codeNum);
			map.put("phone", phone);
			map.put("merComment", merComment);
			map.put("bankComment", bankComment);
			map.put("signData", signData);
		} catch (Exception e) {
			logger.error("交行一键支付-解析通知报文错误：", e);
			return CLOSE_DIRECTLY;
		}


		BankCommand bankCommand = null;
		try {
			bankCommand = bankCommandService.getBankCommandBySerialNo(merAgreeNo);
			logger.info("bankcommdn=" + bankCommand);
			if(ifNoNeedToProcess(bankCommand)){
				return CLOSE_DIRECTLY;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 获得手机号码和卡号以便校验
		String sndrResv1 = bankCommand.getSndrResv1();
		String sndrAcctNo = bankCommand.getSndrAcctNo();
		logger.debug("指令里手机号码：sndrResv1=" + sndrResv1);
		logger.debug("指令里卡号：sndrAcctNo=" + sndrAcctNo);
		map.put("sndrResv1", sndrResv1);
		map.put("sndrAcctNo", sndrAcctNo);

		BankRespDto inPara = new BankRespDto();
		inPara.setOriginalMsgMap(map);
		inPara.setOriginalMsg(notifyMsg);
		inPara.setBnkNo("106"); // 表示中行的回写
		inPara.setApKind(BankEngineConstants.APKIND_AUTHENTICATE);
		Result<BankRespDto> outPara = executeEngineService.writeResponsion(inPara);
		String respStatus = outPara.getModel().getRespStatus();
		logger.debug("交行一键支付-应答回写结果：respStatus=" + respStatus);

		sendMetaQAndUpdateBankCommandByRespStatus(respStatus, bankCommand.getSerialNo(), (merAgreeNo+"|"+agreeNo));

		return CLOSE_DIRECTLY;
	}

	/**农行开户接收银行应答报文
	 * @param request
	 * @param response
	 *
	 */
	@RequestMapping("authenticate/abAuthGetResponsion")
	public String abAuthGetResponsion(HttpServletRequest request, HttpServletResponse response){
		try{
			logger.info("进入回调servlet");

			String msg = request.getParameter("msg");
			logger.debug("农行签约返回msg:"+msg);

			ABConfig abConfig = ConfigFactory.getABConfig();
			String bnkNo = null;
			String bOrderId="";
			HashMap<String,String> paraMap = new HashMap<String,String>();
			TrxResponse tResponse = null;
			if (msg != null) {
				try {
					tResponse  = new TrxResponse(msg);
					bOrderId=tResponse.getOrderID();
					paraMap.put("serialNo", bOrderId);
					bnkNo = abConfig.getBnkNo();	//获取银行代码
					logger.debug("流水号serialNo="+bOrderId+" 银行号bankNo="+bnkNo);
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
			inPara.setOriginalMsgMap(paraMap);
			inPara.setBnkNo(bnkNo);
			inPara.setApKind(BankEngineConstants.APKIND_AUTHENTICATE);
			Result<BankRespDto> outPara = executeEngineService.writeResponsion(inPara);
			String respStatus = outPara.getModel().getRespStatus();
			logger.debug("应答回写结果：respStatus="+respStatus);

			sendMetaQAndUpdateBankCommandByRespStatus(respStatus, bOrderId, bOrderId);

			return CLOSE_DIRECTLY;
		}catch(Exception e){
			logger.error("发生未知异常");
			return CLOSE_DIRECTLY;
		}
	}

	/** 中行开户接收银行应答报文
	 * @param request
	 * @param reponse
	 */
	@RequestMapping("authenticate/bocAuthRegGetResponsion")
	public String bocAuthRegGetResponsion(HttpServletRequest request,HttpServletResponse reponse){
		logger.info("进入回调servlet");

		Map<?, ?> parameterMap=request.getParameterMap();
		HashMap<String,String> parMap = changeCoding(parameterMap);
		String bankAcco=request.getParameter("acctNo");
		String idno=request.getParameter("identityNumber");
		String date=request.getParameter("verifyTime");
		String orderNo=parMap.get("orderNo");
		logger.debug("bankAcco="+bankAcco);
		logger.debug("idno="+idno);
		logger.debug("date="+date);
		logger.debug("orderNo="+orderNo);

		BankCommand bankCommand = null;
		try {
			bankCommand = bankCommandService.getBankCommandByMultiParameters("104", "63", "OPEN", bankAcco, idno);
			logger.info("bankcommnd="+bankCommand);

			parMap.put("serialNo", bankCommand.getSerialNo());

			if(ifNoNeedToProcess(bankCommand)){
				return CLOSE_DIRECTLY;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		String  holderName= bankCommand.getSndrAcctName();
		logger.debug("name="+holderName);
		parMap.put("holderName",holderName);

		BankRespDto inPara = new BankRespDto();
		inPara.setOriginalMsgMap(parMap);
		inPara.setBnkNo("104"); // 表示中行的回写
		inPara.setApKind(BankEngineConstants.APKIND_AUTHENTICATE);
		Result<BankRespDto> outPara = executeEngineService.writeResponsion(inPara);
		String respStatus = outPara.getModel().getRespStatus();
		logger.debug("应答回写结果：respStatus=" + respStatus);


		sendMetaQAndUpdateBankCommandByRespStatus(respStatus, bankCommand.getSerialNo(), null);

		return CLOSE_DIRECTLY;
	}

	/**
	 * 交行开户接收银行应答报文
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("authenticate/bocomAuthGetResponsion")
	public String bocomAuthGetResponsion(HttpServletRequest request, HttpServletResponse response){
		logger.info("进入回调servlet");

		String msg = request.getParameter("notifyMsg");
		logger.debug("交通银行签约返回信息："+msg);

		BOCOMConfig bocomConfig = ConfigFactory.getBOCOMConfig();
		String bnkNo = null;
		if (bocomConfig != null) {
			try {
				bnkNo = bocomConfig.getBnkNo();	//获取银行代码
			}
			catch (Exception e) {
				logger.error("获取交行配置出错",e);
			}
		}

		String serialNo = getBocomSerialNoFromMessage(msg);
		logger.info("流水号serialNo="+serialNo);

		if(ifNoNeedToProcess(serialNo)){
			return CLOSE_DIRECTLY;
		}

		BankRespDto inPara = new BankRespDto();
		inPara.setOriginalMsg(msg);
		inPara.setBnkNo(bnkNo);
		inPara.setApKind(BankEngineConstants.APKIND_AUTHENTICATE);
		Result<BankRespDto> outPara = executeEngineService.writeResponsion(inPara);
		String respStatus = outPara.getModel().getRespStatus();
		logger.debug("应答回写结果：respStatus="+respStatus);

		sendMetaQAndUpdateBankCommandByRespStatus(respStatus, serialNo, null);

		return CLOSE_DIRECTLY;
	}

	private String getBocomSerialNoFromMessage(final String message){
		int lastIndex = message.lastIndexOf("|");
		String srcMsg = message.substring(0, lastIndex+1);

		StringTokenizer stName = new StringTokenizer(srcMsg, "|");//拆解通知结果到Vector
		Vector<String> msgVc = new Vector<String>();
		int i =0;
		while (stName.hasMoreTokens()) {
			String value = (String)stName.nextElement();
			if (value.equals(""))
				value ="&nbsp;";
			msgVc.add(i++,value);
		}

		return msgVc.get(1);
	}


	/**
	 * 建行开户接收银行应答报文
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("authenticate/ccbAuthGetResponsion")
	public String ccbAuthGetResponsion(HttpServletRequest request, HttpServletResponse response){
		logger.info("进入回调servlet");

		String merchentId = request.getParameter("MERCHENT_ID");
		String accreditFlag = request.getParameter("ACCREDIT_FLAG");
		String accreditId = request.getParameter("ACCREDIT_ID");
		String txQuoTa = request.getParameter("TX_QUOTA")!=null?request.getParameter("TX_QUOTA"):"";
		String txDate = request.getParameter("TX_DATE")!=null?request.getParameter("TX_DATE"):"";
		String sign = request.getParameter("SIGN")!=null?request.getParameter("SIGN"):"";
		String type = request.getParameter("TYPE")!=null?request.getParameter("TYPE"):"";
		String referer = request.getParameter("REFERER")!=null?request.getParameter("REFERER"):"";
		String clientId = request.getParameter("CLIENTIP")!=null?request.getParameter("CLIENTIP"):"";
		logger.debug("建行返回签约标志，ACCREDIT_FLAG=" + accreditFlag);
		logger.debug("建行返回签名数据，SIGN=" + sign);
		logger.debug("建行返回签名数据，ACCREDIT_ID=" + accreditId);
		logger.debug("建行验签源数据=" + ("MERCHENT_ID=" + merchentId + "&ACCREDIT_FLAG=" + accreditFlag + "&ACCREDIT_ID=" + accreditId + "&TX_QUOTA=" + txQuoTa + "&TX_DATE=" + txDate + "&TYPE=" + type + "&REFERER=" + referer + "&CLIENTIP=" + clientId));

		HashMap<String,String> map = new HashMap<String,String>();
		map.put("MERCHENT_ID", merchentId);
		map.put("ACCREDIT_FLAG", accreditFlag);
		map.put("ACCREDIT_ID", accreditId);
		map.put("TX_QUOTA", txQuoTa);
		map.put("TX_DATE", txDate);
		map.put("SIGN", sign);
		map.put("TYPE", type);
		map.put("REFERER", referer);
		map.put("CLIENTIP", clientId);


		BankCommand bankCommand = bankCommandService.getBankCommandBySerialNo(accreditId);

		if(ifNoNeedToProcess(bankCommand)){
			return CLOSE_DIRECTLY;
		}

        // 建行签约返回，这里做客户-商户签约、解约
        CCBSign ccbsign = new CCBSign();
        String src = "MERCHENT_ID=" + merchentId + "&ACCREDIT_FLAG=" + accreditFlag + "&ACCREDIT_ID=" + accreditId
                + "&TX_QUOTA=" + txQuoTa + "&TX_DATE=" + txDate + "&TYPE=" + type + "&REFERER=" + referer
                + "&CLIENTIP=" + clientId;
        if (ccbsign.verifySigature(sign, src)) {
            logger.debug("更新ccb_creditid的状态");
            if (bankengineCommonService.updateCCBAccreditid(accreditId, "Y") > 0) {
                logger.debug("更新ccb_creditid的状态成功");

                String merIdTp = bankIdtpMapper.getMerIdType("005", bankCommand.getSndrIdType());

                logger.debug("回写BANKCHECK_SIGN表");
                BankCheckSign bankCheckSign = new BankCheckSign();
                bankCheckSign.setBankNo("005");
                bankCheckSign.setIdNo(bankCommand.getSndrIdNo());
                bankCheckSign.setIdTp(merIdTp);
                bankCheckSign.setBankAcco(bankCommand.getSndrAcctNo());
                bankCheckSign.setIdName(bankCommand.getSndrAcctName());
                bankCheckSign.setAccreditId(accreditId);
                bankCheckSign.setState("Y");

                bankCheckSignService.insertBankCheckSign(bankCheckSign);

            } else {
                logger.debug("更新ccb_creditid的状态失败！");
            }

        } else {
            logger.debug("建行签约、解约返回验证签名失败，签约失败！");
        }

        BankRespDto inPara = new BankRespDto();
        inPara.setOriginalMsgMap(map);
        inPara.setBnkNo("005");
        inPara.setApKind(BankEngineConstants.APKIND_AUTHENTICATE);
        Result<BankRespDto> outPara = executeEngineService.writeResponsion(inPara);
        String respStatus = outPara.getModel().getRespStatus();
        logger.debug("应答回写结果：respStatus=" + respStatus);

        logger.debug("发送MQ消息");
        sendMetaQAndUpdateBankCommandByRespStatus(respStatus, accreditId, accreditId);

		return CLOSE_DIRECTLY;
	}

	/**
	 * 通联开户接收银行应答报文
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("authenticate/allinAuthGetResponsion")
	public String allinAuthGetResponsion(HttpServletRequest request, HttpServletResponse response){
		logger.info("进入回调servlet");

		String msg = request.getParameter("NOTIFYMSG");
		logger.debug("通联签约返回消息msg:"+msg);

		if(StringUtils.isEmpty(msg)){
			return CLOSE_DIRECTLY;
		}

		AllinConfig allinConfig = ConfigFactory.getAllinConfig();
		String bnkNo = null;
		String serialNo = null;
		if (allinConfig != null) {
			try {
				bnkNo = allinConfig.getBnkNo();	//获取银行代码

				AllinTx allintx = new AllinTx(allinConfig);
				Response txResponse;
				txResponse = allintx.receiveResponseMsgV16(msg, allinConfig.getHtfCert());
				TxInfo _txInfo = txResponse.getTxInfo();
				serialNo = _txInfo.getTxTraceNo();

				logger.debug("流水号serialNo="+serialNo);
			}
			catch (Exception e) {
				logger.error("获取通联光大配置出错",e);
				return CLOSE_DIRECTLY;
			}
		}

		if(ifNoNeedToProcess(serialNo)){
			return CLOSE_DIRECTLY;
		}

		BankRespDto inPara = new BankRespDto();
		inPara.setOriginalMsg(msg);
		inPara.setBnkNo(bnkNo);
		inPara.setApKind(BankEngineConstants.APKIND_AUTHENTICATE);
		//Step3.执行引擎
		Result<BankRespDto> outPara = executeEngineService.writeResponsion(inPara);
		String respStatus = outPara.getModel().getRespStatus();

		sendMetaQAndUpdateBankCommandByRespStatus(respStatus, serialNo, null);

		return CLOSE_DIRECTLY;
	}


	/**
	 * 快钱开户接收银行应答报文
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("authenticate/bill99AuthGetResponsion")
	public String bill99AuthGetResponsion(HttpServletRequest request, HttpServletResponse response){
		logger.info("进入回调servlet");

		HashMap<String,String> map = new HashMap<String,String>();
		map.put("version", (request.getParameter("version") != null) ? request.getParameter("version"):"");
		map.put("signType", (request.getParameter("signType") != null)? request.getParameter("signType"):"");
		map.put("merchantMbrCode", (request.getParameter("merchantMbrCode") != null)? request.getParameter("merchantMbrCode"):"");
		map.put("requestId", (request.getParameter("requestId") != null)? request.getParameter("requestId"):"");
		map.put("memberEmail", (request.getParameter("memberEmail") != null)?request.getParameter("memberEmail"):"");
		map.put("memberName", (request.getParameter("memberName") != null)?request.getParameter("memberName"):"");
		map.put("memberIdType", (request.getParameter("memberIdType") != null)? request.getParameter("memberIdType"):"");
		map.put("memberIdNum", (request.getParameter("memberIdNum") != null)?request.getParameter("memberIdNum"):"");
		map.put("memberMobile",(request.getParameter("memberMobile") != null)?request.getParameter("memberMobile"):"");
		map.put("dealResult", (request.getParameter("dealResult") != null)?request.getParameter("dealResult"):"");
		map.put("uId",(request.getParameter("uId") != null)?request.getParameter("uId"):"");
		map.put("ext1",(request.getParameter("ext1") != null)?request.getParameter("ext1"):"");
		map.put("ext2",(request.getParameter("ext2") != null)?request.getParameter("ext2"):"");
		map.put("errCode", (request.getParameter("errCode") != null)?request.getParameter("errCode"):"");
		map.put("signMsg", (request.getParameter("signMsg") != null)? request.getParameter("signMsg"):"");

		logger.info("通联返回的信息="+map.toString());

		String serialNo = request.getParameter("requestId");

		if(ifNoNeedToProcess(serialNo)){
			return CLOSE_DIRECTLY;
		}

		BankRespDto inPara = new BankRespDto();
		inPara.setOriginalMsgMap(map);
		inPara.setBnkNo("199");
		inPara.setApKind(BankEngineConstants.APKIND_AUTHENTICATE);
		//Step3.执行引擎
		Result<BankRespDto> outPara = executeEngineService.writeResponsion(inPara);
		String respStatus = outPara.getModel().getRespStatus();

		sendMetaQAndUpdateBankCommandByRespStatus(respStatus, serialNo, null);

		return CLOSE_DIRECTLY;
	}


	/**
	 * 银联开户接收银行应答报文
	 * @param request
	 * @param response
	 * @return
	 */
	@Deprecated
	@RequestMapping("authenticate/ceb2AuthGetResponsion")
	public String ceb2AuthGetResponsion(HttpServletRequest request, HttpServletResponse response){
		logger.info("进入回调servlet");

		HashMap<String,String> map = new HashMap<String,String>();
		//获取银联返回信息
		map.put("VerNum",request.getParameter("VerNum"));
		map.put("FundDateTime",request.getParameter("FundDateTime"));
		map.put("InstuId",request.getParameter("InstuId"));
		map.put("TransType",request.getParameter("TransType"));
		map.put("ReturnUrl",request.getParameter("ReturnUrl"));
		map.put("EncMsg",request.getParameter("EncMsg"));
		map.put("CheckValue",request.getParameter("CheckValue"));

		logger.info("银联返回信息="+map.toString());

		Person person = null;
		PkgBodyRegisterRep pkgBodyRegisterRep = null;
		String serialNo = null;
		try {
			person = new Person();
			pkgBodyRegisterRep = person.analyseCPPkgEncCheckFromBank(request.getParameter("EncMsg"), request.getParameter("CheckValue"));
			serialNo = pkgBodyRegisterRep.getFundSeqId();
			logger.info("流水号serialNo="+serialNo);
		}
		catch (Exception e) {
			logger.error("接收应答：验签异常",e);
			return CLOSE_DIRECTLY;
		}

		if(ifNoNeedToProcess(serialNo)){
			return CLOSE_DIRECTLY;
		}

		BankRespDto inPara = new BankRespDto();
		inPara.setOriginalMsgMap(map);
		inPara.setBnkNo("011");
		inPara.setApKind(BankEngineConstants.APKIND_AUTHENTICATE);
		//Step3.执行引擎
		Result<BankRespDto> outPara = executeEngineService.writeResponsion(inPara);
		String respStatus = outPara.getModel().getRespStatus();

		sendMetaQAndUpdateBankCommandByRespStatus(respStatus, serialNo, null);

		return CLOSE_DIRECTLY;
	}


	/**
	 * 民生开户接收银行应答报文
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("authenticate/cmbcAuthGetResponsion")
	public String cmbcAuthGetResponsion(HttpServletRequest request, HttpServletResponse response){
		logger.info("进入回调servlet");

		String msg = request.getParameter("Result");
		logger.info("民生开户返回信息="+msg);

		CMBCConfig cmbcConfig = ConfigFactory.getCMBCConfig();
		String bnkNo = null;
		if (cmbcConfig != null) {
			try {
				bnkNo = cmbcConfig.getBnkNo();	//获取银行代码
			}
			catch (Exception e) {
				logger.error("获取民生配置出错",e);
			}
		}
		String serialNo = null;
		String plainText = CMBCEncrypt.DecryptData(msg); // 数据解密
		String[] results = plainText.split("\\|");
		if (results != null && results.length > 0) {
			serialNo =  results[8];
			logger.info("流水号serialNo="+serialNo);
		}

		if(ifNoNeedToProcess(serialNo)){
			return CLOSE_DIRECTLY;
		}

		BankRespDto inPara = new BankRespDto();
		inPara.setOriginalMsg(msg);
		inPara.setBnkNo(bnkNo);
		inPara.setBnkSerialNo(serialNo);
		inPara.setApKind(BankEngineConstants.APKIND_AUTHENTICATE);
		//Step3.执行引擎
		Result<BankRespDto> outPara = executeEngineService.writeResponsion(inPara);
		String respStatus = outPara.getModel().getRespStatus();

		sendMetaQAndUpdateBankCommandByRespStatus(respStatus, serialNo, null);

		return CLOSE_DIRECTLY;

	}


	/**
	 * 银联开户接收银行应答报文
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("authenticate/cpAuthGetResponsion")
	public String cpAuthGetResponsion(HttpServletRequest request, HttpServletResponse response){
		logger.info("进入回调servlet");

		HashMap<String,String> map = new HashMap<String,String>();
		//获取银联返回信息
		map.put("VerNum",request.getParameter("VerNum"));
		map.put("FundDateTime",request.getParameter("FundDateTime"));
		map.put("InstuId",request.getParameter("InstuId"));
		map.put("TransType",request.getParameter("TransType"));
		map.put("ReturnUrl",request.getParameter("ReturnUrl"));
		map.put("EncMsg",request.getParameter("EncMsg"));
		map.put("CheckValue",request.getParameter("CheckValue"));

		logger.info("银联开户返回信息="+map.toString());

		Person person = null;
		PkgBodyRegisterRep pkgBodyRegisterRep = null;
		String serialNo = null;
		try {
			person = new Person();
			pkgBodyRegisterRep = person.analyseCPPkgEncCheckFromBank(request.getParameter("EncMsg"), request.getParameter("CheckValue"));
			serialNo = pkgBodyRegisterRep.getFundSeqId();
			logger.info("流水号serialNo="+serialNo);
		}
		catch (Exception e) {
			logger.error("接收应答：验签异常",e);
			return CLOSE_DIRECTLY;
		}

		if(ifNoNeedToProcess(serialNo)){
			return CLOSE_DIRECTLY;
		}

		BankRespDto inPara = new BankRespDto();
		inPara.setOriginalMsgMap(map);
		inPara.setBnkNo("999");
		inPara.setBnkSerialNo(serialNo);
		inPara.setApKind(BankEngineConstants.APKIND_AUTHENTICATE);
		//Step3.执行引擎
		Result<BankRespDto> outPara = executeEngineService.writeResponsion(inPara);
		String respStatus = outPara.getModel().getRespStatus();

		sendMetaQAndUpdateBankCommandByRespStatus(respStatus, serialNo, null);

		return CLOSE_DIRECTLY;

	}

	/**
	 * 中信银行开户接收银行应答报文
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("authenticate/eciticAuthGetResponsion")
	public String eciticAuthGetResponsion(HttpServletRequest request, HttpServletResponse response){
		logger.info("进入回调servlet");

		HashMap<String,String> msgMap = new HashMap<String,String>();
		msgMap.put("signMsg",request.getParameter("SIGNREQMSG"));

		logger.info("中信开户返回信息="+msgMap);

		EciticConfig eciticConfig = ConfigFactory.getEciticConfig();
		String bnkNo = null;
		if (eciticConfig != null) {
			try {
				bnkNo = eciticConfig.getBnkNo();
			} catch (Exception e) {
				logger.error("获取中信配置出错", e);
			}
		}

		String result = null;
		SignRes signres = new SignRes();
		try {
			result = new EciticSignTool().getOriginalMessage(request.getParameter("SIGNREQMSG"));
			XStream xstream = new XStream();
			xstream.alias("stream", signres.getClass());
			signres = (SignRes) xstream.fromXML(result);
		} catch (ECCryptoProcessorException e) {
			logger.error("解签失败");
		}

		String serialNo = signres.getORDERNO();
		msgMap.put("serialNo", serialNo);
		logger.info("流水号serialNo="+serialNo);

		if(ifNoNeedToProcess(serialNo)){
			return CLOSE_DIRECTLY;
		}

		BankRespDto inPara = new BankRespDto();
		inPara.setOriginalMsgMap(msgMap);
		inPara.setBnkNo(bnkNo);
		inPara.setApKind(BankEngineConstants.APKIND_AUTHENTICATE);
		//Step3.执行引擎
		Result<BankRespDto> outPara = executeEngineService.writeResponsion(inPara);
		String respStatus = outPara.getModel().getRespStatus();
		String protocolNo = outPara.getModel().getBnkSerialNo();

		sendMetaQAndUpdateBankCommandByRespStatus(respStatus, serialNo, protocolNo);

		return CLOSE_DIRECTLY;
	}


	/**
	 * 邮储银行开户接收银行应答报文
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("authenticate/psbcAuthGetResponsion")
	public String psbcAuthGetResponsion(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException{
		logger.info("进入回调servlet");

		request.setCharacterEncoding("GBK");
		String plain = request.getParameter("Plain");
		String signature = request.getParameter("Signature");
		String[] str = plain.split("\\|");

		HashMap<String,String> map = new HashMap<String,String>();
		map.put("Plain", plain);
		map.put("Signature", signature);

		for(int i=0;i<str.length;i++){
		    if("SignNo".equals(str[i].split("=")[0]) || "RespCode".equals(str[i].split("=")[0])
		    	 || "UserID".equals(str[i].split("=")[0])){
		   	 map.put(str[i].split("=")[0],str[i].split("=")[1]);
		    }
		}

		logger.info("邮储开户返回信息="+ map);

		String signNo = map.get("SignNo");
		String serialNo = map.get("UserID");
		logger.info("支付协议号="+signNo);
		logger.info("银行流水号="+serialNo);

		if(ifNoNeedToProcess(serialNo)){
			return CLOSE_DIRECTLY;
		}

		BankRespDto inPara = new BankRespDto();
		inPara.setOriginalMsgMap(map);
		inPara.setBnkNo("015");
		inPara.setApKind(BankEngineConstants.APKIND_AUTHENTICATE);
		//Step3.执行引擎
		Result<BankRespDto> outPara = executeEngineService.writeResponsion(inPara);
		String respStatus = outPara.getModel().getRespStatus();
		String protocolNo = outPara.getModel().getBnkSerialNo();

		sendMetaQAndUpdateBankCommandByRespStatus(respStatus, serialNo, protocolNo);

		return CLOSE_DIRECTLY;
	}

	/**改变编码格式
	 * @param parameterMap
	 * @return
	 */
	private HashMap<String, String> changeCoding(Map<?, ?> parameterMap){
		HashMap<String,String> parMap=new HashMap<String, String>();
	    Iterator<?> it = parameterMap.keySet().iterator();
		while (it.hasNext()) {
			String k = (String) it.next();
			String v = ((String[]) parameterMap.get(k))[0];
			logger.debug("中行开户请求返回参数  原文:"+k+"="+v);
	 		try {
				v = new String(v.getBytes("gbk"), "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			parMap.put(k, v);
			logger.debug("中行开户请求返回参数  :"+k+"="+v);
		}
		return parMap;
	}

	/**
	 * @auth yuangb
	 * @createDate 2014年9月19日
	 * @desc 招行开户接收银行应答报文
	 */
	@RequestMapping("authenticate/cmbAuthGetResponsion")
	public String cmbAuthGetResponsion(HttpServletRequest request,
			HttpServletResponse response) {
		logger.info("<<<<<<<<招商直连签权开始>>>>>>>>>>");
		//1.获取request传递信息
		String uin       = request.getParameter("uin"); // 协议号
		String timeStamp = request.getParameter("TimeStamp"); // (时间戳: 用于生成
		String checkSum  = request.getParameter("CheckSum");
		logger.debug("uin=" + uin+" timeStamp=" + timeStamp+" checkSum=" + checkSum);

		//2.基本逻辑校验
		String protocolNo      =  uin;
		String openSerialNo    =  uin;
		/*查询开始时的BANK_COMMAND指令*/
		BankCommand bankCommand = bankCommandService.getBankCommandBySerialNo(openSerialNo);
		logger.info("bankcommnd="+bankCommand);

		if(ifNoNeedToProcess(bankCommand)){
			return CLOSE_DIRECTLY;
		}

		String cmandInput      = bankCommand.getSndrAcctNo();
		/*查询BANK_CUSTINFO表（该表数据是由银行监听程序写入）*/
		BankCustInfo custInfo  = bankCustInfoMapper.getCustInfoByPtlNo(protocolNo);

		if(custInfo==null||BEStringUtils.lessLength(cmandInput, 4)||BEStringUtils.lessLength(custInfo.getInputAcctNo(), 4)){
		    logger.error("<<<<<<招商直连custinfo表银行账号长度小于4,流水号:"+openSerialNo);

		    return CLOSE_DIRECTLY;
		}

		cmandInput = cmandInput.substring(cmandInput.length()-4,cmandInput.length());
		String custInput      = custInfo.getInputAcctNo();
		 custInput  = custInput.substring(custInput.length()-4,custInput.length());
		if(!custInput.equals(cmandInput)){
			logger.error("<<<<<<招商直连custinfo表卡号比较不一致,流水号:"+openSerialNo);
			return CLOSE_DIRECTLY;
		}


		//3.writeResponsion 实际上是验证request传递信息
		HashMap<String, String> msgMap = new HashMap<String, String>();
		msgMap.put("uin", uin);
		msgMap.put("TimeStamp", timeStamp);
		msgMap.put("CheckSum", checkSum);
		msgMap.put("mcAccountNo", bankCommand.getSndrAcctNo());
		BankRespDto inPara = new BankRespDto();
		inPara.setOriginalMsgMap(msgMap);
		inPara.setBnkNo(bankCommand.getBnkNo());
		inPara.setApKind(BankEngineConstants.APKIND_AUTHENTICATE);
		logger.debug("<<<<<招商直连回写开始>>>>>>>>>");
		Result<BankRespDto> outPara = executeEngineService.writeResponsion(inPara);

		String respStatus= outPara.getModel().getRespStatus();
		if (!BankEngineConstants.CMD_RESP_VERIFIED.equals(respStatus)) {
			logger.error("<<<<<招商直连应答回写结果未知 "+respStatus);
			sendMetaQAndUpdateBankCommand(openSerialNo, protocolNo, TransactionStatus.F);
			return CLOSE_DIRECTLY;
		}

	    //开户指令tranSts 置为 Y状态
		bankCommandMapper.updateBankCommandtranStForOpen(BankEngineConstants.BANKCOMMAND_SUCCESS,"N",openSerialNo);


		//4.启动协议
		/*组装resust*/
		CmbEnableProtocolRequest cmbReq = new CmbEnableProtocolRequest();
		cmbReq.setBnkNo(bankCommand.getBnkNo());
		cmbReq.setTranTp("P");
		cmbReq.setMerTranCo("65");
		cmbReq.setCurrency("156");
		cmbReq.setRefAppKind("001");
		cmbReq.setProtocolNo(protocolNo);
		cmbReq.setBnkAcctNo(bankCommand.getSndrAcctNo());
		cmbReq.setBnkAcctNm(bankCommand.getSndrAcctName());
		cmbReq.setBnkIdTp(bankCommand.getSndrIdType());
		cmbReq.setBnkIdNo(bankCommand.getSndrIdNo());
		cmbReq.setCustNo(custInfo.getBnkCustno());
		cmbReq.setInvTp(bankCommand.getSndrIdType());
		cmbReq.setLockSt("N");
		cmbReq.setTranSt("N");
		cmbReq.setRefAppNo(openSerialNo);

		/*调用启动协议接口*/
		logger.info("<<<<<招商直连启动协议接口开始:\n"+cmbReq);
		CmbEnableProtocolResponse cmbResp=cmbBankCommandService.enableProtocol(cmbReq);
		logger.info("<<<<<招商直连启动协议接口结束:\n"+cmbResp);

		//5.发送mq
		sendMetaQAndUpdateBankCommand(openSerialNo, protocolNo, cmbResp.getTransactionStatus());

	    logger.info("<<<<<<<<招商直连签权结束>>>>>>>>>>");
		return CLOSE_DIRECTLY;
	}

	/**
	 * @auth yuangb
	 * @createDate 2014年9月30日
	 * @desc 工行开户接收银行应答报文
	 */
	@RequestMapping("authenticate/icbcAuthGetResponsion")
	public String icbcAuthGetResponsion(HttpServletRequest request, HttpServletResponse response) {
		logger.info("<<<<<<<<工行直连签权开始>>>>>>>>>>");

		// 获取参数
		String openSerialNo = request.getParameter("merVAR"); // 协议号
		String notifyData = request.getParameter("notifyData"); // 通知结果数据
		String signMsg = request.getParameter("signMsg"); // 银行对通知结果的签名数据
		logger.info("工行直连签权-协议号"+openSerialNo);
		logger.info("工行直连签权-通知结果数据---原文"+notifyData);
		logger.info("工行直连签权-银行对通知结果的签名数据---原文"+signMsg);

		BankCommand bankCommand = null;
		try {
			bankCommand = bankCommandService.getBankCommandBySerialNo(openSerialNo);
			logger.info("bankcommdn=" + bankCommand);
			if(ifNoNeedToProcess(bankCommand)){
				return CLOSE_DIRECTLY;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		BankRespDto inPara = new BankRespDto();
		inPara.setOriginalMsg(notifyData);
		inPara.setBnkNo("002"); // 表示工行的回写
		inPara.setBnkSerialNo(openSerialNo);
		inPara.setApKind(BankEngineConstants.APKIND_AUTHENTICATE);
		Result<BankRespDto> outPara = executeEngineService.writeResponsion(inPara);
		String respStatus = outPara.getModel().getRespStatus();
		logger.debug("工行直连签权-应答回写结果：respStatus=" + respStatus);

		sendMetaQAndUpdateBankCommandByRespStatus(respStatus, openSerialNo, openSerialNo);
		return CLOSE_DIRECTLY;
	}



	/**
	 * 浦发直连开户接收银行应答报文
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("authenticate/spdb2AuthGetResponsion")
	public String spdb2AuthGetResponsion(HttpServletRequest request, HttpServletResponse response){
		logger.info("进入回调servlet");

		//获取浦发返回信息
		String Plain = request.getParameter("Plain");
		String Signature = request.getParameter("Signature");
		logger.debug("浦发银行签约返回数据: "+Plain);

		if(Plain!=null && !"".equals(Plain) && !"null".equals(Plain)){
			String[] str = Plain.split("\\|");
			String Merc_id = "";
			String RespCode = "";
			for(int i=0; i<str.length; i++){
				String str0 = str[i].split("=")[0];
				String str1 = str[i].split("=")[1] !=null ? str[i].split("=")[1] : "";
				if("Merc_id".equals(str0)){
					Merc_id = str1;
				}else if("RespCode".equals(str0)){
					RespCode = str1;
				}
			}

			BankCommand bankCommand = bankCommandService.getBankCommandBySerialNo(Merc_id);

			if(ifNoNeedToProcess(bankCommand)){
				return CLOSE_DIRECTLY;
			}

			if("00".equals(RespCode)){
				boolean result = false;
				SPDB2Config spdb2Config = ConfigFactory.getSPDB2Config();

				try {
					if(!"Y".equals(spdb2Config.getTestFlag())){
						result = SPDB2Sign.verifySigature(Signature,Plain);
					}else{
						result = true;
					}
				} catch (ConfigException e) {
					logger.debug("浦发银行签约返回结果：验签失败！");
				}

				if(result){
					logger.debug("浦发银行签约返回结果：验签成功！");

					bankengineCommonService.updateSPDB2Accreditid(Merc_id, "Y");

					logger.debug("发送MQ消息");
					sendMetaQAndUpdateBankCommand(Merc_id, Merc_id, TransactionStatus.Y);

					return CLOSE_DIRECTLY;
				}else{
					logger.debug("浦发银行签约返回结果：验签失败！");
				}
			}else{
				logger.debug("浦发银行签约返回结果：签约失败！");

				sendMetaQAndUpdateBankCommand(Merc_id, Merc_id, TransactionStatus.F);

				return CLOSE_DIRECTLY;
			}

		}

		return CLOSE_DIRECTLY;
	}

	/**
	 * 光大开户接收银行应答（直接关闭）
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("authenticate/cebAuthGetResponsionClose")
	public String cebAuthGetResponsionClose(HttpServletRequest request, HttpServletResponse response){
		return  CLOSE_DIRECTLY;
	}

	/**
	 * 光大开户接收银行应答报文
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping("authenticate/cebAuthGetResponsion")
	public void cebAuthGetResponsion(HttpServletRequest request, HttpServletResponse response) throws IOException{
		logger.debug("开始接收光大银行返回报文");

		BufferedReader br;
		String signature="";
		try {
			br = new BufferedReader(new InputStreamReader(request.getInputStream(),"UTF-8"));
		    String line = null;
		    StringBuffer sb = new StringBuffer();
		 	while((line = br.readLine())!=null){
		       sb.append(line+"\n");
			}

		 	if(sb==null || "".equals(sb) || "null".equals(sb)){
		 		logger.error("获取银行通知信息失败！ ");
		 		return;
		 	}

			logger.debug("接收光大银行返回报文XML:"+sb.toString());

			String resultXml=sb.toString();
			SAXReader reader = new SAXReader();
			Document doc = reader.read(new StringReader(resultXml));
			String merId=doc.selectSingleNode("//merId").getText();
			String id=doc.selectSingleNode("//Message//@id").getText();
			String resTransId=doc.selectSingleNode("//Message//Plain//@id").getText();
			String bankSerialNo=id;

			if("TSReq".equals(resTransId)){
				logger.debug("光大银行网上签约接口TSReq通知报文ID为："+id);
				String signNo = doc.selectSingleNode("//signNo").getText();
				String cardNo=doc.selectSingleNode("//cardNo").getText();  //银行卡后六位
				String certType=doc.selectSingleNode("//certType").getText(); //证件类型
				String certNo=doc.selectSingleNode("//certNo").getText(); //证件号码

				boolean signFlag = false;
				CEBConfig cebConfig = ConfigFactory.getCEBConfig();
				if(!"Y".equals(cebConfig.getTestFlag())){
					signFlag=CebMerchantSign.merchantVerifyXmlData(resultXml);
				}else{
					signFlag = true;
				}
				logger.debug("光大银行网上签约接口解约结果为："+signFlag);

				String transId="TSRes";

				logger.debug("cardNo="+cardNo+"certNo="+certNo);
				BankCommand bankCommand = bankCommandService.getBankCommandByMultiParameters("112", "63", "OSReq", cardNo, certNo);
				logger.debug("bankCommand指令="+bankCommand);

				if(ifNoNeedToProcess(bankCommand)){
					return ;
				}

				if(signFlag){
					//明文
					String plain=XmlDoc.createXml(new String[]{"id","transId","merId","signNo"},
						 new String[]{bankSerialNo,transId,merId,signNo});
					logger.debug("光大银行网上签约接口报文明文为："+plain);
					//加密
					if(!"Y".equals(cebConfig.getTestFlag())){
						signature=CebMerchantSign.merchantSignXmlData(plain, transId);
					}else{
						signature = plain;
					}
					logger.debug("光大银行网上签约接口请求报文为："+signature);

					bankengineCommonService.updateCEBAccreditid(certNo,certType,cardNo,signNo,"Y");
					logger.debug("更新ceb_creditid的状态成功");

					String merIdTp = bankIdtpMapper.getMerIdType("112", bankCommand.getSndrIdType());

					logger.debug("回写BANKCHECK_SIGN表");
					BankCheckSign bankCheckSign = new BankCheckSign();
					bankCheckSign.setBankNo("112");
					bankCheckSign.setIdNo(bankCommand.getSndrIdNo());
					bankCheckSign.setIdTp(merIdTp);
					bankCheckSign.setBankAcco(bankCommand.getSndrAcctNo());
					bankCheckSign.setIdName(bankCommand.getSndrAcctName());
					bankCheckSign.setAccreditId(signNo);
					bankCheckSign.setState("Y");

					bankCheckSignService.insertBankCheckSign(bankCheckSign);
					logger.debug("回写BANKCHECK_SIGN表成功");

					sendMetaQAndUpdateBankCommand(bankCommand.getSerialNo(), signNo, TransactionStatus.Y);
				}else{
					logger.debug("报文签名校验不通过");

					String plain=XmlDoc.createXml(new String[]{"id","transId","merId","errorCode","errorMessage","errorDetail"},
							 new String[]{bankSerialNo,"Error",merId,"0007","签名无效","报文签名校验不通过"});
					logger.debug("光大银行网上签约接口报文明文为："+plain);
					//加密
					if(!"Y".equals(cebConfig.getTestFlag())){
						signature=CebMerchantSign.merchantSignXmlData(plain, "Error");
					}else{
						signature = plain;
					}

					sendMetaQAndUpdateBankCommand(bankCommand.getSerialNo(), signNo, TransactionStatus.F);
				}

				response.getWriter().print(signature);
			}
		}catch(Exception e){
			logger.error("获取银行通知信息失败！"+e);
		}
	}

	/**
	 * 广发开户接收银行应答报文
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("authenticate/gdbAuthGetResponsion")
	public void gdbAuthGetResponsion(HttpServletRequest request, HttpServletResponse response){
		logger.debug("广发银行签约返回数据开始接收");

		String plain = null;
		ByteArrayOutputStream baos = null;
		ServletInputStream in = null;
		try {
		    in = request.getInputStream();
			int i = -1;
			baos = new ByteArrayOutputStream();
			while ((i = in.read()) != -1) {
				baos.write(i);
			}
			baos.flush();
			plain = baos.toString("UTF-8");
			baos.close();
			in.close();
		} catch (IOException e) {
			logger.error("获取广发返回数据流错误");
			return ;
		}

		if(plain==null || "".equals(plain) || "null".equals(plain)){
			logger.error("获取银行返回信息为空");
			return ;
		}
		//获取广发返回信息
		logger.debug("广发银行签约返回数据: "+plain);

		GDBConfig gdbConfig = ConfigFactory.getGDBConfig();
		String testFlag = null;
		String publicKey = null;
		String version = null;
		String instId = null;
		String certId = null;
		try {
			testFlag = gdbConfig.getTestFlag();
			publicKey = gdbConfig.getPublicKey();
			version = gdbConfig.getVersion(); // 版本号
			instId = gdbConfig.getInstId(); // 商户标识在配置文件中读取
			certId = gdbConfig.getCertId(); // certId
		} catch (ConfigException e) {
			logger.error("获取配置信息错误");
			return ;
		}

		XmlParse xmlParse = new XmlParse();
		Document doc = xmlParse.readDocumentFromString(plain);
		Element rootElement = doc.getRootElement();

		Element Message = rootElement.element("Message");
		Element CSReq = Message.element("CSReq");

		String respSignNo = "";
		String respCardType = "";
		String respSerialNo = "";

		if (CSReq != null) {
			for (Iterator iterator = CSReq.elementIterator(); iterator
					.hasNext();) {
				Element subElement = (Element) iterator.next();
				if ("signNo".equals(subElement.getName())) {
					respSignNo = subElement.getText();
				}else if ("email".equals(subElement.getName())) {	//注意：email用来记录流水号
					respSerialNo = subElement.getText();
				} else if ("cardType".equals(subElement.getName())) {
					respCardType = subElement.getText();
				}
			}
			boolean verifyResult = true;
			/** 模拟器测试标识 */
			if(!"Y".equals(testFlag)) {
				verifyResult = DirectPaySignUtilWithApache.verifyXml(publicKey, plain);
			}
			logger.debug(verifyResult == true ? "验签通过" : "验签不通过");

			BankCommand bankCommand = bankCommandMapper.getBankCommandBySerialNo(respSerialNo);

			if(ifNoNeedToProcess(bankCommand)){
				return ;
			}

			String xmlStr = "";

			if(verifyResult && !"C".equals(respCardType)){
				logger.debug("广发银行签约返回结果：验签成功！");
				logger.debug("广发银行签约返回结果：验签成功！respSerialNo:"+respSerialNo+",respSignNo:"+respSignNo);
				bankengineCommonService.updateGDBAccreditid(respSerialNo, "Y");
				logger.debug("更新ccb_creditid的状态成功");

				sendMetaQAndUpdateBankCommand(respSerialNo, respSignNo, TransactionStatus.Y);

				String[] eleName = { "Message", "CSRes", "version", "instId", "certId",	"signNo" };
				String[] eleValue = { "Message", "CSRes", version, instId, certId, respSignNo };
				xmlStr = xmlParse.createXml(eleName, eleValue); // 生成请求XML
			}else{
				logger.debug("广发银行签约返回结果：验签失败！");

				sendMetaQAndUpdateBankCommand(respSerialNo, respSignNo, TransactionStatus.F);

				String eleName[] = { "Message", "Error", "version", "instId", "certId",	"errorCode", "errorMessage", "vendorCode" };
				String[] eleValue = { "Message", "Error", version, instId, certId, "9999", "验签失败", "" };
				xmlStr = xmlParse.createXml(eleName, eleValue); // 生成请求XML
			}

			try {
				logger.debug(xmlStr);
				String password = gdbConfig.getPrivateKeyPasswd();
				String privateKeyPath = gdbConfig.getPrivateKey();
				xmlStr = DirectPaySignUtilWithApache.signXml(privateKeyPath, password, xmlStr);
				logger.debug(xmlStr);

				response.getOutputStream().write(xmlStr.getBytes("UTF-8"));
			} catch (Exception e) {
				logger.error("返回广发签约消息错误。serialNo:" + respSerialNo + ", 原因是：" + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	/**
	 * 广发开户接收银行应答报文
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("authenticate/gdbAuthGetResponsionClose")
	public String gdbAuthGetResponsionClose(HttpServletRequest request, HttpServletResponse response){
		return  CLOSE_DIRECTLY;
	}


	/**
	 * 上海银行开户接收银行应答报文
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("authenticate/shAuthGetResponsion")
	public String shAuthGetResponsion(HttpServletRequest request, HttpServletResponse response){
		logger.debug("上海银行签约返回数据开始接收");

		HashMap<String,String> map = new HashMap<String,String>();
		//获取银联返回信息
		map.put("instId",request.getParameter("instId"));
		map.put("date",request.getParameter("date"));
		map.put("signNo",request.getParameter("signNo"));
		map.put("cell",request.getParameter("cell"));
		map.put("cardType",request.getParameter("cardType"));
		map.put("cardNo",request.getParameter("cardNo"));
		map.put("resultCode",request.getParameter("errorCode"));
		map.put("resultMessage",request.getParameter("errorMessage"));
		map.put("signData",request.getParameter("signData"));

		logger.debug("应答信息：map="+map);

		String serialNo = request.getParameter("cell");
		logger.debug("流水号serialNo="+serialNo);

		if(ifNoNeedToProcess(serialNo)){
			return CLOSE_DIRECTLY;
		}

		BankRespDto inPara = new BankRespDto();
		inPara.setOriginalMsgMap(map);
		inPara.setBnkNo("018"); // 表示中行的回写
		inPara.setApKind(BankEngineConstants.APKIND_AUTHENTICATE);
		Result<BankRespDto> outPara = executeEngineService.writeResponsion(inPara);
		String respStatus = outPara.getModel().getRespStatus();
		String protoNo = outPara.getModel().getBnkSerialNo();
		logger.debug("应答回写结果：respStatus=" + respStatus+"协议号:protoNo="+protoNo);

		sendMetaQAndUpdateBankCommandByRespStatus(respStatus, serialNo, protoNo);

		return  CLOSE_DIRECTLY;

	}

	/**
	 * 手机银联开户
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("authenticate/cp2AuthGetResponsion")
	public void cp2AuthGetResponsion(HttpServletRequest request, HttpServletResponse response) throws IOException{
		logger.debug("手机银联签约返回数据开始接收");

		String version = request.getParameter("version"); //版本号
		String fundTransTime = request.getParameter("fundTransTime"); //基金公司交易时间
		String instuId = request.getParameter("instuId"); //机构号
		String fundMerId = request.getParameter("fundMerId"); //基金公司商户号
		String transType = request.getParameter("transType"); //交易类型
		String encMsg = request.getParameter("encMsg"); //加密密文
		String signMsg = request.getParameter("signMsg"); //交易数据签名
		String resv1 = request.getParameter("resv1"); //保留字段
		String resv2 = request.getParameter("resv2"); //保留字段
		String resv3 = request.getParameter("resv3"); //保留字段
		String resv4 = request.getParameter("resv4"); //保留字段
		//写日志
		logger.info("version = " + version+" fundTransTime = " + fundTransTime+" instuId = " + instuId+" fundMerId = " + fundMerId +
				" transType = " + transType+" encMsg = " + encMsg+" signMsg = " + signMsg+" resv1 = " + resv1+" resv2 = " + resv2+
				" resv3 = " + resv3+" resv4 = " + resv4);

		String respCode = "";
		String respCodeRemark = "";
		String serialNo = "";
		String idTp = "";
		String idNo = "";
		String bankAcco = "";
		String bankAccoNm = "";
		String protocolNo = "";

		//RESP_1.0开始写应答
        ResponsionDto respDto = null;
        String tranSt = null;
        String bankNo = null;

		SignMsgService signService = new SignMsgService();
		//验签和解密数据
		Pkg2BidResult result = signService.decodeSignMsg(encMsg, signMsg);
		BankCommand bankCommand = null;
		if (result != null) {
			respCode = result.getRespCode();
			respCodeRemark = result.getRespCodeRemark();
			serialNo = result.getFundSeqId();
			idTp = result.getCardType();
			idNo = result.getCardNo();
			bankAcco = result.getCustCardId();
			bankAccoNm = result.getCustCardName();
			protocolNo = result.getProtocolNum();

			bankCommand = bankCommandService.getBankCommandBySerialNo(serialNo);
			bankNo = bankCommand.getBnkNo();
		}

		logger.info("respCode = " + respCode+" respCodeRemark = " + respCodeRemark+" serialNo = " + serialNo+" idTp = " + idTp+" idNo = " + idNo+
				" bankAcco = " + bankAcco+" bankAccoNm = " + bankAccoNm + " protocolNo = " + protocolNo);
		//开户失败
		if (!"000".equals(respCode)) {
			sendMetaQAndUpdateBankCommand(serialNo, protocolNo, TransactionStatus.F);
			tranSt = EBankParamConstant.PM_CO_EBANK$TRAN_ST$F;
		}else{
			sendMetaQAndUpdateBankCommand(serialNo, protocolNo, TransactionStatus.Y);
			tranSt = EBankParamConstant.PM_CO_EBANK$TRAN_ST$Y;
		}

		//RESP_3.0组装应答信息   modify by liuwu  20150518
        respDto = new ResponsionDto();
        ReturnDto returnDto = new ReturnDto();

        respDto.setSerialNo(serialNo);
        respDto.setBnkNo(bankNo);
        respDto.setRespCo(respCode);
        respDto.setRespMsg(respCodeRemark);
        respDto.setTranSt(tranSt);
        returnDto.setResp(respDto);

        // 写response失败不影响绑卡
        try {
            ReturnProxy proxy = new ReturnProxy();
            proxy.processReturn(returnDto);
        } catch (Exception e) {
            e.printStackTrace();
        }

		response.getWriter().print("chinapayok");
	}

	private void sendMetaQAndUpdateBankCommandByRespStatus(String respStatus, String serialNo, String protocolNo){
		if(BankEngineConstants.CMD_RESP_VERIFIED.equals(respStatus)){
			sendMetaQAndUpdateBankCommand(serialNo, protocolNo, TransactionStatus.Y);

		}else if(BankEngineConstants.CMD_RESP_NOT_VERIFIED.equals(respStatus)){
			sendMetaQAndUpdateBankCommand(serialNo, protocolNo, TransactionStatus.F);
		}
	}

	/**
	 * 发送MetaQ消息，并更新银行指令状态（如果有需要的话）</br>
	 * PS：因为预先的通道代码并不是每个通道都会将成功开户的银行指令tranStatus = Y的。
	 *
	 * @param serialNo
	 * @param protocolNo
	 * @param tranStatus
	 */
	private void sendMetaQAndUpdateBankCommand(String serialNo, String protocolNo, TransactionStatus tranStatus){
		sendMqMessage(serialNo, protocolNo, tranStatus);

		bankCommandMapper.updateBankCommandtranStForOpen(tranStatus.toString(),BankEngineConstants.BANKCOMMAND_LOCKST_N,serialNo);

		//保存BANKCARDINFO_FORSIGN表
//		saveBankCardInfoSign(serialNo,protocolNo);

		logger.info("验证结果为：" + tranStatus.toString());
	}

	private void saveBankCardInfoSign(String serialNo,String protocolNo){		 //同一线程内此三步骤应该保持一致性 这样简单处理下
		bankCardInfoSignService.saveBankCardInfoSign(serialNo, protocolNo);
	}

	/**
	 * 发送MQ消息
	 * @param serialNo
	 */
	private void sendMqMessage(String serialNo,String protocolNo,TransactionStatus tranStatus){
		logger.info("准备绑卡发送MetaQ信息，serialNo:" + serialNo);
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

			qMessage.setAccountNo(bankCommand.getSndrAcctNo());
			qMessage.setAccountName(bankCommand.getSndrAcctName());
			qMessage.setIdNo(bankCommand.getSndrIdNo());
			qMessage.setProtocolNo(protocolNo);
			qMessage.setAppKind(bankCommand.getRefAppKind());
			qMessage.setTransactionStatus(tranStatus);


			qMessage.setAccptmd(BEConverter.appSourceToAccptmd(bankCommand.getAppSource()));

			logger.info("开户发送MetaQ信息，" + qMessage);
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
			return true;
		}else{
			return BankEngineConstants.BANKCOMMAND_FINAL_STATUS.indexOf(bankCommand.getTranSt()) > -1;
		}
	}




}
