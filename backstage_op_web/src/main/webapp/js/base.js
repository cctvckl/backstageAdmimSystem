//必填项是否为空校验
function requireCheck(formObj){
	var	boxes = formObj.find("span[id^='chk_']");
	if(boxes && boxes.length>=1){
		for(var i=0; i< boxes.length; i++) {
			var chkObjNameTemp = boxes[i].id.substring(4);
			$chkObjNameTemp = document.getElementById(chkObjNameTemp);
			if($chkObjNameTemp && $chkObjNameTemp.value.trim()=="" ){
				alert(boxes[i].innerHTML+" 不能为空");
				$chkObjNameTemp.value = $chkObjNameTemp.value.trim();
				$chkObjNameTemp.focus();
				return false;
			}
		}
	}
	return true;
}
//详情信息回填
function detailShow(divObj, row){
	var	boxes = divObj.find("div[id^='show_']");
	if(boxes && boxes.length>=1){
		for(var i=0; i< boxes.length; i++) {
			var nameTemp = boxes[i].id.substring(5);
			$('#show_'+nameTemp).html(eval('row.'+nameTemp));
		}
	}
}

/**
 * 通过ajax请求填充对应的select内容
 * @param selObject select对象
 * @param reqUrl 请求url
 * @param selVal select选中内容值
 * @param optionOne option选项是否填充空，true是，false否
 */
function initSelectOption(selObject, reqUrl, selVal, optionOne){
	$.ajax({
        type: "GET",
        dataType: "json",
        url: reqUrl,
        success: function (data) {
        	var selectBody = "";
        	if(optionOne){
        		selectBody = "<option value=''></option>";
        	}
            $.each(data, function (i, n) {
            	if(selVal!="" && n.parakey==selVal){
            		 selectBody += "<option value='" + n.parakey + "' selected='selected'>" + n.paraval + "</option>";
            	}else{
            		 selectBody += "<option value='" + n.parakey + "'>" + n.paraval + "</option>";
            	}
            });
            selObject.html(selectBody);
        },
        error: function (json) {
           
        }
    });
}
function createSelectByArray(array) {
	var selectBody = "<option value=''></option>";
	$.each(array, function () {
		selectBody += "<option value=" + this.id + ">" + this.name + "</option>";
	});
	return selectBody;
}