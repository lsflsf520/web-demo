package com.ujigu.secure.sys.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Repository;

import com.ujigu.secure.db.dao.BaseDao;
import com.ujigu.secure.db.dao.impl.BaseDaoImpl;
import com.ujigu.secure.sys.dao.RoleR2LinkDao;
import com.ujigu.secure.sys.entity.RoleR2Link;
import com.ujigu.secure.web.util.UserLoginUtil;


@Repository
public class RoleR2LinkDaoImpl extends BaseDaoImpl<Integer, RoleR2Link> {
	@Resource
	private RoleR2LinkDao roleR2LinkDao;

	@Override
	protected BaseDao<Integer, RoleR2Link> getProxyBaseDao() {
		return roleR2LinkDao;
	}

	public boolean deleteByRrl(List<Integer> linkIds, List<Integer> roleIds) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("linkIds", linkIds);
		paramMap.put("roleIds", roleIds);
		paramMap.put("acId", UserLoginUtil.getAcId());
		int effectRows = this.getSqlSessionTemplate().delete(
				getMapperNamespace() + ".deleteByRrl", paramMap);

		return effectRows >= 0;
	}

	public boolean deleteByRoleId(Integer roleId) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("roleId", roleId);
		paramMap.put("acId", UserLoginUtil.getAcId());
		int effectRows = this.getSqlSessionTemplate().delete(
				getMapperNamespace() + ".deleteByRoleId", paramMap);

		return effectRows >= 0;
	}

	public boolean deleteByProejctName(String projectName, Integer roleId) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("projectName", projectName);
		paramMap.put("roleId", roleId);
		paramMap.put("acId", UserLoginUtil.getAcId());
		int effectRows = this.getSqlSessionTemplate().delete(
				getMapperNamespace() + ".deleteByProjectName", paramMap);

		return effectRows >= 0;
	}

	// public boolean deleteByRoleId(int roleId, List<Integer> linkIds){
	// Map<String, Object> paramMap = new HashMap<String, Object>();
	// paramMap.put("roleId", roleId);
	// paramMap.put("linkIds", linkIds);
	// int effectRows = this.getSqlSessionTemplate().delete(getMapperNamespace()
	// + ".deleteByRoleId", paramMap);
	//
	// return effectRows >= 0;
	// }

}