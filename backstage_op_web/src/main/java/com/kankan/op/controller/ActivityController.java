package com.kankan.op.controller;

import java.util.ArrayList;
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
import com.kankan.op.beans.ActivityIdBean;
import com.kankan.op.beans.PagedDataResp;
import com.kankan.op.bo.SystemParasBo;
import com.kankan.op.utils.Constants;
import com.kankan.op.utils.HttpRequestHelper;
import com.kankan.op.utils.HttpRequestUtils;
import com.kankan.op.utils.JSONUtil;
import com.kankan.op.utils.JsonObjectUtil;

@Controller
public class ActivityController {

	private static Logger log = Logger.getLogger(ActivityController.class);

	@Autowired
	private SystemParasBo systemParasBo;
	
	@Value("${backstage.server.address}")
	private String backstageUrl;

	@RequestMapping(value = "/getAllActivityList.do", method = RequestMethod.POST)
	@ResponseBody
	public String getAllActivityList(HttpServletRequest request) {
		String url = "http://"
				+ backstageUrl
				+ "/activityIdManage/getAllActivityList?pageNumber=%s&pageSize=%s&"
				+ Constants.activity + "=%s";

		String pageNumber = request.getParameter(Constants.pageNumber);
		String pageSize = request.getParameter(Constants.pageSize);
		Preconditions.checkState(!StringUtils.isEmpty(pageNumber),
				"pageNumber 为空");
		Preconditions.checkState(!StringUtils.isEmpty(pageSize), "pageSize 为空");
		String activity = request.getParameter(Constants.activity);
		url = String.format(url, pageNumber, pageSize, activity);
		String result = HttpRequestUtils.doGet(url);
		TypeReference<PagedDataResp<ActivityIdBean>> typeReference = new TypeReference<PagedDataResp<ActivityIdBean>>() {
		};
		PagedDataResp<ActivityIdBean> dataResp = JSONObject.parseObject(result,
				typeReference);
		if (dataResp != null) {
			//获取所属团队
			Map<String, String> teamMap = systemParasBo.getTeamList();
			if (teamMap != null && !teamMap.isEmpty()) {
				for (ActivityIdBean activityIdBean : dataResp.rows) {
					activityIdBean.teamname = teamMap.get(activityIdBean
							.getTeamid());
				}
			}
		} else {
			// 这样才能提示“没有找到匹配的记录”，否则一直显示正在加载中
			dataResp = new PagedDataResp<ActivityIdBean>();
		}
		return JSONUtil.fromObject(dataResp);
	}

	@RequestMapping(value = "/addActivity.do", method = RequestMethod.POST)
	@ResponseBody
	public String addActivity(HttpServletRequest request, HttpSession session) {
		String url = "http://" + backstageUrl
				+ "/activityIdManage/addOrUpdateActivity";
		String activity = request.getParameter(Constants.activity);

		Preconditions.checkState(!StringUtils.isEmpty(activity),
				"activity(%s) is null", activity);

		String result = null;
		HttpPost httpRequst = new HttpPost(url);
		log.info(url);

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(Constants.activity, activity));
		String loginname = (String) session.getAttribute(Constants.loginName);
		Preconditions.checkState(!StringUtils.isEmpty(loginname),
				"loginname 为空");

		params.add(new BasicNameValuePair(Constants.loginName, loginname));
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
		}

		return JSONUtil.fromObject(JsonObjectUtil.buildMap("result", result));
	}

	@RequestMapping(value = "/updateActivity.do", method = RequestMethod.POST)
	@ResponseBody
	public String updateActivity(HttpServletRequest request, HttpSession session) {
		return addActivity(request, session);
	}

	@RequestMapping(value = "/deleteActivityIds.do", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String deleteActivityIds(HttpServletRequest request) {
		String ids = request.getParameter(Constants.seqids);
		String deleteActivityIdsUrl = "http://" + backstageUrl
				+ "/activityIdManage/delActivityIds?ids=%s";
		String _url = String.format(deleteActivityIdsUrl, ids);
		String result = HttpRequestUtils.doGet(_url);
		return result;
	}
	
}