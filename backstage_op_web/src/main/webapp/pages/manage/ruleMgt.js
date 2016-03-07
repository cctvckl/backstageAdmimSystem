/**
 * Created by LENOVO on 2016/3/2.
 */
//bean定义区
function UpCutVo(fullPrice, minusPrice) {
    this.fullPrice = fullPrice;
    this.minusPrice = minusPrice;
}
function RuleExtVo(prizeDetailTypeId, actTypeId) {
    this.prizeDetailTypeId = prizeDetailTypeId;
    this.actTypeId = actTypeId;
}
function RuleExtVo_UpCut(ruleExtVo,upCutVos) {
    //数组类型，存储满减规则list，元素类型为UpCutVo
    this.upCutVos = upCutVos;
    RuleExtVo.call(this,ruleExtVo.prizeDetailTypeId, ruleExtVo.actTypeId);
}
function RuleExtVo_Register(ruleExtVo,prizeId, prizeKey) {
    this.prizeId = prizeId;
    this.prizeKey = prizeKey;
    RuleExtVo.call(this,ruleExtVo.prizeDetailTypeId, ruleExtVo.actTypeId);
}
function RuleVo(seqId, ruleName, templateId, actId, ruleStatusId, startTime, endTime, ruleExtVos, vipGrade, timeLimit) {
    this.seqId = seqId;
    this.ruleName = ruleName;
    this.templateId = templateId;
    this.actId = actId;
    this.ruleStatusId = ruleStatusId;
    this.startTime = startTime;
    this.endTime = endTime;
    //数组类型，存储规则扩展奖励信息列表,元素类型为RuleExtVo
    this.ruleExtVos = ruleExtVos;
    //限制条件
    this.vipGrade = vipGrade;
    this.timeLimit = timeLimit;
}

function fillBaseRuleExtVo(parentDiv,templateId){
    var prizeDetailTypeId = parentDiv.find("select[name=prizeDetailTypeList] option:selected").attr(selectOptionAttrName);
    var actTypeId = parentDiv.find('select[name=actTypeList] option:selected').attr(selectOptionAttrName);

    debugger;
    var ruleExtVo = new RuleExtVo(prizeDetailTypeId, actTypeId);

    return ruleExtVo;
}

var TableInit = function () {

    var oTableInit = new Object();
    //初始化Table
    oTableInit.Init = function () {
        $table.bootstrapTable({
            url: "/backstage_op_web/getAllRuleList.do",
            method: "post",
            contentType: "application/x-www-form-urlencoded",
            toolbar: '#toolbar',				//工具按钮用哪个容器
            striped: true,					  //是否显示行间隔色
            cache: false,					   //是否使用缓存，默认为true，所以一般情况下需要设置一下这个属性（*）
            pagination: true,				   //是否显示分页（*）
            sortable: false,					 //是否启用排序
            sortOrder: "asc",				   //排序方式
            queryParams: oTableInit.queryParams,//传递参数（*）
            sidePagination: "server",		   //分页方式：client客户端分页，server服务端分页（*）
            pageNumber: 1,					   //初始化加载第一页，默认第一页
            pageSize: 10,					   //每页的记录行数（*）
            pageList: [10, 25, 50, 100],		//可供选择的每页的行数（*）
            showColumns: true,				  //是否显示所有的列
            showRefresh: true,				  //是否显示刷新按钮
            minimumCountColumns: 2,			 //最少允许的列数
            clickToSelect: true,				//是否启用点击选中行
            height: 500,						//行高，如果没有设置height属性，表格自动根据记录条数觉得表格高度
            uniqueId: "ID",					 //每一行的唯一标识，一般为主键列
            showToggle: true,					//是否显示详细视图和列表视图的切换按钮
            cardView: false,					//是否显示详细视图
            detailView: false,				   //是否显示父子表
            //设置为undefined可以获取pageNumber，pageSize，searchText，sortName，sortOrder
            //设置为limit可以获取limit, offset, search, sort, order
            queryParamsType: "undefined",
            columns: [{
                checkbox: true
            },
                {
                    field: 'seqId',
                    editable: true,
                    title: '规则ID',
                }, {
                    editable: true,
                    field: 'ruleName',
                    title: '规则名称'
                },
                {
                    editable: true,
                    field: 'createTime',
                    title: '创建时间'
                },
                {
                    field: 'startTime',
                    title: '开始时间'

                },
                {
                    field: 'endTime',
                    editable: true,
                    title: '结束时间'
                },
                {
                    editable: true,
                    field: 'ruleStatusName',
                    title: '状态'

                },
                {
                    field: 'editBy',
                    editable: true,
                    title: '最近更新'
                }, {
                    editable: true,
                    field: 'editTime',
                    title: '修改时间'
                }
            ]
        });
    };
    //进行ajax查询的参数
    oTableInit.queryParams = function (params) {
        var temp = {
            pageNumber: params.pageNumber,   //页面大小
            pageSize: params.pageSize,  //页码
//				activity : $("#queryRulename").val()
        };
        return temp;
    };
    return oTableInit;
};
