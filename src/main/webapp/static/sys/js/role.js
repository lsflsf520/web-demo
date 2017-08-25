 var roleAutoId = -1;
 function getRowDataModel(rowId){
		roleAutoId += -1; 
		return {
			      name : "",
			      id : roleAutoId
			    };
	}
 
function changeTheMenuCheck(ids){
	 $('#btnSaveRoleMenu').show();
	 cleanTheChooseCheck();
	 //超级管理员和匿名角色权限信息不允许修改
	   if(ids==1||ids==2)
		{
		   $('#btnSaveRoleMenu').hide();
		}
	   var tableSel = "#role-grid-table";
	   var ret = $(tableSel).jqGrid('getRowData', ids);
	   
	   var retProjectName=ret.projectName;
	   
	  //老师只展示teacher
	     if(retProjectName==null ||retProjectName =="")
	    	 {
	    	   $("#projectDiv").hide();
	    	   $("#menuTree").hide();
	    	   return;
	    	 }
	     else
	    	 {
	    	 $("#projectDiv").show();
	    	 $("#menuTree").show();
	    	 }
	   
	   $("#queryProj option").each(function (){  
          if($(this).val() == retProjectName){
        	  jQuery("#queryProj option[value="+retProjectName+"]").attr("selected",true);
        	  jQuery("#queryProj option[value="+retProjectName+"]").show();
             }
          else
        	  {
       	       jQuery("#queryProj option[value="+$(this).val()+"]").hide(); 
        	  }             
            });
		 
	   var projectName=$("#queryProj").val();
	  
	   var treeObj = $.fn.zTree.getZTreeObj("menuTree");
	 	  $.ajax({
	 		// async : false,
	 		  url : WEB_ROOT + "/sys/menu/loadAllLinkForProjectName.do?projectName=" + (projectName ? projectName : $("#queryProj").val()),
	 		  type:"GET",
	 		  success:function(data){
	 			  if(data && data.resultCode != "SUCCESS"){
	 				    commonMessage.message(data.errorMsg);
	 					return;
	 		      }
	 			 setNoParentName(data);
	 			 treeObj.destroy();
	 			  treeObj = $.fn.zTree.init($("#menuTree"), setting, data).expandAll(true);
	 			 $.ajax({
	 				  url : WEB_ROOT + "/sys/role/findMenuByRole.do?roleId="+ids,
	 				  type:"GET",
	 				  success:function(data){
	 					  var treeObj = $.fn.zTree.getZTreeObj("menuTree");
	 					  var nodes = treeObj.transformToArray(treeObj.getNodes()); 
	 					  treeObj.checkAllNodes(false);
	 					   for(var  i=0;i<data.length;i++)
	 				         { 
	 				            for (var j = 0; j < nodes.length; j++) { 
	 				    	    if(data[i]==nodes[j].id)
	 				    		 { 
	 				    	     
	 				    	     //nodes[j].checkedOld=nodes[j].checked;
	 				    	    	 nodes[j].checked=true; 
	 				    	     // nodes[j].checkedOld=false;
	 				    	     
	 				    	     
	 				    	      treeObj.updateNode(nodes[j]);      
	 				    		 } 
	 					  } 
	 				}	
	 					     
	 				  }
	 			  });
	 		  
	 		  }
	 	  });
	 			  
	
}

function cleanTheChooseCheck(){
	$('#clickMenuIds').val("");
}
//将null节点名称设置为功能节点
function setNoParentName(zNodes){
	$.each(zNodes, function (index, info) {
		
		if(zNodes[index].name==null)
			{
			zNodes[index].name="全局功能链接";
			}		 
	 });	
		return zNodes
}


	
/*function loadAllLinkForProjectName(projectName){
 	 var treeObj = $.fn.zTree.getZTreeObj("menuTree");
 	  $.ajax({
 		  url : WEB_ROOT + "/sys/menu/loadAllLinkForProjectName.do?projectName=" + (projectName ? projectName : $("#queryProj").val()),
 		  type:"GET",
 		  success:function(data){
 			  if(data && data.type && data.type != "success"){
 					Global.notify(data.message);
 					return;
 		      }
 			 setNoParentName(data);
 			 
 			
 			  treeObj.destroy();
 			  treeObj = $.fn.zTree.init($("#menuTree"), setting, data).expandAll(true);
 			
 			 
 			  //获取选中行
 			  var tableSel = "#role-grid-table";
 	      	  var ids = $(tableSel).jqGrid('getGridParam','selrow');
 	      	  if(ids==null)
 	      	  {
 	      		 return;
 	      	 }
 	      	changeTheMenuCheck(ids);
 			 
 		  }
 	  });
 	  
 }*/
 function saveRoleMenuRelation(){
   var choseRoleId = $("#mRoleId").val();
   var treeObj = $.fn.zTree.getZTreeObj("menuTree");
   var checknodes = treeObj.getCheckedNodes(true);
   var choseMenuIds="";
   var projectName="web-admin";
	
	 $.each(checknodes, function (index, info) {
		 if(choseMenuIds!="")
			 {
			 choseMenuIds+=",";
			 }
		 choseMenuIds+=checknodes[index].id;
	 });
	 
	 commonMessage.alertComfirm("确定要保存吗？", "确定", "取消", null, function(){
		 $.ajax({
			  url : WEB_ROOT + "/sys/role/saveRoleOfMenus.do",
			  data : {
				choseRoleId : choseRoleId,
				choseMenuIds : choseMenuIds,
				projectName: projectName
			  },
		      type : "POST",
			  success:function(data){
				  if(data && data.resultCode != "SUCCESS"){
					  commonMessage.message(data.errorMsg);
					  return;
			      }
				  
				  commonMessage.message("操作成功");
//				  commonMessage.closeWindow();
				  commonMessage.switchTab("角色管理");
			  }
		  }); 
	 });
	 /*if(confirm("确定要保存吗？")){
		 
	 }*/
   	 
 }
 

