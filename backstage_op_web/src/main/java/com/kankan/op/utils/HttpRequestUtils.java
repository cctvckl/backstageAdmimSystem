package com.kankan.op.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

@Service("damaiParser")
public class HttpRequestUtils {

//    @Value("${get_new_dynamic_url}")
//    private String url;

    public static String doPost(long maitianId, long sinceId) {
        String result = "";
        String url = null;
        HttpPost httpRequst = new HttpPost(url);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("maitianId", maitianId);
        jsonObject.put("sinceId", sinceId);

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("json", jsonObject.toJSONString()));

        try {
            httpRequst.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));
            HttpResponse httpResponse = HttpRequestHelper.getHttpClient().execute(httpRequst);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity httpEntity = httpResponse.getEntity();
                result = EntityUtils.toString(httpEntity, Consts.UTF_8);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = e.getMessage().toString();
        }

        return result;
    }

    /**
     * get请求数据
     * 
     * @date 2016年1月13日
     * @author lili
     * @param url
     * @return
     * @returnType String
     */
    public static String doGet(String url) {
        if (StringUtils.isEmpty(url)) {
            return null;
        }
        String result = null;
        try {
            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = HttpRequestHelper.getHttpClient().execute(httpGet);
            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                HttpEntity httpEntity = response.getEntity();
                result = EntityUtils.toString(httpEntity, Consts.UTF_8);
            }
        } catch (Exception e) {
//            log.error(e);
        }
        return result;
    }
    
    /**
     * 解析数据
     * 
     * @param text
     * @param maitianId
     * @param recursion
     * @return
     */
    private long parseJsonText(String text, long maitianId,boolean recursion) {
        JSONObject jsonObject = JSONObject.parseObject(text);

        // 接口成功返回时解析数据
        if (jsonObject.containsKey("code") && jsonObject.getInteger("code") == HttpStatus.SC_OK) {
            if (jsonObject.containsKey("data")) {
                JSONObject data = jsonObject.getJSONObject("data");
                JSONArray dynamics = data.getJSONArray("dynamic");

                if (dynamics.size() > 0) {
                    for (int i = 0; i < dynamics.size(); i++) {
                        JSONObject dynamic = dynamics.getJSONObject(i);

                        // 只抓取facebook、twitter、instagram的数据
                        if (dynamic.getInteger("type") == 4) {
                            int socialType = dynamic.getInteger("socialType");

                            
                        }
                    }
                    
                    if (recursion) {
                        return dynamics.getJSONObject(dynamics.size() - 1).getLongValue("sortTime");
                    } else {
                        return -1;
                    }
                }
            }
        }

        return -1;
    }

}
