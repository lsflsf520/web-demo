package com.ujigu.secure.sys.controller;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.ujigu.secure.common.bean.GlobalConstant;
import com.ujigu.secure.sys.service.impl.LinkService;
import com.ujigu.secure.sys.vo.MenuInfo;
import com.ujigu.secure.web.util.UserLoginUtil;

@Controller
public class IndexController {
	
	@Resource
	private LinkService linkService;
	
	/**
	 * 首页
	 * @param request 
	 * @throws UnsupportedEncodingException 
	 */
	@RequestMapping("/index")
	public ModelAndView index(HttpServletRequest request) throws UnsupportedEncodingException{
		/*ModelAndView mv = new ModelAndView("index");
		if (UserLoginUtil.hasLogon()) {
			List<MenuInfo> menus = linkService.loadMenu(GlobalConstant.PROJECT_NAME, UserLoginUtil.getUserId());
			if(menus != null && !menus.isEmpty()){
				menus.get(0).setSpread(true);
				buildContext(request.getContextPath(), menus);
				
				mv.addObject("menus", menus);
			}
		}
		
		return mv; */
		
		return new ModelAndView("redirect:/sys/port/index.do");
	}
	
	
}
