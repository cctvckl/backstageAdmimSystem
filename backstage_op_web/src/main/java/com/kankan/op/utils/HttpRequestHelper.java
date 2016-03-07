package com.kankan.op.utils;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * HTTP请求助手
 * 
 * @author huqixin
 */
public class HttpRequestHelper {

    private static ThreadLocal<CloseableHttpClient> localHttpClient = new ThreadLocal<CloseableHttpClient>();

    public static CloseableHttpClient getHttpClient() {
        if (null == localHttpClient.get()) {
            return HttpClients.createDefault();
        } else {
            return localHttpClient.get();
        }
    }

}
