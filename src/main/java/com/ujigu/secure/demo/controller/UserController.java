package com.ujigu.secure.demo.controller;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.ujigu.secure.common.bean.ResultModel;
import com.ujigu.secure.db.bean.PageData;
import com.ujigu.secure.demo.entity.User;
import com.ujigu.secure.demo.service.UserService;

@Controller
@RequestMapping("/demo/user")
public class UserController {

	@Resource
	private UserService userService;
	
	@RequestMapping("list")
	public ModelAndView list(User queryData){
		PageData<User> dataPage = userService.findByPage(queryData);
		
		ModelAndView mav = new ModelAndView("demo/user_list");
		mav.addObject("dataPage", dataPage);
		mav.addObject("queryData", queryData);
		
		return mav;
	}
	
	@RequestMapping("toedit")
	public ModelAndView toedit(Integer pk){
		ModelAndView mav = new ModelAndView("demo/user_edit");
		if(pk != null){
			User user = userService.findById(pk);
			mav.addObject("dbData", user);
		}
		
		return mav;
	}
	
	@RequestMapping("save")
	@ResponseBody
	public ResultModel save(User formData){
		Integer pk = userService.doSave(formData);
		
		return new ResultModel(pk != null);
	}
	
	@RequestMapping("delete")
	public ModelAndView delete(Integer... pks){
		userService.softDel(pks);
		
		return new ModelAndView("redirect:list.do");
	}
	
}
