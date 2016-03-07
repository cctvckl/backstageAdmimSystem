//注册送模板对应的js

function registerClick_Register() {
    //注册送模板start---------------------
    //点击加号和减号实现复制规则奖品的功能
    $("span[name=button_rmvRulePrizeItem]").click(function () {
        $(this).parent().parent().parent().remove();
    });
    $("span[name=button_addRulePrizeItem]").click(function () {
        //debugger;
        var copyedRule = $(this).parent().parent().parent().clone(true);
        copyedRule.find("span[name=button_addRulePrizeItem]").hide();
        copyedRule.find("span[name=button_rmvRulePrizeItem]").show();
        copyedRule.appendTo($prizeInfoDiv);
    });
    $("input[name=prizeId]").blur(function(){
        var $prizeKey = $(this).parent().parent().parent().find("input[name=prizeKey]");
        $.ajax({
            type: "GET",
            dataType: "json",
            url: "/backstage_op_web/getPrizeKeyById.do",
            data: {prizeId: $(this).val()},//将对象序列化成JSON字符串
            success: function (json) {
                if (json.rtn != 0) {
                    console.log("error: json.rtn != 0 ");
                    return;
                }
                $prizeKey.val(json.prizeKey);
                console.log("prizeKey:" + json.prizeKey);
            },
            error: function (json) {
                console.log(json.msg);
                alert("未查到对应的奖品");
            }
        });
    });
    //注册送模板end---------------------
}
function fillPrizeHtml_Register(data) {
    debugger;
    var ruleExtVosArray = data;

    var prizeInfoDivHtml = "";
    $.get('pages/manage/ruleMgt_Register.html', function (htmlRsp) {
        //------------------------------------begin:页面生成------------------------------------------
        htmlRsp = htmlRsp.slice(htmlRsp.indexOf('<body>') + 6, htmlRsp.indexOf('</body>'));
        //有几个奖励项，就生成几个htmlRsp
        for (var i = 0; i < ruleExtVosArray.length; i++) {
            prizeInfoDivHtml += htmlRsp;
        }
        debugger;
        //注入奖品区域div
        $prizeInfoDiv.html(prizeInfoDivHtml);
        //修改奖品区域div的内容，（因为默认只包含一个满减规则行），这边需要根据服务器返回的满减规则数组生
        // 成对应数量的满减规则行
        $($prizeInfoDiv.find(".rulePrizeDivToClone_RegisterGift")).each(function (index) {
            //奖励页第一项为加号，后面为减号
            if (index != 0) {
                $(this).find("span[name=button_addRulePrizeItem]").hide();
                $(this).find("span[name=button_rmvRulePrizeItem]").show();
            }
        });
        //------------------------------------end:页面生成------------------------------------------

        //------------------------------------begin:回填数据------------------------------------------
        //如下的数组的index，是对应到ruleExtVosArray的；因为奖励div数量是由ruleExtVosArray的长度决定的
        $($prizeInfoDiv.find(".rulePrizeDivToClone_RegisterGift")).each(function (index) {
            var ruleExtVo = ruleExtVosArray[index];
            //根据后台数据设置具体奖品类型的选中值
            $.each($(this).find("select[name=prizeDetailTypeList] option"), function (index) {
//                                    debugger;
                if ($(this).attr(selectOptionAttrName) == ruleExtVo.prizeDetailTypeId) {
                    $(this).attr("selected", "selected");
                }
            });
            //根据后台数据设置活动类型的选中值
            $.each($(this).find("select[name=actTypeList] option"), function (index) {
//                                    debugger;
                if ($(this).attr(selectOptionAttrName) == ruleExtVo.actTypeId) {
                    $(this).attr("selected", "selected");
                }
            });

            //根据后台数据设置满减规则数据
            //以下两个为数组元素，需要将这两个数组按下标一一对应起来;两个数组的大小肯定是一样的，因为满多少减多少肯定是成对出现
            var $prizeId = $(this).find('input[name=prizeId]');
            var $prizeKey = $(this).find('input[name=prizeKey]');
            $prizeId.val(ruleExtVo.prizeId);
            $prizeKey.val(ruleExtVo.prizeKey);
        });
        //------------------------------------end:回填数据------------------------------------------

        //重要：注册按钮的点击事件；此处注册事件要写在循环体外面，否则会给一个span元素重复注册多次；导致的后果是点击一次添加会添加多条
        registerClick_Register();

    });

}