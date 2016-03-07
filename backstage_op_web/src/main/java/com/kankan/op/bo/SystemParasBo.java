package com.kankan.op.bo;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kankan.op.cache.CacheKeys;
import com.kankan.op.cache.RuleMgtDicCache;
import com.kankan.op.utils.ApplicationConfigUtil;
import com.kankan.op.utils.Constants;

/**
 * 获取权限系统定义的参数信息数组
 * 
 * @author lianshuping
 * @date 2016年2月24日
 */
@Component("systemParasBo")
public class SystemParasBo {

	@Autowired
	RuleMgtDicCache rulesMgtCache;

	/**
	 * 所属团队
	 * 
	 * @return
	 */
	public Map<String, String> getTeamList() {
		return rulesMgtCache.getYZGParamsCache(
				ApplicationConfigUtil.getValue(Constants.yzg_sys_param_team),
				null, CacheKeys.TEAM_KEY);
	}

	/**
	 * 奖品分类
	 * 
	 * @return
	 */
	public Map<String, String> getPrizeType() {
		return rulesMgtCache.getYZGParamsCache(ApplicationConfigUtil
				.getValue(Constants.yzg_sys_param_prizetype), null,
				CacheKeys.PRIZE_TYPE_KEY);
	}

	/**
	 * 奖品分类下的二级数组
	 * 
	 * @return
	 */
	public Map<String, String> getPrizeEntity(String paramCode) {
		String cachekey = Constants.yzg_sys_param_prizeentity_prefix
				+ paramCode;
		return rulesMgtCache.getYZGParamsCache(null, cachekey, cachekey);
	}

}
