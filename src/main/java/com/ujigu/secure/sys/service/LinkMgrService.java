package com.ujigu.secure.sys.service;


import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.ujigu.secure.sys.entity.Link;
import com.ujigu.secure.sys.vo.MenuInfo;



public interface LinkMgrService {

	public Link findById(Integer id);

	public int save(Link link);

	/**
	 * 
	 * @param linkId
	 * @param roleId
	 * @return 保存链接和角色之间的关联关系
	 */
	public boolean saveLink2Roles(int linkId, List<Integer> roleIds);

	/**
	 * 
	 * @param linkId
	 * @return 返回角色对应的链接列表
	 */
	public List<Integer> findLinkByRoleId(int roleId);

	/**
	 * 
	 * @param linkId
	 * @return 返回链接对应的角色列表
	 */
	public List<Integer> findRoleIdByLinkId(int linkId);

	public boolean invalid(int id, String projectName);

	public List<Link> findRootLinks(String projectName);

	public List<MenuInfo> findAllLinks(String projectName);

	public List<Link> findLinkByParentId(int parentId);

	/**
	 * 
	 * @param projectName
	 * @return 返回给应用的登录用户的可访问菜单列表
	 */
	public List<MenuInfo> loadMenu(String projectName, int userId);

	/**
	 * 
	 * @return 提供给security使用的
	 */
	public Map<String, Collection<Integer>> loadProjectResource(
			String projectName);

	/**
	 * 
	 * @param projectName
	 * @param uri
	 * @return 获取匹配上了uri路径的链接上配置好的角色，用于权限验证
	 */
	public Collection<Integer> loadResource(String projectName,
			String uri);

	/**
	 * 
	 * @param projectName
	 * @param uri
	 * @param exactEqual
	 *            是否需要判断参数uri与权限链接完全相等。true：需要，false：不需要
	 * @return 获取匹配上了uri路径的链接上配置好的角色，用于权限验证
	 */
	public Collection<Integer> loadResource(String projectName,
			String uri, boolean exactEqual);

	/**
	 * 
	 * @param userId
	 * @param projectName
	 * @param uri
	 * @return 校验用户userId有没有访问uri的权限；如果有则返回true，否则返回false
	 */
	public boolean checkUserPriv(int userId, String projectName, String uri);

	/**
	 * 
	 * @return
	 */
//	public Map<String/* url */, PrivilegeInfo> loadUrl2PrivilegeMap();

	int findCountByLinkURL(String urlLink);

}
