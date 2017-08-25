package com.ujigu.secure.sys.controller;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.ujigu.secure.common.bean.GlobalConstant;
import com.ujigu.secure.common.bean.GlobalResultCode;
import com.ujigu.secure.common.bean.ResultModel;
import com.ujigu.secure.common.exception.BaseRuntimeException;
import com.ujigu.secure.sys.entity.Role;
import com.ujigu.secure.sys.service.LinkMgrService;
import com.ujigu.secure.sys.service.RoleMgrService;
import com.ujigu.secure.sys.service.impl.LinkService;
import com.ujigu.secure.sys.service.impl.RoleService;
import com.ujigu.secure.web.util.UserLoginUtil;
import com.ujigu.secure.web.util.WebUtils;


@Controller
@RequestMapping("/sys/role")
public class RoleController {

	@Resource
	private RoleMgrService roleMgrService;

	@Resource
	private LinkMgrService linkMgrService;
	
	/*@RequestMapping("list")
	public String toIndex(HttpServletRequest request){
		String[] projectNames = new String[]{GlobalConstant.PROJECT_NAME};
		if(projectNames == null || projectNames.length <= 0){
			projectNames = new String[]{"web-ms", "web-teacher"};
		}
		
		String projectNameStr = "";
		for(String projectName : projectNames){
			projectNameStr += projectName + ":" + projectName + ";";
		}
		projectNameStr = projectNameStr.substring(0, projectNameStr.length() - 1);
		request.setAttribute("projectNames", projectNames);
		request.setAttribute("projectNameStr", projectNameStr);
		
		return "sys/role";
	}*/

	@RequestMapping("findAvailRoles")
	@ResponseBody
	public ResultModel findAvailRoles(HttpServletRequest request,
			HttpServletResponse response) {
		ResultModel model = ResultModel.buildMapResultModel();
		List<Role> availRoles = roleMgrService.findAvailRoles();
//		Map<String, Object> resultMap = new HashMap<String, Object>();
		String linkIdStr = request.getParameter("linkId");
		String userIdStr = request.getParameter("userId");
		if (StringUtils.isNotBlank(linkIdStr)) {
			List<Integer> roleIds = linkMgrService
					.findRoleIdByLinkId(Integer.valueOf(linkIdStr));
//			resultMap.put("roleIds", roleIds);
			model.put("roleIds", roleIds);
		} else if (StringUtils.isNotBlank(userIdStr)) {
			List<Integer> roleIds = roleMgrService.findRoleIdByUserId(Integer
					.valueOf(userIdStr));
			model.put("roleIds", roleIds);
		}

//		resultMap.put("availRoles", availRoles);
		model.put("availRoles", availRoles);
//		WebUtils.writeJson(resultMap, request, response);
		return model;
	}

	@RequestMapping("list")
	public String list(HttpServletRequest request,
			HttpServletResponse response, String keyword) {
		int maxRows = StringUtils.isBlank(request.getParameter("pageNum")) ? 20
				: Integer.valueOf(request.getParameter("pageNum"));

		int currPage = StringUtils.isBlank(request.getParameter("page")) ? 1
				: Integer.valueOf(request.getParameter("page"));

		Page<Role> pager = roleMgrService.searchRole(keyword, currPage, maxRows);

		try {
			pager.getContent();
		} catch (NullPointerException e) {
			pager = new PageImpl<Role>(new ArrayList<Role>());
		}
		
		request.setAttribute("rolePage", pager);
		request.setAttribute("projectName", GlobalConstant.PROJECT_NAME);
		request.setAttribute("keyword", keyword);
		RoleService roleService = (RoleService)roleMgrService;
		request.setAttribute("superAdminRoleId", roleService.getMySuperAdminRoleId());
		request.setAttribute("departOperRoleId", roleService.getMyDepartOperRoleId());
//		request.setAttribute("projectNameStr", StringUtils.join(projectNames, ","));

//		WebUtils.writeJson(pager, request, response);
		return "sys/role";
	}

	@RequestMapping("saveRoleOfMenus")
	@ResponseBody
	public ResultModel saveRoleOfMenus(HttpServletRequest request,
			HttpServletResponse response) {

		String roleId = request.getParameter("choseRoleId");
		String menuids = request.getParameter("choseMenuIds");
		String projectName = request.getParameter("projectName");
		List<Integer> menuIdsList = new ArrayList<Integer>();

		if (StringUtils.isNotBlank(menuids)) {
			String[] parts = menuids.split(",");
			for (String part : parts) {
				menuIdsList.add(Integer.valueOf(part));
			}
		}

		try{
			roleMgrService.saveRole2Links(Integer.valueOf(roleId), menuIdsList,
					projectName);
		}catch(BaseRuntimeException e){
			return new ResultModel("ERR", e.getFriendlyMsg());
		}
		
		return new ResultModel(true);
	}

	@RequestMapping("saveUserRelation")
	public void saveUserRelation(HttpServletRequest request,
			HttpServletResponse response, int userId) {
		String roleIdStr = request.getParameter("roleIds");
		List<Integer> roleList = new ArrayList<Integer>();
		if (StringUtils.isNotBlank(roleIdStr)) {
			String[] parts = roleIdStr.split(",");
			for (String part : parts) {
				roleList.add(Integer.valueOf(part));
			}
		}

		boolean result = roleMgrService.saveUser2Roles(userId, roleList);
		if (result) {
			WebUtils.writeJson(new ResultModel(GlobalResultCode.SUCCESS),
					request, response);
			return;
		}

		WebUtils.writeJson(new ResultModel(GlobalResultCode.SUCCESS), request,
				response);
	}
	
	// @RequestMapping("findUserRole")
	// public void findUserRole(HttpServletRequest request,
	// HttpServletResponse response){
	// List<Role> userRoleList = roleMgrRpcService.findUserRole();
	//
	// WebUtils.writeJson(userRoleList, request, response);
	// }

	// @RequestMapping("findUserRole4Jqgrid")
	// public void findUserRole4Jqgrid(HttpServletRequest request,
	// HttpServletResponse response){
	// List<Role> userRoleList = roleMgrRpcService.findUserRole();
	// String roleStr = "";
	// if(userRoleList != null){
	// for(Role role : userRoleList){
	// roleStr += role.getId() + ":" + role.getName() + ";";
	// }
	// }
	// WebUtils.writeJson(roleStr, request, response);
	// }

	@RequestMapping("toedit")
	public ModelAndView toedit(Integer pk){
		ModelAndView mav = new ModelAndView("sys/role_edit");
		if(pk != null){
			Role role = roleMgrService.findById(pk);
			mav.addObject("role", role);
		}
		
		return mav;
	}
	
	@RequestMapping("toprivedit")
	public ModelAndView toprivedit(HttpServletRequest request, Integer pk){
		ModelAndView mav = new ModelAndView("sys/role_priv_edit");
		Role role = roleMgrService.findById(pk);
//		String projectName = request.getParameter("projectName");
//		List<MenuInfo> links = linkMgrService.findAllLinks(StringUtils
//				.isNotBlank(projectName) ? projectName : GlobalConstant.PROJECT_NAME);
//		mav.addObject("links", links);
		mav.addObject("projectName", GlobalConstant.PROJECT_NAME);
		mav.addObject("roleName", role.getName());
		mav.addObject("roleId", pk);
		
		return mav;
	}
	
	@RequestMapping("refresh")
	public ModelAndView refresh(Integer pk){
		ModelAndView mav = new ModelAndView("redirect:toprivedit.do?pk=" + pk);
		
		LinkService.delLinkCache(UserLoginUtil.getAcId());
		
		return mav;
	}
	
	@RequestMapping("save")
	@ResponseBody
	public ResultModel save(HttpServletRequest request, HttpServletResponse response,
			Role role) {
		try {
			if (role.getId() != null && role.getId() < 0) {
				role.setId(null);
			}
			roleMgrService.save(role);
		} catch (BaseRuntimeException e) {
			return new ResultModel("CODE", e.getFriendlyMsg());
		}

		return new ResultModel(true);
	}

	@RequestMapping("delete")
	@ResponseBody
	public ResultModel delete(Integer... pks) {
		try {
			for(int pk : pks){
				roleMgrService.invalid(pk);
			}
		} catch (BaseRuntimeException e) {
			return new ResultModel("CODE", e.getFriendlyMsg());
		}

		return new ResultModel(true);
	}

}
