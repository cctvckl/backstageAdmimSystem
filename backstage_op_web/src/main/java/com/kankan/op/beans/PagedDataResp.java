package com.kankan.op.beans;

import java.util.List;


public class PagedDataResp<T> {
	public int rtn;
	public int total;
	public List<T> rows;
}
