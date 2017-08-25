package com.ujigu.secure.sys.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.ujigu.secure.common.bean.GlobalConstant;
import com.ujigu.secure.common.bean.GlobalResultCode;
import com.ujigu.secure.common.bean.ResultModel;
import com.ujigu.secure.common.exception.BaseRuntimeException;
import com.ujigu.secure.common.utils.ThreadUtil;
import com.ujigu.secure.sys.entity.Worker;
import com.ujigu.secure.sys.service.impl.LinkService;
import com.ujigu.secure.sys.service.impl.WorkerService;
import com.ujigu.secure.sys.vo.MenuInfo;
import com.ujigu.secure.web.util.LoginSesionUtil;
import com.ujigu.secure.web.util.UserLoginUtil;
import com.ujigu.secure.web.util.WebUtils;

@Controller
@RequestMapping("/sys/port")
public class PassportController {

	private final static Logger LOG = LoggerFactory.getLogger(PassportController.class);

	@Resource
	private WorkerService workerService;
	
	@Resource
	private LinkService linkService;
	
	@RequestMapping("index")
    public ModelAndView index(HttpServletRequest request){
		try {
			ModelAndView mv = new ModelAndView("index");
			if (UserLoginUtil.hasLogon()) {
				List<MenuInfo> menus = linkService.loadMenu("web-admin", UserLoginUtil.getUserId());
				mv.addObject("menus", menus);
				return mv;
			}
		} catch (Exception e) {
			LOG.debug(LOG.isDebugEnabled() ? "session info is not exists." : null);
		}
        
        return new ModelAndView("redirect:loginPage.do");
    }
	
	@RequestMapping("loadMenu")
	@ResponseBody
	public ResultModel loadMenu(HttpServletRequest request){
		if (UserLoginUtil.hasLogon()) {
			List<MenuInfo> menus = linkService.loadMenu(GlobalConstant.PROJECT_NAME, UserLoginUtil.getUserId());
			if(menus != null && !menus.isEmpty()){
				menus.get(0).setSpread(true);
				buildContext(request.getContextPath(), menus);
				return new ResultModel(menus);
			}
			
			return new ResultModel("NO_MENU", "您没有任何对应的菜单，请联系管理员！");
		}
		
		return new ResultModel("NOT_LOGON", "您尚未登录，请先登录！");
	}
	
	private void buildContext(String requestContext, List<MenuInfo> menus){
		for(MenuInfo menu : menus){
			if(StringUtils.isNotBlank(menu.getHref()) && menu.getHref().startsWith("/")){
				menu.setHref(requestContext + menu.getHref());
			}
			if(menu.getChildren() != null && !menu.getChildren().isEmpty()){
				buildContext(requestContext, menu.getChildren());
			}
		}
	}
	
	@RequestMapping("welcome")
    public ModelAndView welcome(HttpServletRequest request) throws ParseException{
        ModelAndView mv = new ModelAndView("sys/welcome");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date = sdf.format(new Date());
		String startDate = date + " 00:00:00";
		String endDate = date + " 23:59:59";
		Date startTime = sdf2.parse(startDate);
		Date endTime = sdf2.parse(endDate);
		mv.addObject("startTime", startTime);
		mv.addObject("endTime", endTime);
		mv.addObject("realName", UserLoginUtil.getRealName());
        mv.addObject("content", "欢迎使用XX网后台管理系统");
        mv.addObject("title", "XX网后台管理");
        return mv;
    }

	@RequestMapping("loginPage")
	public String loginPage(HttpServletRequest request) {

		try {
			if (UserLoginUtil.hasLogon()) {
				return "redirect:index.do";
			}
		} catch (Exception e) {
			LOG.debug(LOG.isDebugEnabled() ? "session info is not exists." : null);
		}
		if (!"true".equals(request.getParameter("noreferer"))) {
			String referer = StringUtils.isNotBlank(request.getParameter("referer")) ? request.getParameter("referer")
					: request.getHeader("referer");
			request.setAttribute("referer", referer);
		}
		request.setAttribute("errorMsg", request.getParameter("errorMsg"));
		LOG.debug(LOG.isDebugEnabled() ? request.getHeader("user-agent") : "");
		return "sys/loginPage";
		// return "user/padLoginPage";
	}

	@RequestMapping("doLogon")
	@ResponseBody
	public ResultModel doLogon(HttpServletRequest request, HttpServletResponse response) {

		String loginName = request.getParameter("loginName");
		String password = request.getParameter("password");

		if (StringUtils.isBlank(loginName) || StringUtils.isBlank(password)) {
//			attr.addAttribute("errorMsg", "用户名和密码均不能为空！");
//			return "redirect:"
//					+ (StringUtils.isBlank(request.getHeader("referer")) ? "index.do" : request
//							.getHeader("referer").split("\\?")[0]);
			return new ResultModel("ILLEGAL_PARAM", "用户名和密码均不能为空！");
		}

		String errorMsg = "登录错误，请重试！";
			Worker user = null;
			try {
				int acId = GlobalConstant.AGENT_COMPANY_ID;
				
				user = workerService.checkPasswd(acId, loginName, password);

				doLogon(request, response, user);

//				return "redirect:index.do";
				return new ResultModel(GlobalResultCode.SUCCESS);
			} catch (BaseRuntimeException be) {
                LOG.warn(be.getMessage());
//				return "redirect:/user/pwd/toresetpage";
                errorMsg = be.getFriendlyMsg();
			}

//		attr.addAttribute("errorMsg", errorMsg);
//		return "redirect:" + request.getHeader("referer").split("\\?")[0];
			return new ResultModel("LOGON_FAIL", errorMsg);
	}

	@RequestMapping("doLogout")
	@ResponseBody
	public ResultModel doLogout(HttpServletRequest request, HttpServletResponse response) {

		UserLoginUtil.removeSession(request, response);

//		try {
//			userRpcService.logout(LoginSesionUtil.getUserId(), ThreadUtil.getToken());
//		} catch (Exception e) {
//			LOG.error(e.getMessage(), e);
//		}

//		String loginPage = "/sys/port/loginPage.do";
		// String referer = request.getHeader("referer");
		// if (StringUtils.isNotBlank(referer)) {
		// if (referer.contains("/web-teacher/") ||
		// referer.contains("/web-teacher")) {
		// loginPage = "/user/login/teacherLoginPage";
		// } else if (referer.contains("/web-ms/") ||
		// referer.contains("/web-ms")) {
		// loginPage = "/user/login/msLoginPage";
		// }
		// }

//		return "redirect:" + loginPage + "?noreferer=true";
		return new ResultModel(true);
	}
	
	/**
	 * 处理登录信息，将登录信息存入缓存，会写cookie以及更新登录记录等
	 * 
	 * @param request
	 * @param response
	 * @param userRpcService
	 * @param user
	 * @return 返回一个token
	 */
	private String doLogon(HttpServletRequest request, HttpServletResponse response, Worker worker) {

//		if (user.isStudent() && !UserInfoUtil.isTYStudent(user.getSignName())) {// TY开头的学生账号为体验账号，登录不做限制
//			LoginSesionUtil.checkLoginState(user.getId());
//		}

		Map<String, Object> sessionMap = parseSessionMap(worker, request);

		String token = UserLoginUtil.storeSession(sessionMap, response, false);

		ThreadUtil.clear();
		ThreadUtil.setToken(token);
		ThreadUtil.put(ThreadUtil.ACID_NAME, worker.getAcId());
		
		LOG.info("token:" + token + ",userId:" + worker.getId() + ",loginName:" + worker.getLoginName() + ",clientIP:" + WebUtils.getClientIp(request));

//		try {
//			userRpcService.updateLoginInfo(user.getId(), user.getType(), token, WebUtils.getClientIp(request));
//		} catch (Exception ex) {
//			LOG.warn("error occured in update user login info,msg:" + ex.getMessage());
//		}

		return token;
	}

	private Map<String, Object> parseSessionMap(Worker worker, HttpServletRequest request) {

		Map<String, Object> sessionMap = new HashMap<String, Object>();
		sessionMap.put(ThreadUtil.USER_ID, worker.getId());
		sessionMap.put(ThreadUtil.EMAIL, worker.getEmail());
		sessionMap.put(ThreadUtil.PHONE, worker.getPhone());
		sessionMap.put(ThreadUtil.REAL_NAME, worker.getWorkerName());
		sessionMap.put(ThreadUtil.LOGIN_NAME, worker.getLoginName());
		sessionMap.put(ThreadUtil.ROLE_NAME, worker.getRoleId());
		sessionMap.put(ThreadUtil.ACID_NAME, worker.getAcId());
		sessionMap.put(ThreadUtil.ADID_NAME, worker.getDepartId());

		sessionMap.put(ThreadUtil.EQUIP_TYPE, WebUtils.getEquipType(request));
		sessionMap.put(ThreadUtil.LOGIN_IP, WebUtils.getClientIp(request));


		return sessionMap;
	}

}
