package com.kankan.op.utils;

public class Constants {
	
	//优职供请求参数
	public static final String api = "api";
	public static final String loginPwd = "loginPwd";
	public static final String authService_host = "authService_host";
	public static final String authService_port = "authService_port";
	public static final String authService_name = "authService_name";
	public static final String hrService_host = "hrService_host";
	public static final String hrService_port = "hrService_port";
	public static final String hrService_name = "hrService_name";
	public static final String systemService_host = "systemService_host";
	public static final String systemService_port = "systemService_port";
	public static final String systemService_name = "systemService_name";
	public static final String paramId = "paramId";
	public static final String appId = "appId";
	public static final String staffId = "staffId";
	public static final String roleId = "roleId";
	public static final String forCode = "forCode";
	public static final String system_perm_type = "systemPermType";
	public static final String system_perm_type_menu = "1";
	public static final String system_perm_type_operation = "2";
	public static final String system_perm_type_data = "3";
	//优职供服务
	public static final String loginName = "loginName";
	public static final String yzg_auth_account_login = "yzg.auth.account.login";
	public static final String yzg_auth_menu_getAll = "yzg.auth.menu.getAll";
	public static final String yzg_auth_menu_get = "yzg.auth.account.getMenuTree";
	public static final String yzg_param_detail_query = "yzg.param.detail.query";
	
	//参数数组定义
	public static final String yzg_sys_param_team = "yzg.team.paramid";
	public static final String yzg_sys_param_prizetype = "yzg.team.prizetype";
	public static final String yzg_sys_param_prizeentity_prefix = "backstage_op_prize_"; //具体奖品参数编号前缀
	
	/**
	 * hrService
	 */
	public static final String yzg_hr_account_getAllRole = "yzg.hr.account.getAllRole";
	public static final String yzg_roles_split_char = ",";
	
	/**
	 * authService
	 */
	public static final String yzg_auth_grant_role_getPerm = "yzg.auth.grant.role.getPerm";
	
	
	//优职供响应
	public static final String loginId = "loginId";
	public static final String OK = "OK";
	public static final String onlyDir = "onlyDir";
	public static final String data = "data";
	
	//session key常量
	public static final String menuPerData = "menuPerData";
	public static final String user_auth_ops = "user_auth_ops"; 
	
	//浏览器http请求参数
	public static final String activity = "activity";
	public static final String pageNumber = "pageNumber";
	public static final String pageSize = "pageSize";
	public static final String seqids = "seqids";
	public static final String rule = "rule";
	public static final String ruleId = "ruleId";
	public static final String prizeinfo = "prizeinfo";
	public static final String prizeId = "prizeId";
	
	//向后台发送的请求的参数key
	public static final String userId = "userId";
	
	//系统操作名称，用于操作权限前缀
	public static final String system_operation = "op.backstage.";
	
	//请求后缀
	public static final String request_surfix=".do";
	
	public static final String FAIL = "-1";
	public static final String SUCCESS = "0";
	
}
