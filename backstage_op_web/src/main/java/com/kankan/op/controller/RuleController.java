package com.kankan.op.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.expression.common.TemplateAwareExpressionParser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.base.Preconditions;
import com.kankan.op.beans.ActivityIdBean;
import com.kankan.op.beans.PagedDataResp;
import com.kankan.op.beans.RuleExtVo;
import com.kankan.op.beans.RuleVo;
import com.kankan.op.beans.UpCutVo;
import com.kankan.op.utils.Constants;
import com.kankan.op.utils.HttpRequestHelper;
import com.kankan.op.utils.HttpRequestUtils;
import com.kankan.op.utils.JSONUtil;
import com.kankan.op.utils.JsonObjectUtil;

@Controller
public class RuleController {

	private static Logger log = Logger.getLogger(RuleController.class);
	@Value("${backstage.server.address}")
	private String backstageUrl;

	@RequestMapping(value = "/getAllRuleList.do", method = RequestMethod.POST)
	@ResponseBody
	public String getAllRuleList(HttpServletRequest request) {
		String baseUrl = "http://" + backstageUrl + "/rulesMgt/getRuleList?curPage=%s&pageSize=%s";
		String url = "";

		String pageNumber = request.getParameter(Constants.pageNumber);
		String pageSize = request.getParameter(Constants.pageSize);
		Preconditions.checkState(!StringUtils.isEmpty(pageNumber), "pageNumber 为空");
		Preconditions.checkState(!StringUtils.isEmpty(pageSize), "pageSize 为空");
		String rule = request.getParameter(Constants.rule);
		if (rule == null) {
			// 未携带rule参数时，取出来的rule为null
			url = String.format(baseUrl, pageNumber, pageSize);
		} else {
			url = String.format(baseUrl, pageNumber, pageSize) + "&ruleName=" + rule;
		}
		String result = HttpRequestUtils.doGet(url);

		return result;
	}

	@RequestMapping(value = "/getRuleTemplatesAndRuleStatus.do", method = RequestMethod.GET)
	@ResponseBody
	public String getRuleTemplatesAndRuleStatus() {
		String url = "http://" + backstageUrl + "/rulesMgt/getRuleTemplatesAndRuleStatus";
		String result = HttpRequestUtils.doGet(url);
		log.info(result);
		return result;
	}

	@RequestMapping(value = "/getOneRuleByRuleId.do", method = RequestMethod.GET)
	@ResponseBody
	public String getOneRuleByRuleId(HttpServletRequest request) {
		String url = "http://" + backstageUrl + "/rulesMgt/getOneRule?ruleId=%s";
		String ruleId = request.getParameter(Constants.ruleId);
		url = String.format(url, ruleId);

		String result = HttpRequestUtils.doGet(url);
		log.info(result);
		return result;
	}

	@RequestMapping(value = "/deleteRulesByIds.do", method = RequestMethod.POST)
	@ResponseBody
	public String deleteActivityIds(HttpServletRequest request) {
		String ids = request.getParameter(Constants.seqids);
		String deleteActivityIdsUrl = "http://" + backstageUrl + "/rulesMgt/deleteRules?ids=%s";
		String _url = String.format(deleteActivityIdsUrl, ids);
		String result = HttpRequestUtils.doGet(_url);
		return result;
	}
	@RequestMapping(value = "/getPrizeKeyById.do", method = RequestMethod.GET)
	@ResponseBody
	public String getPrizeKeyById(HttpServletRequest request) {
		String prizeId = request.getParameter(Constants.prizeId);
		String getPrizeKeyByIdUrl = "http://" + backstageUrl + "/prizeMgt/getPrizeKeyById?prizeId=%s";
		log.info(prizeId);
		String _url = String.format(getPrizeKeyByIdUrl, prizeId);
		String result = HttpRequestUtils.doGet(_url);
		log.info(result);
		return result;
	}

	@RequestMapping(value = "/addRule.do", method = RequestMethod.POST)
	@ResponseBody
	public String addRule(HttpServletRequest request, HttpSession session) {
		String urlAddRule = "http://" + backstageUrl + "/rulesMgt/addRule";

		String rule = request.getParameter(Constants.rule);
		log.info(rule);
		Preconditions.checkState(!StringUtils.isEmpty(rule), "rule(%s) is null", rule);

		TypeReference<RuleVo> typeReference = new TypeReference<RuleVo>() {
		};
		RuleVo ruleVo = JSONObject.parseObject(rule, typeReference);
		String preconditonResult = checkParameter(ruleVo);
		if (!Constants.OK.equals(preconditonResult)) {
			return JSONUtil.fromObject(JsonObjectUtil.buildMap("rtn", Constants.FAIL, "result", preconditonResult));
		}
		String result = null;
		HttpPost httpRequst = new HttpPost(urlAddRule);
		log.info(urlAddRule);

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(Constants.rule, rule));
		String loginname = (String) session.getAttribute(Constants.loginName);
		Preconditions.checkState(!StringUtils.isEmpty(loginname), "loginname 为空");

		params.add(new BasicNameValuePair(Constants.loginName, loginname));
		try {
			httpRequst.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));
			HttpResponse httpResponse = HttpRequestHelper.getHttpClient().execute(httpRequst);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity httpEntity = httpResponse.getEntity();
				result = Constants.OK;
			}
		} catch (Exception e) {
			e.printStackTrace();
			result = e.getMessage().toString();
		}

		return JSONUtil.fromObject(JsonObjectUtil.buildMap("rtn", Constants.SUCCESS, "result", result));
	}

	private String checkParameter(RuleVo ruleVo) {
		if (StringUtils.isEmpty(ruleVo.getRuleName())) {
			return "规则名不能为空";
		}
		String temp = String.valueOf(ruleVo.getTemplateId());
		if (StringUtils.isEmpty(temp) || "null".equals(temp)) {
			return "规则模板不能为空";
		}

		if (StringUtils.isEmpty(ruleVo.getActId())) {
			return "活动id不能为空";
		}

		temp = String.valueOf(ruleVo.getRuleStatusId());
		if (StringUtils.isEmpty(temp) || "null".equals(temp)) {
			return "规则状态不能为空";
		}
		if (StringUtils.isEmpty(ruleVo.getStartTime())) {
			return "开始时间不能为空";
		}
		if (StringUtils.isEmpty(ruleVo.getEndTime())) {
			return "结束时间不能为空";
		}
		//
		for (RuleExtVo ruleExtVo : ruleVo.getRuleExtVos()) {
			temp = String.valueOf(ruleExtVo.getPrizeDetailTypeId());
			if (StringUtils.isEmpty(temp) || "null".equals(temp)) {
				return "奖品具体类型不能为空";
			}
			temp = String.valueOf(ruleExtVo.getActTypeId());
			if (StringUtils.isEmpty(temp) || "null".equals(temp)) {
				return "活动类型不能为空";
			}
			switch (ruleVo.getTemplateId().intValue()) {
			case RuleVo.templateId_UpCut:
				for (UpCutVo upCutVo : ruleExtVo.getUpCutVos()) {
					if (Math.abs(upCutVo.getFullPrice() - 0) < 10E-6 ||
							Math.abs(upCutVo.getMinusPrice() - 0) < 10E-6){
						return "满减价格不能为空";
					}
					if (StringUtils.isEmpty(String.valueOf(upCutVo.getFullPrice()))) {
						return "满减价格不能为空";
					}
					if (StringUtils.isEmpty(String.valueOf(upCutVo.getMinusPrice()))) {
						return "满减价格不能为空";
					}
				}
				break;
			case RuleVo.templateId_Register:
				
				break;
				
			default:
				break;
			}
			

		}
		temp = String.valueOf(ruleVo.getVipGrade());
		if (StringUtils.isEmpty(temp) || "null".equals(temp)) {
			return "会员级别不能为空";
		}
		if (StringUtils.isEmpty(String.valueOf(ruleVo.getTimeLimit()))) {
			return "次数限制不能为空";
		}
		return Constants.OK;
	}

	@RequestMapping(value = "/updateRule.do", method = RequestMethod.POST)
	@ResponseBody
	public String updateRule(HttpServletRequest request, HttpSession session) {
		String urlAddRule = "http://" + backstageUrl + "/rulesMgt/updateRule";

		String rule = request.getParameter(Constants.rule);
		log.info(rule);
		Preconditions.checkState(!StringUtils.isEmpty(rule), "rule(%s) is null", rule);

		TypeReference<RuleVo> typeReference = new TypeReference<RuleVo>() {
		};
		RuleVo ruleVo = JSONObject.parseObject(rule, typeReference);
		String preconditonResult = checkParameter(ruleVo);
		if (!Constants.OK.equals(preconditonResult)) {
			return JSONUtil.fromObject(JsonObjectUtil.buildMap("rtn", Constants.FAIL, "result", preconditonResult));
		}
		
		String result = null;
		HttpPost httpRequst = new HttpPost(urlAddRule);
		log.info(urlAddRule);

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(Constants.rule, rule));
		String loginname = (String) session.getAttribute(Constants.loginName);
		Preconditions.checkState(!StringUtils.isEmpty(loginname), "loginname 为空");

		params.add(new BasicNameValuePair(Constants.loginName, loginname));
		try {
			httpRequst.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));
			HttpResponse httpResponse = HttpRequestHelper.getHttpClient().execute(httpRequst);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity httpEntity = httpResponse.getEntity();
				result = Constants.OK;
			}
		} catch (Exception e) {
			e.printStackTrace();
			result = e.getMessage().toString();
		}

		return JSONUtil.fromObject(JsonObjectUtil.buildMap("result", result));
	}
	// 通过mock测试，不需要通过界面；详细参考com.kankan.op.test.TestRuleController

	@RequestMapping(value = "/addRuleMock.do", method = RequestMethod.POST)
	@ResponseBody
	public String mockAddRule(HttpServletRequest request, HttpSession session) {
		String urlAddRule = "http://" + "192.168.28.229:9090" + "/rulesMgt/addRule";

		String rule = request.getParameter(Constants.rule);
		log.info(rule);
		rule = "{\"ruleName\":\"逐日12\",\"templateId\":\"1\",\"actId\":\"123\",\"ruleStatusId\":\"383\",\"startTime\":\"2016-02-24 15:21:25\",\"endTime\":\"2016-02-24 15:21:26\",\"ruleExtVos\":[{\"prizeDetailTypeId\":\"388\",\"actTypeId\":\"379\",\"upCutVos\":[{\"fullPrice\":\"33\",\"minusPrice\":\"11\"}]},{\"prizeDetailTypeId\":\"388\",\"actTypeId\":\"379\",\"upCutVos\":[{\"fullPrice\":\"332\",\"minusPrice\":\"111\"}]}]}";
		Preconditions.checkState(!StringUtils.isEmpty(rule), "rule(%s) is null", rule);

		String result = null;
		HttpPost httpRequst = new HttpPost(urlAddRule);
		log.info(urlAddRule);

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(Constants.rule, rule));
		String loginname = "lili";
		Preconditions.checkState(!StringUtils.isEmpty(loginname), "loginname 为空");

		params.add(new BasicNameValuePair(Constants.loginName, loginname));
		try {
			httpRequst.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));
			HttpResponse httpResponse = HttpRequestHelper.getHttpClient().execute(httpRequst);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity httpEntity = httpResponse.getEntity();
				result = Constants.OK;
			}
		} catch (Exception e) {
			e.printStackTrace();
			result = e.getMessage().toString();
		}

		return JSONUtil.fromObject(JsonObjectUtil.buildMap("result", result));
	}
}