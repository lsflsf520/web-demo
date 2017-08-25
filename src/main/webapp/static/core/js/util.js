              
var msgTool = {}

var commonMessage = {}

layui.use('layer', function(){
  var layer = layui.layer;
	 
  msgTool.info = function(msg, options){
	  layer.msg(msg);
  }
  
  msgTool.warn = function(msg, options){
	  layer.msg(msg, {icon: 5}); 
  }
  
  msgTool.success = function(msg, options){
	  layer.msg(msg, {icon: 1});  
  }
  
  msgTool.error = function(msg, options){
	  layer.alert(msg, {icon: 2});
  }
  
  msgTool.confirm = function(msg, callback, yesName, noName){
	  layer.open({
		  content: msg
		  ,btn: [yesName ? yesName : '确定', noName ? noName : '取消']
		  ,yes: callback
	  });
  }
  
  msgTool.close = function(){
	  layer.closeAll();
  }
  
  commonMessage.openWindow = function(title,html,size,preAction,data){
		if(preAction!=null)
			preAction(data);
		layer.open({
			 type: 1,
		     offset: 't',
		     title: title,
		     skin: 'layui-layer-rim', //加上边框
		     area: size, //宽高
		     content: $('#'+html) //捕获的元素，注意：最好该指定的元素要存放在body最外层，否则可能被其它的相对元素所影响
		});
	}
  
  commonMessage.closeWindow = function(){
		parent.layer.closeAll();
		layer.closeAll();
	}
  
	  
});