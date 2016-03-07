package com.kankan.op.beans;

public class SystemParaBean {

	private String parakey;

	private String paraval;

	public SystemParaBean(String parakey, String paraval) {
		super();
		this.parakey = parakey;
		this.paraval = paraval;
	}

	public String getParakey() {
		return parakey;
	}

	public void setParakey(String parakey) {
		this.parakey = parakey;
	}

	public String getParaval() {
		return paraval;
	}

	public void setParaval(String paraval) {
		this.paraval = paraval;
	}

}
