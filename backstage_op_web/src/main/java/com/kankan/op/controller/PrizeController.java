package com.kankan.op.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.base.Preconditions;
import com.kankan.op.beans.PagedDataResp;
import com.kankan.op.beans.PrizeBean;
import com.kankan.op.bo.SystemParasBo;
import com.kankan.op.utils.Constants;
import com.kankan.op.utils.HttpRequestHelper;
import com.kankan.op.utils.HttpRequestUtils;
import com.kankan.op.utils.JSONUtil;
import com.kankan.op.utils.JsonObjectUtil;

@Controller
public class PrizeController {
	private static Logger log = Logger.getLogger(PrizeController.class);

	@Autowired
	private SystemParasBo systemParasBo;

	@Value("${backstage.server.address}")
	private String backstageUrl;

	private String getRequestUrlPrefix(String operationName) {
		String urlPrefix = "http://" + backstageUrl + "/prizeMgt/"
				+ operationName;
		return urlPrefix;
	}

	@RequestMapping(value = "/getAllPrizeList.do", method = RequestMethod.POST)
	@ResponseBody
	public String getAllPrizeList(HttpServletRequest request) {
		String url = getRequestUrlPrefix("getAllPrizeInfo")
				+ "?pageNumber=%s&pageSize=%s&" + Constants.prizeinfo + "=%s";

		String pageNumber = request.getParameter(Constants.pageNumber);
		String pageSize = request.getParameter(Constants.pageSize);
		Preconditions.checkState(!StringUtils.isEmpty(pageNumber),
				"pageNumber 为空");
		Preconditions.checkState(!StringUtils.isEmpty(pageSize), "pageSize 为空");
		String prizeinfo = request.getParameter(Constants.prizeinfo);
		url = String.format(url, pageNumber, pageSize, prizeinfo);
		String result = HttpRequestUtils.doGet(url);
		TypeReference<PagedDataResp<PrizeBean>> typeReference = new TypeReference<PagedDataResp<PrizeBean>>() {
		};
		PagedDataResp<PrizeBean> dataResp = JSONObject.parseObject(result,
				typeReference);
		if (dataResp != null) {
			// 获取奖品类型
			Map<String, String> prizeTypeMap = systemParasBo.getPrizeType();
			Map<String, String> prizeEntityMap = new HashMap<String, String>();
			for (Map.Entry<String, String> entry : prizeTypeMap.entrySet()) {
				Map<String, String> entityMap = systemParasBo
						.getPrizeEntity(entry.getKey());
				if (entityMap != null && !entityMap.isEmpty()) {
					prizeEntityMap.putAll(entityMap);
				}
			}
			for (PrizeBean prizeBean : dataResp.rows) {
				prizeBean.setPrizeTypeName(prizeTypeMap.get(prizeBean
						.getPrizeType()));
				prizeBean.setPrizeEntityName(prizeEntityMap.get(prizeBean
						.getPrizeEntity()));
			}
		} else {
			// 这样才能提示“没有找到匹配的记录”，否则一直显示正在加载中
			dataResp = new PagedDataResp<PrizeBean>();
		}
		return JSONUtil.fromObject(dataResp);
	}

	@RequestMapping(value = "/addPrize.do", method = RequestMethod.POST)
	@ResponseBody
	public String addPrizeInfo(PrizeBean prizeBean, HttpSession session) {
		return addOrUpdate(prizeBean, session, "addPrizeInfo");
	}

	@RequestMapping(value = "/updatePrize.do", method = RequestMethod.POST)
	@ResponseBody
	public String updatePrizeInfo(PrizeBean prizeBean,
			HttpSession session) {
		return addOrUpdate(prizeBean, session, "updatePrizeInfo");
	}

	@RequestMapping(value = "/deletePrize.do", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String deletePrizeInfo(HttpServletRequest request) {
		String ids = request.getParameter(Constants.seqids);
		String deleteUrl = getRequestUrlPrefix("delPrizeIds") + "?ids=%s";
		String _url = String.format(deleteUrl, ids);
		String result = HttpRequestUtils.doGet(_url);
		return result;
	}

	private String addOrUpdate(PrizeBean prizeBean, HttpSession session,
			String operationName) {
		if (prizeBean==null) {
			return "";
		}
		String loginname = (String) session.getAttribute(Constants.loginName);
		Preconditions.checkState(!StringUtils.isEmpty(loginname),
				"loginname 为空");
		
		prizeBean.setCreateBy(loginname);
		prizeBean.setEditBy(loginname);
		
		String url = getRequestUrlPrefix(operationName);
		String result = null;
		HttpPost httpRequst = new HttpPost(url);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		
		String prizeinfo = JSONUtil.fromObject(prizeBean);
		params.add(new BasicNameValuePair(Constants.prizeinfo, prizeinfo));
		try {
			httpRequst
					.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));
			HttpResponse httpResponse = HttpRequestHelper.getHttpClient()
					.execute(httpRequst);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity httpEntity = httpResponse.getEntity();
				result = Constants.OK;
			}
		} catch (Exception e) {
			e.printStackTrace();
			result = e.getMessage().toString();
			log.error(e.getMessage());
		}

		return JSONUtil.fromObject(JsonObjectUtil.buildMap("result", result));
	}

}
