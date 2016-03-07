package com.kankan.op.utils;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 将对象转换成JSON格式的工具类，如果JACKSON Engineer加载成功就用其直接转换，否则使用JSONEncoder转换
 * 
 * @author ZengDong
 * @since 2010-6-1 下午11:46:55
 */
public class JSONUtil {

	/**
	 * 将Object类型的对象转换成JSON
	 */
	public static String fromObject(Object obj) {
		return JSON.toJSONString(obj);
	}

	/**
	 * 将JSON字符串转换为对应的JavaBean，如果转换出现问题返回null
	 */
	public static <T> T toObject(String json, Class<T> clazz) {
		ObjectMapper mapper = new ObjectMapper();
		T result = null;
		try {
			result = mapper.readValue(json, clazz);
		} catch (Exception e) {
		}
		return result;
	}

}
