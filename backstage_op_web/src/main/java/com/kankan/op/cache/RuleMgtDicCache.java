package com.kankan.op.cache;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import redis.clients.jedis.exceptions.JedisException;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.kankan.op.utils.ApplicationConfigUtil;
import com.kankan.op.utils.HttpRequestUtils;
import com.kankan.op.utils.JSONUtil;
import com.kankan.op.utils.ServiceUrlUtil;

/**
 * 
 * @author liqing
 */
@Service
public class RuleMgtDicCache {

	@Resource(name = "jedisTemplateOPRead")
	public JedisTemplate jedisTemplateOPRead;

	/**
	 * 参数ID，和“参数编号”必传一个
	 * 
	 * @param paramId
	 *            参数ID
	 * @param paramCode
	 *            参数编号
	 * @param cacheKey
	 *            缓存key
	 * @return
	 */
	public Map<String, String> getYZGParamsCache(String paramId,
			String paramCode, String cacheKey) {
		Map<String, String> result = null;
		try {
			String jsonData = jedisTemplateOPRead.get(cacheKey);
			if (jsonData == null) { // 表示需要从数据库重新加载
				result = loadYZGParamsCache(paramId, paramCode, cacheKey);
			} else {
				result = JSONUtil.toObject(jsonData, LinkedHashMap.class);
			}
		} catch (JedisException ex) {
			throw ex;
		}
		return result;
	}

	private Map<String, String> loadYZGParamsCache(String paramId,
			String paramCode, String cacheKey) {

		Map<String, String> parasMap = new HashMap<String, String>();
		parasMap.put("api",
				ApplicationConfigUtil.getValue("yzg.param.detail.query"));
		// 参数ID，和“参数编号”必传一个
		if (paramId == null) {
			parasMap.put("paramCode", paramCode);
		} else {
			parasMap.put("paramId", paramId);
		}
		//排序方式
		parasMap.put("order", "detailorder");

		// 优值供活动类型字典
		String _url = ServiceUrlUtil.getRequestUrl("authService_host",
				"systemService_port", "systemService_name", parasMap);
		String acttypeResult = HttpRequestUtils.doGet(_url);
		Map<String, String> result = new LinkedHashMap<String, String>();
		JSONObject jsonObject = JSONObject.parseObject(acttypeResult);
		if (jsonObject.containsKey("code")) {
			String code = jsonObject.getString("code");
			if ("OK".equals(code)) {
				if (jsonObject.containsKey("data")) {
					JSONObject data = jsonObject.getJSONObject("data");
					if (data.containsKey("datas")) {
						JSONArray jsonArr = JSONObject.parseArray(data
								.getString("datas"));
						for (int i = 0; i < jsonArr.size(); i++) {
							JSONObject o = jsonArr.getJSONObject(i);
							String seqid = o.getString("seqId");
							String detailValue = o.getString("detailValue");
							result.put(seqid, detailValue);
						}
					}
				}
				jedisTemplateOPRead.set(cacheKey, JSONUtil.fromObject(result));
			}
		}

		return result;
	}

}
