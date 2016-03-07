package com.kankan.op.utils;

import java.io.InputStream;
import java.util.Properties;

/**
 * 用于获取应用程序的部署环境的信息 已经重新配置获取途径。从数据库中获取所有的配置信息而不从属性文件中获取
 * 
 */
public class ApplicationConfigUtil {

	protected static final Properties prop = new Properties();

	static {
		try {
			InputStream in = ApplicationConfigUtil.class
					.getResourceAsStream("/commonConfig.properties");
			prop.load(in);
			in.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 是否需要清除临时文件，可通过配置/commonConfig.properties的clearuptempdir属性
	 * 
	 * @return 是否需要清除临时文件
	 */
	public static boolean isClearupTempdir() {
		return "true".equalsIgnoreCase(prop.getProperty("clearuptempdir"));
	}

	/**
	 * 是否保存cookies
	 * 
	 * @return 是否保存cookies
	 */
	public static boolean isCookies() {
		return "true".equalsIgnoreCase(prop.getProperty("iscookies"));
	}

	/**
	 * 获得指定key的指。可以指定key的默认值。
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static String getValue(String key, String defaultValue) {
		return prop.getProperty(key, defaultValue);
	}

	/**
	 * 获得指定key的指。
	 * 
	 * @param key
	 * @return
	 */
	public static String getValue(String key) {
		return prop.getProperty(key);
	}

}
