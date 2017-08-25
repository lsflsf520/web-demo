package com.ujigu.secure.sys.controller;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ujigu.secure.common.bean.GlobalConstant;
import com.ujigu.secure.common.bean.GlobalResultCode;
import com.ujigu.secure.common.bean.ResultModel;
import com.ujigu.secure.common.exception.BaseRuntimeException;
import com.ujigu.secure.common.utils.BaseConfig;
import com.ujigu.secure.sys.entity.Link;
import com.ujigu.secure.sys.service.LinkMgrService;
import com.ujigu.secure.sys.vo.MenuInfo;


@Controller
@RequestMapping("/sys/menu")
public class MenuController {

	@Resource
	private LinkMgrService linkMgrService;

	@RequestMapping("/list")
	public String index(HttpServletRequest request) {
		String projectName = request.getParameter("projectName");
		List<Link> links = linkMgrService.findRootLinks(StringUtils
				.isNotBlank(projectName) ? projectName : GlobalConstant.PROJECT_NAME);
		request.setAttribute("links", links);
		
		String[] projectNames = BaseConfig.getValueArr("sys.project.names");
		if(projectNames == null || projectNames.length <= 0){
			projectNames = new String[]{"web-admin", "web-company"};
		}
		
//		String[] projectNames = new String[]{GlobalConstant.PROJECT_NAME};
		
		request.setAttribute("projectNames", projectNames);
		
		return "sys/menu";
	}
	
	@RequestMapping("/loadLinkForProjectName")
	@ResponseBody
	public ResultModel loadLinkForProjectName(String projectName) {
		List<Link> links = linkMgrService.findRootLinks(StringUtils
				.isNotBlank(projectName) ? projectName : "web-admin");

		return new ResultModel(links);
	}

	// 刘帅
	@RequestMapping("/loadAllLinkForProjectName")
	@ResponseBody
	public ResultModel loadAllLinkForProjectName(String projectName) {
		List<MenuInfo> links = linkMgrService.findAllLinks(StringUtils
				.isNotEmpty(projectName) ? projectName : "web-admin");
		// request.setAttribute("links", links);
		return new ResultModel(links);
	}

	@RequestMapping("/loadLinkForAll")
	@ResponseBody
	public ResultModel loadLinkForAll(String projectName) {
		List<Link> links = linkMgrService.findRootLinks(StringUtils
				.isNotBlank(projectName) ? projectName : "web-admin");

		return new ResultModel(links);
	}
	
	@RequestMapping("loadLinkByRole")
	@ResponseBody
	public ResultModel loadLinkByRole(int roleId) {
		List<Integer> result = linkMgrService.findLinkByRoleId(roleId);
		
		return new ResultModel(result);
	}

	@RequestMapping("/saveRoleRelation")
	@ResponseBody
	public ResultModel saveRoleRelation(HttpServletRequest request,
			HttpServletResponse response, int linkId) {
		String roleIdStr = request.getParameter("roleIds");
		List<Integer> roleIdList = new ArrayList<Integer>();
		if (StringUtils.isNotBlank(roleIdStr)) {
			String[] parts = roleIdStr.split(",");
			for (String part : parts) {
				roleIdList.add(Integer.valueOf(part));
			}
		}
		boolean result = linkMgrService.saveLink2Roles(linkId, roleIdList);
		if (result) {
			return new ResultModel(GlobalResultCode.SUCCESS);
		}

		return new ResultModel(GlobalResultCode.SUCCESS);
	}

	@RequestMapping("/saveLink")
	@ResponseBody
	public ResultModel saveLink(HttpServletRequest request,
			HttpServletResponse response, Link link) {
		try {
			int linkId = linkMgrService.save(link);

			link = linkMgrService.findById(linkId);
		} catch (BaseRuntimeException e) {
			return new ResultModel("CODE", e.getFriendlyMsg());
		}

		return new ResultModel(link);
	}

	@RequestMapping("/hasSameLinkURL")
	@ResponseBody
	public ResultModel hasSameLinkURL(HttpServletRequest request,
			HttpServletResponse response, String link) {
		int count = 0;
		try {
			count = linkMgrService.findCountByLinkURL(link);

		} catch (BaseRuntimeException e) {
			return new ResultModel("CODE", e.getFriendlyMsg());
		}

		return new ResultModel(count);
	}

	@RequestMapping("/deleteLink")
	@ResponseBody
	public ResultModel deleteLink(HttpServletRequest request,
			HttpServletResponse response, int linkId, String projectName) {
		if (linkId <= 0) {
			return new ResultModel("CODE", "参数错误");
		}
		try {
			linkMgrService.invalid(linkId, projectName);
		} catch (BaseRuntimeException e) {
			return new ResultModel("CODE", e.getFriendlyMsg());
		}

		return new ResultModel(GlobalResultCode.SUCCESS);
	}

	@RequestMapping("/findByParentId")
	@ResponseBody
	public List<Link> findByParentId(HttpServletRequest request,
			HttpServletResponse response, Integer parentId) {
		if (parentId == null) {
//			WebUtils.writeJson(new ArrayList<Link>(), request, response);
			return new ArrayList<Link>();
		}
		List<Link> links = linkMgrService.findLinkByParentId(parentId);

		return links;
//		WebUtils.writeJson(links, request, response);
	}

}
