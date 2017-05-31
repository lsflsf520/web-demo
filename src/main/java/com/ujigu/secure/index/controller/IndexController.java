package com.ujigu.secure.index.controller;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IndexController {
	
	/**
	 * 首页
	 * @param request 
	 * @throws UnsupportedEncodingException 
	 */
	@RequestMapping("/index")
	public ModelAndView index(HttpServletRequest request) throws UnsupportedEncodingException{
		ModelAndView mv = new ModelAndView("index");
		/*try {
			Map<String, Object> map = siteService.getSite();
			mv.addObject("title", map.get("title"));
			mv.addObject("descp", map.get("description"));
			mv.addObject("kword", map.get("keywords"));
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}*/
		return mv; 
	}
	
}
