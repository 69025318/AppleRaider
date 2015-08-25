package com.htffund.etrade.bankengine.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 用于通过页面导入基础配置信息
 *
 * @author wenchun
 *
 */
@Controller
@RequestMapping(value = "baseConfig/")
public class BaseConfigOperationController {

	@RequestMapping(value = "bankCardBin")
	public String importBankCardBin(HttpServletRequest request,HttpServletResponse response){

		return null;
	}
}
