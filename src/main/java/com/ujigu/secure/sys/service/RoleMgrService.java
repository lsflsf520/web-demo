package com.ujigu.secure.sys.service;
import java.util.List;

import org.springframework.data.domain.Page;

import com.ujigu.secure.sys.entity.Role;
import com.ujigu.secure.sys.entity.Worker;



public interface RoleMgrService {

	public boolean save(Role role);

	/**
	 * 
	 * @param roleId
	 * @param linkIds
	 * @return 保存角色和链接的关联关系
	 */
	public boolean saveRole2Links(int roleId, List<Integer> savelinkIds,
			String projectName);

	/**
	 * 
	 * @param userId
	 * @param roleIds
	 * @return
	 */
	public boolean saveUser2Roles(int userId, List<Integer> roleIds);

	/**
	 * 
	 * @return 获取可以用于设置用户类型的角色
	 */
	public List<Role> findUserRole();

	public boolean invalid(int id);

	public List<Role> findAvailRoles();

	public List<Role> findByUserId(int userId);

	/**
	 * 
	 * @param userId
	 * @param roleIds
	 *            角色的Id数组
	 * @return 如果userId所拥有的角色中，至少有一个在roleIds中，就返回true；否则返回false
	 */
	public boolean isInRole(int userId, Integer... roleIds);

	/**
	 * 
	 * @param userId
	 * @param zkRoleKeys
	 *            配置在zk上web-rpc-basedata/application.properties节点中的以“user.role.”
	 *            开头的key的后半部分字符串，其值一般是数据库中对应的角色Id。
	 *            例如：user.role.gradeLeader=1014
	 *                 user.role.schoolLeader=1016
	 *            那么 zkRoleKeys 就只需传入 "gradeLeader","schoolLeader" 即可，
	 *                也可以整个字符串传入 "user.role.gradeLeader","user.role.schoolLeader"
	 * @return 如果userId所拥有的角色中，至少有一个在zkRoleKeys中，就返回true；否则返回false
	 */
	public boolean isInRole(int userId, String... zkRoleKeys);

	public List<Integer> findRoleIdByUserId(int userId);

	Page<Role> searchRole(String roleName, int currPage, int maxRows);

	public Role findById(Integer pk);

	/**
	 * 
	 * @param roleid
	 * @return 查询包含某个角色的用户列表
	 */
	public List<Integer> findUsersByRoles(Integer roleId);

	/**
	 * 
	 * @param roleId
	 * @return 返回某角色下的用户列表
	 */
	public List<Worker> findUserListByRole(int roleId);

}
