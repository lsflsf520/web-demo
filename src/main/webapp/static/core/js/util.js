              
var msgTool = {}

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
  
  
	  
});