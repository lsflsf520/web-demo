package com.ujigu.secure.sys.service.impl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.ujigu.secure.common.bean.CommonStatus;
import com.ujigu.secure.common.bean.PageInfo.OrderDirection;
import com.ujigu.secure.common.utils.ArrayUtils;
import com.ujigu.secure.common.utils.BaseConfig;
import com.ujigu.secure.db.dao.impl.BaseDaoImpl;
import com.ujigu.secure.db.service.impl.BaseServiceImpl;
import com.ujigu.secure.sys.dao.impl.RoleDaoImpl;
import com.ujigu.secure.sys.entity.Role;
import com.ujigu.secure.sys.entity.RoleR2Link;
import com.ujigu.secure.sys.entity.Worker;
import com.ujigu.secure.sys.service.RoleMgrService;
import com.ujigu.secure.web.common.service.DataDictService;
import com.ujigu.secure.web.util.UserLoginUtil;

@Service
public class RoleService extends BaseServiceImpl<Integer, Role> implements
		RoleMgrService {

	@Resource
	private RoleDaoImpl roleDaoImpl;

	@Resource
	private RoleR2LinkService roleR2LinkServiceImpl;

//	@Resource
//	private UserR2RoleService userR2RoleServiceImpl;

	@Resource
	private WorkerService workerService;
	
	@Resource
    private DataDictService dataDictService;
	
	private final static List<Integer> SPECIAL_ROLE_IDS = new ArrayList<>(); //特殊角色，拥有这个角色的用户，有所有菜单的权限
	private final static List<Integer> DEPART_OPER_ROLE_IDS = new ArrayList<>(); //机构操作员角色
    static{
    	int[] roleIds = BaseConfig.getIntArr("role.allprivs.ids");
    	if(roleIds != null){
    		for(int roleId : roleIds){
    			SPECIAL_ROLE_IDS.add(roleId);
    		}
    	}
    	roleIds = BaseConfig.getIntArr("role.depart.oper.ids");
    	if(roleIds != null){
    		for(int roleId : roleIds){
    			DEPART_OPER_ROLE_IDS.add(roleId);
    		}
    	}
    }
    
    public static List<Integer> getSuperAdminRoles(){
    	return Collections.unmodifiableList(SPECIAL_ROLE_IDS);
    }
    
    public static List<Integer> getDepartOperRoleIds(){
    	return Collections.unmodifiableList(DEPART_OPER_ROLE_IDS);
    }
    
    public Integer getMySuperAdminRoleId(){
    	List<Role> availRoles = findAvailRoles();
    	for(Role role : availRoles){
    		if(SPECIAL_ROLE_IDS.contains(role.getId())){
    			return role.getId();
    		}
    	}
    	return null;
    }
    
    public Integer getMyDepartOperRoleId(){
    	List<Role> availRoles = findAvailRoles();
    	for(Role role : availRoles){
    		if(DEPART_OPER_ROLE_IDS.contains(role.getId())){
    			return role.getId();
    		}
    	}
    	return null;
    }

	@Override
	protected BaseDaoImpl<Integer, Role> getBaseDaoImpl() {

		return roleDaoImpl;
	}

	@Override
	public boolean save(Role role) {

		if (role.getId() == null) {
			role.setDbState(CommonStatus.NORMAL);
			role.setLastUptime(new Date());
			role.setCreateTime(new Date());
			role.setAcId(UserLoginUtil.getAcId());
			this.insert(role);
			dataDictService.refreshData("role_id_name");
			return true;
		}

		boolean result = this.update(role);
		dataDictService.refreshData("role_id_name");
		
		return result;
	}

	@Override
	public boolean invalid(int id) {

		/*UserR2Role urr = new UserR2Role();
		urr.setRoleId(id);
		List<UserR2Role> urrList = userR2RoleServiceImpl.findByEntity(urr);
		if (urrList != null && !urrList.isEmpty()) {
			throw new BaseRuntimeException("NOT_SUPPORT",
					"该角色已经作用在某些用户上，不能被删除！");
		}*/
		Role role = new Role();
		role.setId(id);
		role.setDbState(CommonStatus.DELETED);
		boolean result = this.update(role);
		dataDictService.refreshData("role_id_name");
		
		return result;
	}

	@Override
	public List<Role> findByUserId(int userId) {

		return roleDaoImpl.findByUserId(userId);
	}

	@Override
	public List<Integer> findRoleIdByUserId(int userId) {

		/*UserR2Role urr = new UserR2Role();
		urr.setUserId(userId);
		List<UserR2Role> urrList = userR2RoleServiceImpl.findByEntity(urr);*/
		List<Integer> roleIds = new ArrayList<Integer>();
		/*if (urrList != null && !urrList.isEmpty()) {
			for (UserR2Role currUrr : urrList) {
				roleIds.add(currUrr.getRoleId());
			}
		}*/
		return roleIds;
	}

	@Override
	public List<Role> findAvailRoles() {

		Role role = new Role();
		role.setDbState(CommonStatus.NORMAL);
		role.setAcId(UserLoginUtil.getAcId());
		return this.findByEntity(role, "create_time", OrderDirection.desc);
	}

	@Override
	public boolean saveRole2Links(int roleId, List<Integer> savelinkIds,
			String projectName) {

		Role role = this.findById(roleId);
		if (null == role) {
			return false;
		}

		roleR2LinkServiceImpl.deleteByRole(roleId);

		if (ArrayUtils.isEmpty(savelinkIds)) {
			return true;
		}

		RoleR2Link role2Link = new RoleR2Link();
		role2Link.setRoleId(roleId);
		// 查询当前关联Link
		List<RoleR2Link> currtRole2Link = roleR2LinkServiceImpl
				.findByEntity(role2Link);

		List<RoleR2Link> saveRole2Link = new ArrayList<RoleR2Link>();
		// saveRole2Link.addAll(currtRole2Link);

		for (RoleR2Link roleR2Link : currtRole2Link) {
			// 筛选出其他projectname的权限无需移除
			if (savelinkIds.contains(roleR2Link.getLinkId())) {
				savelinkIds.remove(roleR2Link.getLinkId());
			}
		}
		Integer acId = UserLoginUtil.getAcId();
		// 添加勾选的权限
		for (int lkId : savelinkIds) {
			RoleR2Link roleR2Link = new RoleR2Link();
			roleR2Link.setRoleId(roleId);
			roleR2Link.setLinkId(lkId);
			roleR2Link.setAcId(acId);
			saveRole2Link.add(roleR2Link);
		}

		if (ArrayUtils.isEmpty(saveRole2Link)) {
			return true;
		}

		roleR2LinkServiceImpl.insertBatch(saveRole2Link);

		return true;
	}

	/**
	 * 
	 * @return 获取可以用于设置用户类型的角色
	 */
	public List<Role> findUserRole() {

		return roleDaoImpl.findUserRole();
	}

	@Override
	public boolean saveUser2Roles(int userId, List<Integer> roleIds) {

		/*userR2RoleServiceImpl.deleteByUserId(userId);

		if (roleIds != null && !roleIds.isEmpty()) {
			List<UserR2Role> urrList = new ArrayList<UserR2Role>();
			for (Integer roleId : roleIds) {
				UserR2Role urr = new UserR2Role();
				urr.setRoleId(roleId);
				urr.setUserId(userId);

				urrList.add(urr);
			}
			userR2RoleServiceImpl.insertBatch(urrList);
		}*/

		return true;
	}

	@Override
	public Page<Role> searchRole(String roleName, int currPage, int maxRows) {

		Role role = new Role();
		role.setDbState(CommonStatus.NORMAL);
		role.setAcId(UserLoginUtil.getAcId());
		if(StringUtils.isNotBlank(roleName)){
			role.setName(roleName);
		}
		return this.findByPage(role, currPage, maxRows, "name", OrderDirection.desc);
	}

	@Override
	public List<Integer> findUsersByRoles(Integer roleId) {

//		return userR2RoleServiceImpl.loadByRoleId(roleId);
		
		return new ArrayList<>();
	}

	@Override
	public List<Worker> findUserListByRole(int roleId) {

		List<Integer> userIdList = findUsersByRoles(roleId);
		if (userIdList != null && !userIdList.isEmpty()) {
			return workerService.findWorkers(userIdList.toArray(new Integer[0]));
		}

		return new ArrayList<Worker>();
	}

	@Override
	public boolean isInRole(int userId, Integer... roleIds) {

		if (roleIds == null || roleIds.length <= 0) {
			return false;
		}

		List<Integer> userRoleIds = findRoleIdByUserId(userId);
		if (userRoleIds.isEmpty()) {
			return false;
		}

		for (int roleId : roleIds) {
			if (userRoleIds.contains(roleId)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean isInRole(int userId, String... zkRoleKeys) {

		if (zkRoleKeys == null || zkRoleKeys.length <= 0) {
			return false;
		}
		List<Integer> roleIds = new ArrayList<Integer>();
		for (String roleKey : zkRoleKeys) {
			if (StringUtils.isNotBlank(roleKey)) {
				if (!roleKey.startsWith("user.role.")) {
					roleKey = "user.role." + roleKey;
				}

//				int[] roleIdArr = ConfigOnZk.getIntArr("web-rpc-basedata/application.properties", roleKey);
				int[] roleIdArr = BaseConfig.getIntArr(roleKey);

				if (roleIdArr != null && roleIdArr.length > 0) {
					for (int roleId : roleIdArr) {
						roleIds.add(roleId);
					}
				}
			}
		}

		return isInRole(userId, roleIds.toArray(new Integer[0]));
	}
}