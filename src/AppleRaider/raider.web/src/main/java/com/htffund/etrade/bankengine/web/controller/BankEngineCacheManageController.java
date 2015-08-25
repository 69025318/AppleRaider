package com.htffund.etrade.bankengine.web.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fund.etrading.ebankapp.base.api.LoggerFactory;
import com.htffund.etrade.bankengine.common.cache.CacheDelegate;
import com.htffund.etrade.bankengine.dao.service.BankCardInfoSignService;

@Controller
public class BankEngineCacheManageController {
	private final Logger logger = LoggerFactory.getEBANKLogger();

	@Resource(name="mapCacheDelegate")
	private CacheDelegate cacheDelegate;

	@Resource
	private BankCardInfoSignService bankCardInfoSignService;

	@RequestMapping(value = "cache/clearCache")
	public String clearCache(){

		try {
			cacheDelegate.clearCache();
			logger.info("清空银行引擎缓存成功");
		} catch (Exception e) {
			logger.error("清空银行引擎缓存成功", e);
			return "cache/failure";
		}

		return "cache/success";
	}

	@RequestMapping(value = "config/manager")
	public String manager(){
		return "config/manager";
	}

	@RequestMapping(value = "config/changeConfig")
	public String changeConfig(HttpServletRequest request,String isOpen){
		try {
			bankCardInfoSignService.changeConfig(isOpen);
			logger.info("是否进行绑卡信息比较,0--关闭,1--打开"+isOpen);
		} catch (Exception e) {
			logger.error("设置失败", e);
			return "config/failure";
		}

		return "config/success";
	}



}
