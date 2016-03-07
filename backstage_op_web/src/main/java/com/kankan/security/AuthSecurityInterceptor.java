package com.kankan.security;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.kankan.op.utils.Constants;

public class AuthSecurityInterceptor  implements HandlerInterceptor{

	private static Logger log = Logger.getLogger(AuthSecurityInterceptor.class);
	@Override
	public void afterCompletion(HttpServletRequest arg0,
			HttpServletResponse arg1, Object arg2, Exception arg3)
			throws Exception {
		
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response,
			Object arg2, ModelAndView arg3) throws Exception {
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", 0);
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
			Object handler) throws Exception {
		Set<String> userAuthOps = (HashSet<String>) request.getSession().getAttribute(Constants.user_auth_ops);
		String springUri = null;
		log.info(request.getRequestURI());
		//log.info(request.getRequestURL());
		

		if(handler.getClass().isAssignableFrom(HandlerMethod.class)){
			//获取*.do
			String reqUri = request.getRequestURI();
			reqUri = reqUri.substring(reqUri.lastIndexOf('/')+1);
			String opAuthName = Constants.system_operation+reqUri;
			//判断是否有此权限
			if(userAuthOps!=null&&userAuthOps.contains(opAuthName)){
				return true;
			}
			log.info("无权限"+opAuthName);
			response.sendRedirect("showNoAuth.do");
			return false;
		}
		return true;
//		String uri = request.getRequestURI();
//		String leaves[] = uri.split("/");
//		String springUri = leaves[leaves.length-1].replace(Constants.request_surfix, 
	}

}
