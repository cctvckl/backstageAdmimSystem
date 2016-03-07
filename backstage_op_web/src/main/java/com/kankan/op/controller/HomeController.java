package com.kankan.op.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.kankan.op.beans.LoginResp;
import com.kankan.op.beans.RespOfAuthSystem;
import com.kankan.op.beans.User;
import com.kankan.op.constants.RtnConstants;
import com.kankan.op.utils.ApplicationConfigUtil;
import com.kankan.op.utils.AuthSysUtils;
import com.kankan.op.utils.Constants;
import com.kankan.op.utils.HttpRequestUtils;
import com.kankan.op.utils.JsonObjectUtil;
import com.kankan.op.utils.ServiceUrlUtil;

@Controller
public class HomeController {

	private static Logger log = Logger.getLogger(HomeController.class);

	@RequestMapping(value = "/validation.do", method = RequestMethod.POST)
	@ResponseBody
	public String validation(@ModelAttribute User user,
			HttpServletRequest request) {

		Map<String, String> parasMap = new HashMap<String, String>();
		parasMap.put(Constants.api, ApplicationConfigUtil
				.getValue(Constants.yzg_auth_account_login));
		parasMap.put(Constants.loginName, user.getLoginName());
		parasMap.put(Constants.loginPwd, user.getLoginPwd());
		String _url = ServiceUrlUtil.getRequestUrl(Constants.authService_host,
				Constants.authService_port, Constants.authService_name,
				parasMap);
		log.info(user.getLoginName());
		String result = HttpRequestUtils.doGet(_url);
		JSONObject jsonObject = JSONObject.parseObject(result);
		if (jsonObject.containsKey(Constants.data)) {
			JSONObject data = jsonObject.getJSONObject(Constants.data);
			if (data == null) {
				return "";
			}
			if (data.containsKey(Constants.loginId)) {
				String loginId = (data)
						.getString(Constants.loginId);
//				request.getSession().setAttribute(Constants.loginId, loginId);
				return loginId;
			}
		}
		return "";
	}

	@RequestMapping(value = "/login.do", method = RequestMethod.POST)
	public String login(@ModelAttribute User user,
			final RedirectAttributes redirectAttributes,
			HttpServletRequest request) {

		Map<String, String> parasMap = new HashMap<String, String>();
		parasMap.put(Constants.api, ApplicationConfigUtil
				.getValue(Constants.yzg_auth_account_login));
		parasMap.put(Constants.loginName, user.getLoginName());
		parasMap.put(Constants.loginPwd, user.getLoginPwd());
		String _url = ServiceUrlUtil.getRequestUrl(Constants.authService_host,
				Constants.authService_port, Constants.authService_name,
				parasMap);
		log.info(user.getLoginName());
		String result = HttpRequestUtils.doGet(_url);
		TypeReference<RespOfAuthSystem<LoginResp>> typeReference = new TypeReference<RespOfAuthSystem<LoginResp>>() {
		};
		RespOfAuthSystem<LoginResp> authSystem = JSONObject.parseObject(result,
				typeReference);
//		RespOfAuthSystem<LoginResp> authSystem = null;
//			authSystem = AuthSysUtils.convertResultJson(_url);
		if (Constants.OK.equals(authSystem.code)) {
			try {
			LoginResp data = authSystem.data;
			request.getSession().setAttribute(Constants.loginId, data.loginId);
			request.getSession().setAttribute(Constants.loginName,
					user.getLoginName());

			parasMap = new HashMap<String, String>();
			parasMap.put(Constants.api, Constants.yzg_auth_menu_get);
			parasMap.put(Constants.appId,
					ApplicationConfigUtil.getValue(Constants.appId));
			parasMap.put(Constants.loginId,data.loginId);
//			parasMap.put(Constants.onlyDir, "0");
			String getMenuUrl = ServiceUrlUtil.getRequestUrl(
					Constants.authService_host, Constants.authService_port,
					Constants.authService_name, parasMap);

			String menus = HttpRequestUtils.doGet(getMenuUrl);
			request.getSession().setAttribute(Constants.menuPerData, menus);// 为了把数据放到session
			// 获取用户权限数据，并放入session
			Map<String, String> paramMap2 = new HashMap<String, String>();
			paramMap2.put(Constants.api, Constants.yzg_hr_account_getAllRole);
			paramMap2.put(Constants.appId,
					ApplicationConfigUtil.getValue(Constants.appId));
			paramMap2.put(Constants.staffId, data.staffId);
			// 获取该用户所有角色信息
			String getRolesUrl = ServiceUrlUtil.getRequestUrl(
					Constants.hrService_host, Constants.hrService_port,
					Constants.hrService_name, paramMap2);
				RespOfAuthSystem<String> resultRole = AuthSysUtils.convertResultJson(getRolesUrl);
				// 整个每个角色下的权限信息
				String roles[] = resultRole.data
						.split(Constants.yzg_roles_split_char);
				List<String> allOps = new ArrayList<String>();
				for (String role : roles) {
					Map<String, String> paramMap3 = new HashMap<String, String>();
					paramMap3.put(Constants.api,
							Constants.yzg_auth_grant_role_getPerm);
					paramMap3.put(Constants.appId,
							ApplicationConfigUtil.getValue(Constants.appId));
					paramMap3.put(Constants.roleId, role+Constants.forCode);
					paramMap3.put(Constants.system_perm_type,
							Constants.system_perm_type_operation);
					String getOpsUrl = ServiceUrlUtil.getRequestUrl(
							Constants.authService_host,
							Constants.authService_port,
							Constants.authService_name, paramMap3);
					RespOfAuthSystem<JSONArray> resultOps = AuthSysUtils
							.convertResultJson(getOpsUrl);
//					String ops[] = resultOps.data.split(Constants.yzg_roles_split_char);
					for(Object object:resultOps.data){
						allOps.add((String)object);
					}
				}
				request.getSession().setAttribute(Constants.user_auth_ops, new HashSet<String>(allOps));
			} catch (Exception e) {
				log.error(e);
			}
		}

		return "redirect:index.html";

	}

	@RequestMapping(value = "/init.do", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String initMenu(HttpSession session) {
		return (String) session.getAttribute(Constants.menuPerData);
	}

	@RequestMapping(value = "/loginuser.do", method = RequestMethod.GET)
	@ResponseBody
	public String initUserName(HttpSession session) {
		return (String) session.getAttribute(Constants.loginName);
	}
	
	@RequestMapping(value = "/showNoAuth.do", method = RequestMethod.GET)
	@ResponseBody
	public String showNoAuth(){
		return JsonObjectUtil.getRtnAndDataJsonObject(RtnConstants.NO_OP_AUTH, "无此操作权限", "");
	}

	@RequestMapping(value = "/signOut.do", method = RequestMethod.GET)
	public String signOut(HttpSession session, ModelMap modelmap,
			final RedirectAttributes redirectAttributes) {
		session.invalidate();
		return "redirect:login.html";

	}

}
