package com.kankan.op.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RuleVo  implements Serializable{
	private static final long serialVersionUID = 5304223814762803515L;
	private Long seqId;
	private String ruleName;
	private Long templateId;
	public static final int templateId_UpCut = 1;
	public static final int templateId_Register = 2;
	private String templateName;
	private String actId;
	private String actName;
	private String ruleStatusId;
	private String ruleStatusName;
	private String startTime;
    private String endTime;
    private String vipGrade;
    private Integer timeLimit;
    private String createBy;
    private String createTime;
    private String editBy;
    private String editTime;
    private List<RuleExtVo> ruleExtVos=new ArrayList<RuleExtVo>();
	public Long getSeqId() {
		return seqId;
	}
	public void setSeqId(Long seqId) {
		this.seqId = seqId;
	}
	public String getRuleName() {
		return ruleName;
	}
	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}
	public String getActId() {
		return actId;
	}
	public void setActId(String actId) {
		this.actId = actId;
	}
	
	public String getActName() {
		return actName;
	}
	public void setActName(String actName) {
		this.actName = actName;
	}
	public String getRuleStatusId() {
		return ruleStatusId;
	}
	public void setRuleStatusId(String ruleStatusId) {
		this.ruleStatusId = ruleStatusId;
	}
	public String getRuleStatusName() {
		return ruleStatusName;
	}
	public void setRuleStatusName(String ruleStatusName) {
		this.ruleStatusName = ruleStatusName;
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	public String getVipGrade() {
		return vipGrade;
	}
	public void setVipGrade(String vipGrade) {
		this.vipGrade = vipGrade;
	}
	public Integer getTimeLimit() {
		return timeLimit;
	}
	public void setTimeLimit(Integer timeLimit) {
		this.timeLimit = timeLimit;
	}
	public String getCreateBy() {
		return createBy;
	}
	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public String getEditBy() {
		return editBy;
	}
	public void setEditBy(String editBy) {
		this.editBy = editBy;
	}
	public String getEditTime() {
		return editTime;
	}
	public void setEditTime(String editTime) {
		this.editTime = editTime;
	}
	
	public List<RuleExtVo> getRuleExtVos() {
		return ruleExtVos;
	}
	public void setRuleExtVos(List<RuleExtVo> ruleExtVos) {
		this.ruleExtVos = ruleExtVos;
	}
	public Long getTemplateId() {
		return templateId;
	}
	public void setTemplateId(Long templateId) {
		this.templateId = templateId;
	}
	public String getTemplateName() {
		return templateName;
	}
	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}
    
    
}
