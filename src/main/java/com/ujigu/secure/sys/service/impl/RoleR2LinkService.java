package com.ujigu.secure.sys.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.ujigu.secure.db.dao.impl.BaseDaoImpl;
import com.ujigu.secure.db.service.impl.BaseServiceImpl;
import com.ujigu.secure.sys.dao.impl.RoleR2LinkDaoImpl;
import com.ujigu.secure.sys.entity.RoleR2Link;
import com.ujigu.secure.web.util.UserLoginUtil;


@Service
public class RoleR2LinkService extends BaseServiceImpl<Integer, RoleR2Link> {
	@Resource
	private RoleR2LinkDaoImpl roleR2LinkDaoImpl;

	@Override
	protected BaseDaoImpl<Integer, RoleR2Link> getBaseDaoImpl() {
		return roleR2LinkDaoImpl;
	}

	public boolean deleteByRrl(List<Integer> linkIds, List<Integer> roleIds) {
		LinkService.delLinkCache(UserLoginUtil.getAcId());
		if (linkIds == null || linkIds.isEmpty() || roleIds == null
				|| roleIds.isEmpty()) {
			return false;
		}
		return roleR2LinkDaoImpl.deleteByRrl(linkIds, roleIds);
	}

	public boolean deleteByRole(Integer roleId) {
		if (roleId == null) {
			return false;
		}
		LinkService.delLinkCache(UserLoginUtil.getAcId());
		return roleR2LinkDaoImpl.deleteByRoleId(roleId);
	}

	public boolean deleteByProjectName(String projectName, Integer roleId) {
		if (projectName == null) {
			return false;
		}
		LinkService.delLinkCache(UserLoginUtil.getAcId());

		return roleR2LinkDaoImpl.deleteByProejctName(projectName, roleId);
	}
	// public boolean deleteByRoleId(int roleId, List<Integer> linkIds) {
	// if (DefaultCacheNS.SYS_PRIV.needRemoveAllCacheAfterModify()) {
	// EhCacheTool.removeAll(DefaultCacheNS.SYS_PRIV);
	// }
	// return roleR2LinkDaoImpl.deleteByRoleId(roleId, linkIds);
	// }

}
