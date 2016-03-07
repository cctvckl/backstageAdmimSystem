package com.kankan.op.beans;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PrizeBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Long seqId;
	
	/**
	 * 奖品名称
	 */
	private String prizeName;
	/**
	 * 奖品KEY
	 */
	private String prizeKey;
	/**
	 * 奖品类型，来源权限系统的数组参数
	 */
	private String prizeType = "";

	private String prizeTypeName;

	/**
	 * 具体奖品，来源权限系统的数组参数
	 */
	private String prizeEntity = "";

	private String prizeEntityName;

	/**
	 * 具体奖品批号，为空表示改奖品没有设置批号
	 */
	private String prizeEntityId = "";
	/**
	 * 初始库存
	 */
	private int stock;
	/**
	 * 已经发放奖品数
	 */
	private int realSaleNum;
	
	/**
	 * 真实库存： 初始库存-已经发放奖品数。供页面显示使用
	 */
	private int realStock;
	
	/**
	 * 每日发送限制
	 */
	private int sendLimit;
	/**
	 * 单个用户领取限制
	 */
	private int useLimit;
	/**
	 * 开始时间
	 */
	private String startTime;
	/**
	 * 结束时间
	 */
	private String endTime;
	/**
	 * 有效期，0为无限制（有的奖品是没有有效期的，比如实物奖品）
	 */
	private int validDay;
	/**
	 * 发送频率预警，-1为无限制
	 */
	private int sendLevel = -1;
	/**
	 * 奖品库存预警，-1为无限制
	 */
	private int stockLevel = -1;
	/**
	 * 创建人
	 */
	private String createBy = "";
	/**
	 * 创建时间
	 */
	private String createTime = "";
	/**
	 * 修改人
	 */
	private String editBy = "";
	/**
	 * 修改时间
	 */
	private String editTime = "";

	/**
	 * 状态。供页面显示使用
	 */
	private String statusDesc;

	public Long getSeqId() {
		return seqId;
	}

	public void setSeqId(Long seqId) {
		this.seqId = seqId;
	}

	public String getPrizeName() {
		return prizeName;
	}

	public void setPrizeName(String prizeName) {
		this.prizeName = prizeName;
	}

	public String getPrizeKey() {
		return prizeKey;
	}

	public void setPrizeKey(String prizeKey) {
		this.prizeKey = prizeKey;
	}

	public String getPrizeType() {
		return prizeType;
	}

	public void setPrizeType(String prizeType) {
		this.prizeType = prizeType;
	}

	public String getPrizeEntity() {
		return prizeEntity;
	}

	public void setPrizeEntity(String prizeEntity) {
		this.prizeEntity = prizeEntity;
	}

	public String getPrizeEntityId() {
		return prizeEntityId;
	}

	public void setPrizeEntityId(String prizeEntityId) {
		this.prizeEntityId = prizeEntityId;
	}

	public int getStock() {
		return stock;
	}

	public void setStock(int stock) {
		this.stock = stock;
	}

	public int getRealSaleNum() {
		return realSaleNum;
	}

	public void setRealSaleNum(int realSaleNum) {
		this.realSaleNum = realSaleNum;
	}

	public int getSendLimit() {
		return sendLimit;
	}

	public void setSendLimit(int sendLimit) {
		this.sendLimit = sendLimit;
	}

	public int getUseLimit() {
		return useLimit;
	}

	public void setUseLimit(int useLimit) {
		this.useLimit = useLimit;
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

	public int getValidDay() {
		return validDay;
	}

	public void setValidDay(int validDay) {
		this.validDay = validDay;
	}

	public int getSendLevel() {
		return sendLevel;
	}

	public void setSendLevel(int sendLevel) {
		this.sendLevel = sendLevel;
	}

	public int getStockLevel() {
		return stockLevel;
	}

	public void setStockLevel(int stockLevel) {
		this.stockLevel = stockLevel;
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

	public String getPrizeTypeName() {
		return prizeTypeName;
	}

	public void setPrizeTypeName(String prizeTypeName) {
		this.prizeTypeName = prizeTypeName;
	}

	public String getPrizeEntityName() {
		return prizeEntityName;
	}

	public void setPrizeEntityName(String prizeEntityName) {
		this.prizeEntityName = prizeEntityName;
	}

	public String getStatusDesc() {
		Date effecttimeDate;
		Date expiretimeDate;
		try {
			SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			effecttimeDate = DEFAULT_DATE_FORMAT.parse(startTime);
			expiretimeDate = DEFAULT_DATE_FORMAT.parse(endTime);
			Date currentTime = new Date();
			if (effecttimeDate.compareTo(currentTime) <= 0
					&& currentTime.compareTo(expiretimeDate) < 0) {
				statusDesc = "有效";
			} else {
				statusDesc = "无效";
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return statusDesc;
	}

	public void setStatusDesc(String statusDesc) {
		this.statusDesc = statusDesc;
	}

	public int getRealStock() {
		return stock - realSaleNum;
	}

	public void setRealStock(int realStock) {
		this.realStock = realStock;
	}
	
	
}
