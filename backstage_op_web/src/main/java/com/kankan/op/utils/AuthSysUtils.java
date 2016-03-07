package com.kankan.op.utils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.kankan.op.beans.LoginResp;
import com.kankan.op.beans.RespOfAuthSystem;

/**
 * 权限系统工具类
 * @author LENOVO
 *
 */
public class AuthSysUtils {
	
	/**
	 * 根据请求返回json，并进行封装,仅用于yzg权限系统
	 * lili  2016年2月20日
	 * @throws Exception 
	 */
	public static <T> RespOfAuthSystem<T> convertResultJson(String requestUrl) throws Exception{
		TypeReference<RespOfAuthSystem<T>> typeReference = new TypeReference<RespOfAuthSystem<T>>() {};
		RespOfAuthSystem<T> authSystem = JSONObject.parseObject(HttpRequestUtils.doGet(requestUrl), typeReference);
		if (Constants.OK.equals(authSystem.code)){
			return authSystem;
		}
		throw new Exception(authSystem.msg);
	}
}
