package com.kankan.op.beans;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ActivityIdBean {

	/*
	 * 活动id
	 */
	private long seqid;
	/*
	 * 活动名称
	 */
	private String actname;
	/*
	 * 来源team
	 */
	private String teamid;
	/*
	 * 来源team
	 */
	public String teamname;
	/*
	 * 生效时间
	 */
	private String effecttime;
	/*
	 * 过期时间
	 */
	private String expiretime;
	
	/*
	 * 创建人
	 */
	private String createby;
	/*
	 * 创建时间
	 */
	private String createtime;
	/*
	 * 修改人
	 */
	private String editby;
	/*
	 * 修改时间
	 */
	private String edittime;

	private String statusdesc;

	public ActivityIdBean() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ActivityIdBean(String actname, String teamid, String effecttime,
			String expiretime, String createby, String createtime,
			String editby, String edittime) {
		super();
		this.actname = actname;
		this.teamid = teamid;
		this.effecttime = effecttime;
		this.expiretime = expiretime;
		this.createby = createby;
		this.createtime = createtime;
		this.editby = editby;
		this.edittime = edittime;
	}

	public long getSeqid() {
		return seqid;
	}

	public void setSeqid(long seqid) {
		this.seqid = seqid;
	}

	public String getActname() {
		return actname;
	}

	public void setActname(String actname) {
		this.actname = actname;
	}

	public String getTeamid() {
		return teamid;
	}

	public void setTeamid(String teamid) {
		this.teamid = teamid;
	}

	public String getEffecttime() {
		return effecttime;
	}

	public void setEffecttime(String effecttime) {
		this.effecttime = effecttime;
	}

	public String getExpiretime() {
		return expiretime;
	}

	public void setExpiretime(String expiretime) {
		this.expiretime = expiretime;
	}

	public String getCreateby() {
		return createby;
	}

	public void setCreateby(String createby) {
		this.createby = createby;
	}

	public String getCreatetime() {
		return createtime;
	}

	public void setCreatetime(String createtime) {
		this.createtime = createtime;
	}

	public String getEditby() {
		return editby;
	}

	public void setEditby(String editby) {
		this.editby = editby;
	}

	public String getEdittime() {
		return edittime;
	}

	public void setEdittime(String edittime) {
		this.edittime = edittime;
	}

	public String getStatusdesc() {
		Date effecttimeDate;
		Date expiretimeDate;
		try {
			SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			effecttimeDate = DEFAULT_DATE_FORMAT.parse(effecttime);
			expiretimeDate = DEFAULT_DATE_FORMAT.parse(expiretime);
			Date currentTime = new Date();
			if (effecttimeDate.compareTo(currentTime) <= 0
					&& currentTime.compareTo(expiretimeDate) < 0) {
				statusdesc = "有效";
			} else {
				statusdesc = "无效";
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return statusdesc;
	}

	public void setStatusdesc(String statusdesc) {
		this.statusdesc = statusdesc;
	}

	@Override
	public String toString() {
		return "ActivityIdBean [seqid=" + seqid + ", actname=" + actname
				+ ", teamid=" + teamid + ", teamname=" + teamname
				+ ", effecttime=" + effecttime + ", expiretime=" + expiretime
				+ ", createby=" + createby
				+ ", createtime=" + createtime + ", editby=" + editby
				+ ", edittime=" + edittime + "]";
	}

}
