package com.kankan.op.utils;

import java.util.HashMap;
import java.util.Map;


public class ServiceUrlUtil {

	private static Map<String, String> urlMap = new HashMap<String, String>();

	/**
	 * 获取请求接口url前半部分。如：http://10.11.10.124:58801/authService?
	 * 
	 * @param host
	 * @param port
	 * @param serviceName
	 * @return
	 */
	private static String getHostPortByService(String host, String port,
			String serviceName) {
		if (urlMap.get(serviceName) == null) {
			StringBuilder sb = new StringBuilder();
			sb.append("http://").append(ApplicationConfigUtil.getValue(host))
					.append(":")
					.append(ApplicationConfigUtil.getValue(port, "80"))
					.append("/")
					.append(ApplicationConfigUtil.getValue(serviceName))
					.append("?");
			urlMap.put(serviceName, sb.toString());
		}
		return urlMap.get(serviceName);
	}

	/**
	 * 获取请求接口url。
	 * 
	 * @param host
	 * @param port
	 * @param serviceName
	 * @param parasMap
	 * @return
	 */
	public static String getRequestUrl(String host, String port,
			String serviceName, Map<String, String> parasMap) {

		String hostportnameStr = getHostPortByService(host, port, serviceName);
		if (parasMap == null || parasMap.isEmpty()) {
			return hostportnameStr;
		}
		StringBuilder sb = new StringBuilder(hostportnameStr);
		for (Map.Entry<String, String> entry : parasMap.entrySet()) {
			if (entry.getValue() == null) {
				continue;
			}
			sb.append(entry.getKey()).append("=").append(entry.getValue())
					.append("&");
		}
		return sb.substring(0, sb.length() - 1);
	}

}
