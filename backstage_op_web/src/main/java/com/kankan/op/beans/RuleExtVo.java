package com.kankan.op.beans;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RuleExtVo implements Serializable{
	
	private static final long serialVersionUID = -6932794786890174367L;
	
	private Long seqId;
	private Long ruleId;
	private Long prizeId;
	private String prizeDetailTypeId;//具体奖品，优职供
	private String prizeDetailTypeName;
	private String actTypeId;//活动类型，优职供
	private String actTypeName;
	private String actPrice;
	private List<UpCutVo> upCutVos = new ArrayList<UpCutVo>();;
	private String prizeKey;
	
	public Long getSeqId() {
		return seqId;
	}
	public void setSeqId(Long seqId) {
		this.seqId = seqId;
	}
	public Long getRuleId() {
		return ruleId;
	}
	public void setRuleId(Long ruleId) {
		this.ruleId = ruleId;
	}
	public Long getPrizeId() {
		return prizeId;
	}
	public void setPrizeId(Long prizeId) {
		this.prizeId = prizeId;
	}
	public String getPrizeDetailTypeId() {
		return prizeDetailTypeId;
	}
	public void setPrizeDetailTypeId(String prizeDetailTypeId) {
		this.prizeDetailTypeId = prizeDetailTypeId;
	}
	public String getPrizeDetailTypeName() {
		return prizeDetailTypeName;
	}
	public void setPrizeDetailTypeName(String prizeDetailTypeName) {
		this.prizeDetailTypeName = prizeDetailTypeName;
	}
	public String getActTypeId() {
		return actTypeId;
	}
	public void setActTypeId(String actTypeId) {
		this.actTypeId = actTypeId;
	}
	public String getActTypeName() {
		return actTypeName;
	}
	public void setActTypeName(String actTypeName) {
		this.actTypeName = actTypeName;
	}
	public String getActPrice() {
		return actPrice;
	}
	public void setActPrice(String actPrice) {
		this.actPrice = actPrice;
	}
	public List<UpCutVo> getUpCutVos() {
		return upCutVos;
	}
	public void setUpCutVos(List<UpCutVo> upCutVos) {
		this.upCutVos = upCutVos;
	}
	public String getPrizeKey() {
		return prizeKey;
	}
	public void setPrizeKey(String prizeKey) {
		this.prizeKey = prizeKey;
	}
	
	
	
}
