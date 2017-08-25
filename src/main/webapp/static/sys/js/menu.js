function getSelectedNode() {
	var nodes = treeObj.getSelectedNodes();

	return nodes && nodes.length > 0 ? treeObj.getSelectedNodes()[0] : null;
}

function popRootMenuDiv() {
	resetForm("MENU", true);
	//Global.popDiv("新增菜单", "#menuEditDiv", 500);
	
	//add("新增菜单", "menuEditDiv", 500);
	commonMessage.openWindow("新增菜单", "menuEditDiv", 700);
	//Global.popDiv("新增菜单", "#menuEditDiv", 700);
}

function popMenuDiv() {
	if (!getSelectedNode()) {
		commonMessage.message("请先选择一个父菜单");
		return;
	}
	resetForm("MENU");
	commonMessage.openWindow("新增菜单", "menuEditDiv", 500);
	layui.form().render();
}

function popMenu2Edit() {
	var currNode = getSelectedNode();
	if (!currNode) {
		commonMessage.message("请先选择一个需要编辑的菜单");
		return;
	}
	resetForm(currNode.dbType);
	$("#parentId").val(currNode.parentId);
//	var queryProjName = currNode.projectName;
//	$("#projectName > option[value='" + queryProjName + "']").attr("selected",
//			"true");
	$(".varName").each(function() {
		$(this).text("LINK" == currNode.dbType ? "功能" : "菜单");
	});
	
	if (currNode.isShare) {
		$("#shareRadio input:radio:last").attr("checked", "checked");
	}
	if (currNode.needDataCheck) {
		$("#dataCheckRadio input:radio:last").attr("checked", "checked");
	}
	$("#linkId").val(currNode.id);
	$("#linkName").val(currNode.name);
	$("#linkType").val(currNode.dbType);
	$("#linkUrl").val(currNode.link);
	$("#linkCode").val(currNode.code);
	$("#linkOrderRank").val(currNode.orderRank);
	$("#linkRemark").val(currNode.remark);
	commonMessage.openWindow("编辑", "menuEditDiv", 700);
	
	var nodeProjs = currNode.projectName ? currNode.projectName.split(",") : "";
	$("#projectName input:checkbox").each(function(i,d){
		var chbval = $(d).val();
		if($.inArray(chbval, nodeProjs) >= 0){
			$(d).prop("checked", "true");
		}
	});
	//layui.form().render('checkbox');
	layui.form().render();
}

function popLinkDiv() {
	if (!getSelectedNode()) {
		commonMessage.message("请先选择一个父菜单");
		return;
	}
	resetForm("LINK");
	commonMessage.openWindow("新增功能", "menuEditDiv", 700);
	layui.form().render();
}
function resetForm(linkType, isRoot) {
	var currNode = getSelectedNode();
	$("#parentId").val(currNode && !isRoot ? currNode.id : null);
	$("#linkType").val(linkType);
//	var queryProjName = $("#queryProj").val();
//	$("#projectName > option[value='" + queryProjName + "']").attr("selected",
//			"true");
	$(".varName").each(function() {
		$(this).text("LINK" == linkType ? "功能" : "菜单");
	});

	$("#linkId").val("");
	if("LINK" == linkType){
		$("#projDiv").css("display", "none");
	}else{
		$("#projDiv").css("display", "block");
	}
	$("#projectName input:checkbox").each(function(i,d){
		$(d).removeAttr("checked");
	});
	
	$("#shareRadio input:radio:first").attr("checked", "checked");
	$("#dataCheckRadio input:radio:first").attr("checked", "checked");
	$("#linkName").val("");
	$("#linkUrl").val("");
	$("#linkCode").val("");
	$("#linkOrderRank").val("");
	$("#linkRemark").val("");
	layui.form().render();
}
function popDistribRoleDiv() {
	var currNode = getSelectedNode();
	if (currNode == null) {
		commonMessage.message("请先选择一个节点");
		return;
	}
	$("#linkShowName").text(
			currNode.name + (currNode.link ? "(" + currNode.link + ")" : ""));
	$("#lkId").val(currNode.id);

	$.ajax({
				url : WEB_ROOT + "/sys/role/findAvailRoles.do?linkId="
						+ currNode.id,
				type : "GET",
				success : function(data) {
					if (data && data.resultCode != "SUCCESS") {
						commonMessage.message(data.errorMsg);
						return;
					}

					var roleContent = "";
					$(data.model.availRoles)
							.each(
									function() {
										roleContent += "<li><input type='checkbox' lay-skin='primary' title='"+this.name+"' value='"
												+ this.id
												+ "'"
												+ (data.model.roleIds
														&& $.inArray(this.id,
																data.model.roleIds) >= 0 ? "checked"
														: "")
												+ ">"
												+this.name
												+ "</li>";
									});

					$("#rolelistUl").html(roleContent);

					commonMessage.openWindow("关联角色", "roleDistribDiv", 700);
				}
			});

}

function saveRoleRelation() {
	var linkId = $("#lkId").val();
	var roleIds = "";
	$("#rolelistUl input[type='checkbox']:checked").each(function() {
		roleIds += $(this).val() + ",";
	});

	$.ajax({
		url : WEB_ROOT + "/sys/menu/saveRoleRelation.do",
		data : {
			linkId : linkId,
			roleIds : roleIds
		},
		type : "POST",
		success : function(data) {
			if (data && data.resultCode != "SUCCESS") {
				commonMessage.message(data.errorMsg);
				return;
			}

			commonMessage.message("操作成功");
			commonMessage.closeWindow();
		}
	});
}
function hasSameLink() {

	var hasSameLink = false;
	$.ajax({
		async : false,
		url : WEB_ROOT + "/sys/menu/hasSameLinkURL.do",
		data : {
			link : $("#linkUrl").val()
		},
		type : "POST",
		success : function(data) {

			if (data.model > 0) {
				hasSameLink = true;
			}
		}

	});

	return hasSameLink;
}

function saveLink() {
	var isUpdate = $("#linkId").val() ? true : false;
	var linkUrl = $("#linkUrl").val();
	
	if (linkUrl) {
		if (!isUpdate) {
			if (hasSameLink()) {
				commonMessage.message("链接地址 '"+ linkUrl +"' 已存在!");
				return;
			}
		}

	}
	var pjNameElems = $("#projectName input:checkbox:checked");
	if(pjNameElems.length <= 0 && linkUrl && "MENU" == $("#linkType").val()){
		commonMessage.message("可见web至少要勾选一个!");
		return;
	}
	var pjNames = "";
	pjNameElems.each(function(i, d){
		pjNames += $(d).val() + ",";
	});

	$.ajax({
		url : WEB_ROOT + "/sys/menu/saveLink.do",
		data : {
			id : $("#linkId").val(),
			parentId : $("#parentId").val(),
			name : $("#linkName").val(),
			link : $("#linkUrl").val(),
			projectName : pjNames,
			isShare : $("#shareRadio input:radio:checked").val(),
			needDataCheck : $("#dataCheckRadio input:radio:checked").val(),
			dbType : $("#linkType").val(),
			code : $("#linkCode").val(),
			orderRank : $("#linkOrderRank").val(),
			remark : $("#linkRemark").val()
		},
		type : "POST",
		success : function(data) {
			if (data && data.resultCode != "SUCCESS") {
				commonMessage.message(data.errorMsg);
				return;
			}

			var parentId = $("#parentId").val();
			var isUpdate = $("#linkId").val() ? true : false;

			var parentNode = getSelectedNode();
			var hasFirstExpanded = parentNode && parentNode.children; // 是否已经展开过

			commonMessage.message("操作成功!");
			commonMessage.closeWindow();
			// alert("parentId:" + parentId + "," +
			// JSON.stringify(data.userdata));
			// 重新加载parent下的子菜单
			if (parentId) {
				// treeObj.refresh();
				// treeObj.expandNode(parentNode, true, false);
				if (isUpdate) {
					var currNode = parentNode;
					currNode.name = data.model.name;
					currNode.link = data.model.link;
					currNode.projectName = data.model.projectName;
					currNode.code = data.model.code;
					currNode.orderRank = data.model.orderRank;
					currNode.remark = data.model.remark;
					currNode.isShare = data.model.isShare;
					currNode.needDataCheck = data.model.needDataCheck;

					treeObj.updateNode(currNode);
				} else {
					if (hasFirstExpanded) {
						treeObj.addNodes(parentNode, {
							id : data.model.id,
							pId : parentId,
							name : data.model.name,
							link : data.model.link,
							projectName : data.model.projectName,
							code : data.model.code,
							orderRank : data.model.orderRank,
							remark : data.model.remark,
							isShare : data.model.isShare,
							needDataCheck : data.model.needDataCheck,
							isParent : data.model.isParent
						});
					}
				}
			} else {
				loadLinkForProjectName($("#projectName").val());
			}

		}
	});
}

function deleteLink() {
	var currNode = getSelectedNode();
	if (currNode == null) {
		commonMessage.message("请选择一个菜单或者功能");
		return;
	}
	
	actions(WEB_ROOT + "/sys/menu/deleteLink.do?linkId=" + currNode.id
			+ "&projectName=" + currNode.projectName, null, 'dataForm', "要删除 " + currNode.name, function(data) {
		/*if (data && data.resultCode != "SUCCESS") {
			commonMessage.message(data.errorMsg);
			return;
		}
		commonMessage.message("操作成功！");*/
		treeObj.removeNode(currNode);
	}, null, null);

	/*if (confirm("确定要删除 " + currNode.name + " 吗？")) {
		$.ajax({
			url : WEB_ROOT + "/sys/menu/deleteLink.do?linkId=" + currNode.id
					+ "&projectName=" + currNode.projectName,
			type : "GET",
			success : function(data) {
				if (data && data.resultCode != "SUCCESS") {
					commonMessage.message(data.errorMsg);
					return;
				}
				commonMessage.message("操作成功！");
				treeObj.removeNode(currNode);
			}
		});
	}*/
}

function loadLinkForProjectName(projectName) {
	$.ajax({
		url : WEB_ROOT + "/sys/menu/loadLinkForProjectName.do?projectName="
				+ (projectName ? projectName : $("#queryProj").val()),
		type : "GET",
		success : function(data) {
			if (data && data.resultCode != "SUCCESS") {
				commonMessage.message(data.errorMsg);
				return;
			}
			// alert(JSON.stringify(data));
			treeObj.destroy();
			treeObj = $.fn.zTree.init($("#menuTree"), setting, data.model);
		}
	});

}
