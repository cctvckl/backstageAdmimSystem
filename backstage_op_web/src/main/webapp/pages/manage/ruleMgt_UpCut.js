//满减模板对应的js

function registerClick_UpCut() {
    //满减模板start---------------------
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
    $("span[name=button_addUpCutItem]").click(function () {
        var source = $(this).parent();
        var copyedUpCutDiv = source.clone(false);
        copyedUpCutDiv.find("span[name=button_addUpCutItem]").hide();
        copyedUpCutDiv.find("span[name=button_rmvUpCutItem]").show();
        copyedUpCutDiv.appendTo(source.parent());
        //绑定删除按钮事件
        $("span[name=button_rmvUpCutItem]").click(function () {
            debugger;
            $(this).parent().remove();
        });
    });

    //满减模板end---------------------
}
function fillPrizeHtml_UpCut(data) {
    var ruleExtVosArray = data;

    var prizeInfoDivHtml = "";
    $.get('pages/manage/ruleMgt_UpCut.html', function (htmlRsp) {
        //------------------------------------begin:页面生成------------------------------------------
        htmlRsp = htmlRsp.slice(htmlRsp.indexOf('<body>') + 6, htmlRsp.indexOf('</body>'));
        //有几个奖励项，就生成几个htmlRsp
        for (var i = 0; i < ruleExtVosArray.length; i++) {
            prizeInfoDivHtml += htmlRsp;
        }
        //注入奖品区域div
        $prizeInfoDiv.html(prizeInfoDivHtml);
        //修改奖品区域div的内容，（因为默认只包含一个满减规则行），这边需要根据服务器返回的满减规则数组生
        // 成对应数量的满减规则行
        $($prizeInfoDiv.find(".rulePrizeDivToClone_UpCut")).each(function (index) {
            //奖励页第一项为加号，后面为减号
            if (index != 0) {
                $(this).find("span[name=button_addRulePrizeItem]").hide();
                $(this).find("span[name=button_rmvRulePrizeItem]").show();
            }
            //以下生成对应条数的满减规则行
            var currentRuleExtVos = ruleExtVosArray[index];
            for (var j = 0; j < currentRuleExtVos.upCutVos.length; j++) {
                if (j == 0) {
                    //因为默认有一个了，这边跳过一个
                    continue;
                }
                //添加满减规则的那个span的父元素就是我们要复制的那个满减规则div，克隆一份附着到父元素
                var source = $(this).find("span[name=button_addUpCutItem]").parent();
                var copyedUpCutDiv = source.clone(true);
                //除了第一条满减规则显示加号，其余的都显示减号
                copyedUpCutDiv.find("span[name=button_addUpCutItem]").hide();
                copyedUpCutDiv.find("span[name=button_rmvUpCutItem]").show();
                copyedUpCutDiv.appendTo(source.parent());
            }
        });
        //------------------------------------end:页面生成------------------------------------------

        //------------------------------------begin:回填数据------------------------------------------
        //如下的数组的index，是对应到ruleExtVosArray的；因为奖励div数量是由ruleExtVosArray的长度决定的
        $($prizeInfoDiv.find(".rulePrizeDivToClone_UpCut")).each(function (index) {
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
            var upPriceListArray = $(this).find('input[name=upPriceToGetCutPrice]');
            var cutPriceListArray = $(this).find('input[name=cutPriceOfPrize]');
            for (var i = 0; i < ruleExtVo.upCutVos.length; i++) {
                upPriceListArray[i].value = ruleExtVo.upCutVos[i].fullPrice;
                cutPriceListArray[i].value = ruleExtVo.upCutVos[i].minusPrice;
//                                    debugger;
            }

        });
        //------------------------------------end:回填数据------------------------------------------

        //重要：注册按钮的点击事件；此处注册事件要写在循环体外面，否则会给一个span元素重复注册多次；导致的后果是点击一次添加会添加多条
        registerClick_UpCut();

    });

}