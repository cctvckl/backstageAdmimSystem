/**
 * 
 */
package com.kankan.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author liqing
 *
 */
public class LoginSecurityInterceptor implements HandlerInterceptor {

	private String LOGIN_URL = "/login.html";
	
	private static Logger log = Logger.getLogger(LoginSecurityInterceptor.class);

	@Override
	public void afterCompletion(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, Exception arg3)
			throws Exception {

	}

	@Override
	public void postHandle(HttpServletRequest arg0, HttpServletResponse response, Object arg2, ModelAndView arg3)
			throws Exception {
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", 0);
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		HttpSession session = request.getSession();
		Object obj = session.getAttribute("loginId");
		if (obj == null || "".equals(obj.toString())) {
			// request.getRequestDispatcher(LOGIN_URL).forward(request,
			// response);
			response.sendRedirect(request.getContextPath() + LOGIN_URL);
			return false;
		}

		return true;
	}

}
