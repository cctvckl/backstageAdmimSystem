package com.kankan.op.beans;

import java.io.Serializable;

public class UpCutVo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6932794786890174367L;
	private double fullPrice;
	private double minusPrice;
	public double getFullPrice() {
		return fullPrice;
	}
	public void setFullPrice(double fullPrice) {
		this.fullPrice = fullPrice;
	}
	public double getMinusPrice() {
		return minusPrice;
	}
	public void setMinusPrice(double minusPrice) {
		this.minusPrice = minusPrice;
	}
}
