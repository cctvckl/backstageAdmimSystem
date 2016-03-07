package com.kankan.op.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kankan.op.beans.SystemParaBean;
import com.kankan.op.bo.SystemParasBo;
import com.kankan.op.utils.JSONUtil;
import com.kankan.op.utils.JsonObjectUtil;

/**
 * 此类主要获取权限系统定义的参数信息，获取不同的参数定义不同的方法，方法和请求名称统一由sys开头，主要是为了拦截器不会因为没有分配操作权限而被拦截
 * 
 * @author lianshuping
 * @date 2016年2月24日
 */
@Controller
public class SystemParaController {

	@Autowired
	private SystemParasBo systemParasBo;

	/**
	 * 获取所属团队
	 * 
	 * @return
	 */
	@RequestMapping(value = "/sysTeamList.do", method = RequestMethod.GET)
	@ResponseBody
	public String sysTeamList() {
		return generateJsonData(systemParasBo.getTeamList());
	}
	
	/**
	 * 获取奖品分类
	 * 
	 * @return
	 */
	@RequestMapping(value = "/sysPrizeTypeList.do", method = RequestMethod.GET)
	@ResponseBody
	public String sysPrizeTypeList() {
		return generateJsonData(systemParasBo.getPrizeType());
	}
	
	/**
	 * 获取具体奖品
	 * 
	 * @return
	 */
	@RequestMapping(value = "/sysPrizeEntityList.do", method = RequestMethod.GET)
	@ResponseBody
	public String sysPrizeEntityList(HttpServletRequest request) {
		String paraCode = request.getParameter("id");
		return generateJsonData(systemParasBo.getPrizeEntity(paraCode));
	}

	private String generateJsonData(Map<String, String> parasMap) {
		List<SystemParaBean> parasList = new ArrayList<SystemParaBean>();
		if (parasMap != null) {
			for (Map.Entry<String, String> entry : parasMap.entrySet()) {
				parasList.add(new SystemParaBean(entry.getKey(), entry
						.getValue()));
			}
		}
		return JSONUtil.fromObject(parasList);

	}

}
