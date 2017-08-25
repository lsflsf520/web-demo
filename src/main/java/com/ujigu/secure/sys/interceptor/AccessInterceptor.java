package com.ujigu.secure.sys.interceptor;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ujigu.secure.common.bean.GlobalConstant;
import com.ujigu.secure.common.bean.ResultModel;
import com.ujigu.secure.common.utils.ThreadUtil;
import com.ujigu.secure.sys.service.LinkMgrService;
import com.ujigu.secure.web.filter.AbstractInterceptor;
import com.ujigu.secure.web.util.UserLoginUtil;
import com.ujigu.secure.web.util.WebUtils;


public class AccessInterceptor extends AbstractInterceptor{

//	private final static Logger LOG = LoggerFactory.getLogger(AccessInterceptor.class);
	
	@Resource
	private LinkMgrService linkMgrService;
	
	@Override
	protected boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			String requestUri) throws Exception {
		boolean result = linkMgrService.checkUserPriv(UserLoginUtil.getUserId(), GlobalConstant.PROJECT_NAME, request.getServletPath());
		if(!result){
			handleError(request, response, "ACCESS_DENIED", "非授权的访问", request.getContextPath() + "/error/403.do");
		}
		
		return result;
	}
	
	
	private void handleError(HttpServletRequest request, HttpServletResponse response, String operType, String errorMsg, String redirectUrl) throws IOException{
		if(ThreadUtil.isAppReq() || WebUtils.isAjax(request)){
			WebUtils.writeJson(new ResultModel(operType, errorMsg), request, response);
		}else{
			response.sendRedirect(redirectUrl);
		}
	}
	
}
