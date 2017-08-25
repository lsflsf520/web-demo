package com.ujigu.secure.sys.service.impl;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.jms.Message;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.mapping.SqlCommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.ujigu.secure.cache.constant.DefaultJedisKeyNS;
import com.ujigu.secure.cache.redis.ShardJedisTool;
import com.ujigu.secure.common.bean.CommonStatus;
import com.ujigu.secure.common.bean.GlobalConstant;
import com.ujigu.secure.common.bean.GlobalResultCode;
import com.ujigu.secure.common.bean.PageInfo.OrderDirection;
import com.ujigu.secure.common.exception.BaseRuntimeException;
import com.ujigu.secure.common.utils.BaseConfig;
import com.ujigu.secure.common.utils.BeanUtils;
import com.ujigu.secure.common.utils.JsonUtil;
import com.ujigu.secure.common.utils.LogUtils;
import com.ujigu.secure.common.utils.RegexUtil;
import com.ujigu.secure.db.dao.impl.BaseDaoImpl;
import com.ujigu.secure.db.service.impl.BaseServiceImpl;
import com.ujigu.secure.mq.bean.ModifyMsg;
import com.ujigu.secure.mq.receiver.queue.DbMsgQueueHandler;
import com.ujigu.secure.sys.constant.LinkType;
import com.ujigu.secure.sys.dao.impl.LinkDaoImpl;
import com.ujigu.secure.sys.entity.Link;
import com.ujigu.secure.sys.entity.RoleR2Link;
import com.ujigu.secure.sys.service.LinkMgrService;
import com.ujigu.secure.sys.vo.LinkRoleVO;
import com.ujigu.secure.sys.vo.MenuInfo;
import com.ujigu.secure.web.common.service.DataDictService;
import com.ujigu.secure.web.util.UserLoginUtil;

@Service
public class LinkService extends BaseServiceImpl<Integer, Link> implements
		LinkMgrService, DbMsgQueueHandler {

	private final static Logger LOG = LoggerFactory
			.getLogger(LinkService.class);

	@Resource
	private LinkDaoImpl linkDaoImpl;

	@Resource
	private RoleR2LinkService roleR2LinkServiceImpl;

//	@Resource
//	private UserR2RoleService userR2RoleServiceImpl;
	
	@Resource
	private DataDictService dataDictService;
	
	private static AntPathMatcher urlMatcher = new AntPathMatcher();
	
	private final static Set<Integer> PINGBI_LINK_IDS = new HashSet<>();
	private final static Set<Integer> ONLY_SHOW_CSAI_LINK_IDS = new HashSet<>();
	
	static{
		int[] pingbiIds = BaseConfig.getIntArr("pingbi.link.ids");
		if(pingbiIds != null){
			for(int pbId : pingbiIds){
				PINGBI_LINK_IDS.add(pbId);
			}
		}
		
		int[] show4csaiLinkIds = BaseConfig.getIntArr("only.show.csai.link.ids");
		if(show4csaiLinkIds != null){
			for(int linkId : show4csaiLinkIds){
				ONLY_SHOW_CSAI_LINK_IDS.add(linkId);
			}
		}
	}
    
	@Override
	protected BaseDaoImpl<Integer, Link> getBaseDaoImpl() {

		return linkDaoImpl;
	}

	@Override
	public int save(Link link) {

		link.setLastUptime(new Date());
		if (link.getId() == null) {
			if (StringUtils.isBlank(link.getCode())) {
				link.setCode(getCode(link.getDbType()));
			}
			if (link.getOrderRank() == null) {
				link.setOrderRank(100);
			}
			link.setDbState(CommonStatus.NORMAL);
			link.setCreateTime(new Date());
			this.insertReturnPK(link);

			delLinkCache(UserLoginUtil.getAcId());
			return link.getId();
		}

		Link preLink = this.findById(link.getId());
		if (preLink == null) {
			throw new BaseRuntimeException(GlobalResultCode.NOT_EXISTS);
		}

		if (link.getIsShare() != null
				&& (preLink.getIsShare() == null || preLink.getIsShare()
						.byteValue() != link.getIsShare().byteValue())) {
			LinkRoleVO lrv = searchByLinkId(preLink.getProjectName(),
					link.getId());
			List<Integer> linkIds = parseLinkIds(lrv);
			linkDaoImpl.updateSharable(link.getIsShare(), linkIds);
		}

		if (link.getProjectName() != preLink.getProjectName()) {
			LinkRoleVO lrv = searchByLinkId(preLink.getProjectName(),
					link.getId());
			List<Integer> linkIds = parseLinkIds(lrv);
			linkDaoImpl.updateChildProjectName(link.getProjectName(), linkIds);
		}

		boolean result = this.update(link);
		if (!result) {
			throw new BaseRuntimeException(GlobalResultCode.DB_OPER_ERROR);
		}
		delLinkCache(UserLoginUtil.getAcId());
		return link.getId();
	}
	
	public static void delLinkCache(int acId){
		if (DefaultJedisKeyNS.lk.needRemoveAllCacheAfterModify()) {
			ShardJedisTool.del(DefaultJedisKeyNS.lk, acId);
			
			LogUtils.debug("link cache has refreshed for acId:%d", acId);
		}
	}

	@Override
	// @Transactional
	public boolean saveLink2Roles(int linkId, List<Integer> roleIds) {

		Link link = this.findById(linkId);

		LinkRoleVO lrv = searchByLinkId(link.getProjectName(), linkId);
		if (lrv == null) {
			throw new BaseRuntimeException(GlobalResultCode.NOT_EXISTS);
		}
		// RoleR2Link query = new RoleR2Link();
		// query.setLinkId(linkId);
		// List<RoleR2Link> preRrlList =
		// roleR2LinkServiceImpl.findByEntity(query);
		Integer acId = UserLoginUtil.getAcId();
		Set<Integer> existRoleIds = lrv.getRoleIds(acId);

		List<Integer> delRoleIds = parseDelRoleIds(existRoleIds, roleIds);

		List<Integer> linkIdList = parseLinkIds(lrv);
		roleR2LinkServiceImpl.deleteByRrl(linkIdList, delRoleIds);

		List<Integer> insertRoleIds = parseInsertRoleIds(existRoleIds, roleIds);
		if (insertRoleIds != null && !insertRoleIds.isEmpty()) {
			List<RoleR2Link> rrlList = new ArrayList<RoleR2Link>();

			Map<Integer, LinkRoleVO> lrMap = buildChildMap(lrv);
			for (int lkId : linkIdList) {
				for (int roleId : insertRoleIds) {
					LinkRoleVO childLR = lrMap.get(lkId);
					if (childLR != null
							&& !childLR.getRoleIds(acId).contains(roleId)) {
						RoleR2Link rrl = new RoleR2Link();
						rrl.setLinkId(lkId);
						rrl.setRoleId(roleId);
						rrl.setAcId(acId);

						rrlList.add(rrl);
					}
				}
			}

			roleR2LinkServiceImpl.insertBatch(rrlList);
		}
		return true;
	}

	private Map<Integer, LinkRoleVO> buildChildMap(LinkRoleVO lrv) {

		Map<Integer, LinkRoleVO> lrMap = new HashMap<Integer, LinkRoleVO>();
		lrMap.put(lrv.getId(), lrv);
		if (lrv.getChildren() != null && !lrv.getChildren().isEmpty()) {
			for (LinkRoleVO lrVo : lrv.getChildren()) {
				Map<Integer, LinkRoleVO> childLRMap = buildChildMap(lrVo);
				if (childLRMap != null && !childLRMap.isEmpty()) {
					lrMap.putAll(childLRMap);
				}
			}
		}

		return lrMap;
	}

	private List<Integer> parseLinkIds(LinkRoleVO lrv) {

		List<Integer> linkIdList = new ArrayList<Integer>();
		linkIdList.add(lrv.getId());
		if (lrv.getChildren() != null && !lrv.getChildren().isEmpty()) {
			for (LinkRoleVO child : lrv.getChildren()) {
				List<Integer> childIdList = parseLinkIds(child);
				if (!childIdList.isEmpty()) {
					linkIdList.addAll(childIdList);
				}
			}
		}

		return linkIdList;
	}

	/**
	 * 
	 * @param preRrlList
	 * @param roleIds
	 * @return 把preRrlList中有而roleIds中没有的角色ID跳出来加入待删除列表(key为delList);
	 */
	private List<Integer> parseDelRoleIds(Set<Integer> existRoleIds,
			List<Integer> roleIds) {

		List<Integer> delList = new ArrayList<Integer>();
		for (Integer existRoleId : existRoleIds) {
			if (roleIds == null || roleIds.isEmpty()
					|| !roleIds.contains(existRoleId)) {
				delList.add(existRoleId);
			}
		}

		return delList;
	}

	/**
	 * 
	 * @param preRrlList
	 * @param roleIds
	 * @return 把preRrlList没有而roleIds中有的角色ID挑出来加入待插入列表(key为insertList)
	 */
	private List<Integer> parseInsertRoleIds(Set<Integer> existRoleIds,
			List<Integer> roleIds) {

		if (existRoleIds == null || existRoleIds.isEmpty()) {
			return roleIds;
		}

		// List<Integer> preRoleIds = parseRoleIds(preRrlList);
		List<Integer> insertList = new ArrayList<Integer>();
		for (Integer roleId : roleIds) {
			if (!existRoleIds.contains(roleId)) {
				insertList.add(roleId);
			}
		}

		return insertList;
	}

	// private List<Integer> parseRoleIds(List<RoleR2Link> preRrlList){
	// List<Integer> roleIds = new ArrayList<Integer>();
	// for(RoleR2Link rrl : preRrlList){
	// roleIds.add(rrl.getRoleId());
	// }
	//
	// return roleIds;
	// }

	@Override
	public boolean invalid(int id, String projectName) {

		LinkRoleVO lrv = searchByLinkId(projectName, id);
		List<Integer> linkIds = parseLinkIds(lrv);
		boolean result = false;
		if (linkIds != null && !linkIds.isEmpty()) {
			linkDaoImpl.invalid(linkIds);
			if (DefaultJedisKeyNS.lk.needRemoveAllCacheAfterModify()) {
				ShardJedisTool.del(DefaultJedisKeyNS.lk, UserLoginUtil.getAcId());
			}
		}

		return result;
	}

	@Override
	public List<Link> findRootLinks(String projectName) {

		Link link = new Link();
		link.setProjectName(projectName);
		link.setDbType(LinkType.MENU);

		return linkDaoImpl.findRootLinks(link);
	}

	@Override
	public List<Link> findLinkByParentId(int parentId) {

		Link link = new Link();
		link.setParentId(parentId);
		link.setDbState(CommonStatus.NORMAL);

		return this.findByEntity(link, "order_rank", OrderDirection.desc);
	}

	@Override
	public List<MenuInfo> loadMenu(String projectName, int userId) {

		List<LinkRoleVO> linkRoleList = initLinkForProject(projectName); // 初始化链接(菜单)树

		List<MenuInfo> rootMenuList = new ArrayList<MenuInfo>();

		Integer roleId = UserLoginUtil.getRoleId();
		List<Integer> roleIds = roleId == null ? null : Arrays.asList(roleId);
//		List<Integer> roleIds = userR2RoleServiceImpl.loadByUserId(userId);
		if (linkRoleList != null && !linkRoleList.isEmpty()) {
			Collections.sort(linkRoleList, new Comparator<LinkRoleVO>() {

				@Override
				public int compare(LinkRoleVO link1, LinkRoleVO link2) {

					if (link1 != null && link2 != null) {
						if (link1.getOrderRank() > link2.getOrderRank()) {
							return -1;
						} else if (link1.getOrderRank() > link2.getOrderRank()) {
							return 1;
						}
					}

					return 0;
				}
			});

//			List<String> projNameList = getRelatedProjectNames(projectName);
			for (LinkRoleVO linkRole : linkRoleList) {
				boolean canshow = canShow(projectName, linkRole, roleIds, true);
				if (canshow) {
					MenuInfo menuInfo = convertMenu(linkRole, projectName, false);
					if(menuInfo != null){
						rootMenuList.add(menuInfo);
					}
				}
			}
		}

		return rootMenuList;
	}

	/**
	 * 
	 * @param linkRole 
	 * @param projectName 如果projectName为空，则不需要对菜单的projectName做校验，否则需要校验
	 * @param isMgr 调用此方法的目的是否为管理菜单用，true:是用来做菜单管理； false：加载界面左侧菜单
	 * @return
	 */
	private MenuInfo convertMenu(LinkRoleVO linkRole, String projectName, boolean isMgr) {

		if(StringUtils.isNotBlank(projectName) && LinkType.MENU.equals(linkRole.getType()) && (!projectNameMatched(projectName, linkRole.getProjectName()) || needPingbi(linkRole, isMgr))){
			return null;
		}
		MenuInfo menuInfo = new MenuInfo();
		menuInfo.setIcon("fa fa-file-text-o");
		menuInfo.setTitle(linkRole.getName());
        menuInfo.setCode(linkRole.getCode());
		menuInfo.setId(linkRole.getId() + "");
		menuInfo.setName(linkRole.getName());
		
		String url = linkRole.getLink();
		if(!isMgr && StringUtils.isNotBlank(url)){
			url = url.split("\\|")[0]; //如果是在左侧显示菜单，则取第一个链接作为菜单的链接
		}
		url = StringUtils.isNotBlank(url) ? url : linkRole.getCode();
		menuInfo.setHref(url);
		menuInfo.setUrl(url);
//		menuInfo.setProjectName(linkRole.getProjectName());
//		menuInfo.setProjectDomain(BaseConfig.getValue(linkRole.getProjectName() + ".domain"));
//		menuInfo.setProjectDomain(ConfigOnZk.getValue(ZkConstant.APP_ZK_PATH, linkRole.getProjectName() + ".web.domain",
//				ConfigOnZk.getValue("web-passport/application.properties", linkRole.getProjectName() + ".web.domain", "")));

		List<MenuInfo> childMenuList = new ArrayList<MenuInfo>();
		List<LinkRoleVO> linkRoleList = linkRole.getChildren();
		if (linkRoleList != null && !linkRoleList.isEmpty()) {
			menuInfo.setIcon("fa fa-folder-open-o");
			for (LinkRoleVO currLR : linkRoleList) {
				MenuInfo currMenu = convertMenu(currLR, projectName, isMgr);
				if(currMenu != null){
					childMenuList.add(currMenu);
				}
			}
		}

		menuInfo.setChildren(childMenuList);

		return menuInfo;
	}
	
	private boolean needPingbi(LinkRoleVO linkRole, boolean isMgr){
		return ( 
				 ( 
				  ( isMgr ||!hasSpecialRole(Arrays.asList(UserLoginUtil.getRoleId())) ) && PINGBI_LINK_IDS.contains(linkRole.getId()) 
				 ) 
				|| (ONLY_SHOW_CSAI_LINK_IDS.contains(linkRole.getId()) && UserLoginUtil.getAcId() != 1112)
				)
				;
	}

	private synchronized List<LinkRoleVO> initLinkForProject(String projectName) {

		// 先从缓存中读取菜单权限树
		List<String> valList = getRelatedProjectNames(projectName);
		String projKey = buildProjKey(valList, projectName);

		Integer acId = UserLoginUtil.getAcId();
		String linkTreeStr = ShardJedisTool.hget(DefaultJedisKeyNS.lk, acId, "lfp"
				+ projKey);
		if (StringUtils.isNotBlank(linkTreeStr)) {
			List<LinkRoleVO> cacheList = JsonUtil.create().fromJson(linkTreeStr,
					new TypeToken<List<LinkRoleVO>>() {
					}.getType());
			return cacheList;
		}

		List<LinkRoleVO> rootLinkRoleList = initLinkForProj(projectName);
		for (String val : valList) {
			if (!projectName.equals(val)) {
				List<LinkRoleVO> otherProjList = initLinkForProj(val);

				rootLinkRoleList.addAll(otherProjList);
			}
		}

		
		
		if (!rootLinkRoleList.isEmpty()) {
			ShardJedisTool.hset(DefaultJedisKeyNS.lk, acId, "lfp" + projKey,
					JsonUtil.create().toJson(rootLinkRoleList,
							new TypeToken<List<LinkRoleVO>>() {
							}.getType()));
		}

		return rootLinkRoleList;
	}

	private List<String> getRelatedProjectNames(String projectName) {

//		String[] valArr = ConfigOnZk.getValueArr(ZkConstant.APP_ZK_PATH, projectName + ".related.projectName");

//		return Arrays.asList(valArr == null || valArr.length <= 0 ? new String[] { projectName } : valArr);
		
		return Arrays.asList(new String[] { projectName });
	}

	private String buildProjKey(List<String> valList, String projectName) {

		Collections.sort(valList);

		String ret = "";
		for (String val : valList) {
			ret += val + "_";
		}

		return ret.substring(0, ret.length() - 1);
	}

	private List<LinkRoleVO> initLinkForProj(String projectName) {

		List<LinkRoleVO> rootLinkRoleList = new ArrayList<LinkRoleVO>(); // 从顶级菜单开始，构建树形结构

		List<Link> links = linkDaoImpl.loadForProject(projectName);

		if (links != null && !links.isEmpty()) {
			List<LinkRoleVO> linkRoleList = convert(links);
			Map<Integer, LinkRoleVO> linkMap = BeanUtils
					.buildPK2BeanMap(linkRoleList);
//			LinkRoleVO virtualMenu = new LinkRoleVO(true);
//			rootLinkRoleList.add(virtualMenu); // 先将虚拟菜单加入根菜单
			for (LinkRoleVO linkRole : linkRoleList) {

				if (linkRole.getParentId() == null) {
					if (LinkType.MENU.equals(linkRole.getType())) {
						rootLinkRoleList.add(linkRole);// 如果是根根菜单，先加入根菜单列表
					} 
//					else {
//						// 如果没有父节点，且链接类型不是菜单，就加入虚拟链接列表中
//						virtualMenu.addChild(linkRole);
//					}
				} else {
					LinkRoleVO parentLinkRole = linkMap.get(linkRole
							.getParentId());
					if (parentLinkRole == null) {
						LOG.warn("not found parent Link with parentId '" + linkRole.getParentId() + "' for current link " + linkRole.getId());
					} else {
						parentLinkRole.addChild(linkRole);
					}
				}
			}
		}

		List<RoleR2Link> rrlList = roleR2LinkServiceImpl.findAll();
		Map<Integer/*acId*/,Map<Integer/* linkId */, Set<Integer /* roleId */>>> acId2link2RoleIdMap = parseLink2Roles(rrlList);
		configRole(rootLinkRoleList, acId2link2RoleIdMap);

		return rootLinkRoleList;
	}

	/**
	 * 
	 * @param projectName
	 *            工程名
	 * @param linkId
	 *            菜单ID
	 * @return 从菜单树中查找指定的菜单，如果该菜单下有子菜单，则其children属性将不会为空
	 */
	private LinkRoleVO searchByLinkId(String projectName, int linkId) {

		List<LinkRoleVO> rootLinkRoleList = initLinkForProject(projectName);

		return searchByLinkId(rootLinkRoleList, linkId);
	}

	private LinkRoleVO searchByLinkId(List<LinkRoleVO> rootLinkRoleList,
			int linkId) {

		if (rootLinkRoleList != null && !rootLinkRoleList.isEmpty()) {
			for (LinkRoleVO lrv : rootLinkRoleList) {
				if (lrv.getId() == linkId) {
					return lrv;
				}
				LinkRoleVO target = searchByLinkId(lrv.getChildren(), linkId);
				if (target != null) {
					return target;
				}
			}
		}

		return null;
	}

	private void configRole(List<LinkRoleVO> rootLinkRoleList,
			Map<Integer/*acId*/,Map<Integer/* linkId */, Set<Integer /* roleId */>>> acId2Link2RoleIdMap) {

		if (rootLinkRoleList == null || rootLinkRoleList.isEmpty()
				|| acId2Link2RoleIdMap == null || acId2Link2RoleIdMap.isEmpty()) {
			return;
		}
		for (LinkRoleVO linkRole : rootLinkRoleList) {
			for(Integer acId : acId2Link2RoleIdMap.keySet()){
				Map<Integer/* linkId */, Set<Integer /* roleId */>> link2RoleIdMap = acId2Link2RoleIdMap.get(acId);
				if(link2RoleIdMap != null){
					Set<Integer /* roleId */> roleIds = link2RoleIdMap.get(linkRole
							.getId());
					if (roleIds != null && !roleIds.isEmpty()) {
						linkRole.addRoleIds(acId, roleIds);
					}
				}
			}

			// Set<Integer> parentRoleIds = link2RoleIds.get(linkRole
			// .getParentId());
			// if (parentRoleIds != null && !parentRoleIds.isEmpty()) {
			// linkRole.addRoleIds(parentRoleIds);
			// }

			configRole(linkRole.getChildren(), acId2Link2RoleIdMap); // 递归配置子菜单的角色
		}

	}

	private Map<Integer/*acId*/,Map<Integer/* linkId */, Set<Integer /* roleId */>>> parseLink2Roles(
			List<RoleR2Link> rrlList) {

		Map<Integer/*acId*/,Map<Integer/* linkId */, Set<Integer /* roleId */>>> acId2link2RoleIdMap = new HashMap<Integer, Map<Integer, Set<Integer>>>();
		if (rrlList != null && !rrlList.isEmpty()) {
			for (RoleR2Link rrl : rrlList) {
				Map<Integer/* linkId */, Set<Integer /* roleId */>> link2RoleIdMap = acId2link2RoleIdMap.get(rrl.getAcId());
				if(link2RoleIdMap == null){
					link2RoleIdMap = new HashMap<Integer, Set<Integer>>();
					acId2link2RoleIdMap.put(rrl.getAcId(), link2RoleIdMap);
				}
				Set<Integer> roleIds = link2RoleIdMap.get(rrl.getLinkId());
				if (roleIds == null) {
					roleIds = new HashSet<Integer>();
					link2RoleIdMap.put(rrl.getLinkId(), roleIds);
				}
				roleIds.add(rrl.getRoleId());
			}
		}

		return acId2link2RoleIdMap;
	}

	private List<LinkRoleVO> convert(List<Link> links) {

		List<LinkRoleVO> linkRoleList = new ArrayList<LinkRoleVO>();
		for (Link link : links) {
			LinkRoleVO linkRole = convert(link);
			linkRoleList.add(linkRole);
		}

		return linkRoleList;
	}

	private LinkRoleVO convert(Link link) {

		LinkRoleVO linkRole = new LinkRoleVO();
		linkRole.setCode(link.getCode());
		linkRole.setId(link.getId());
		linkRole.setLink(link.getLink());
		linkRole.setProjectName(link.getProjectName());
		linkRole.setName(link.getName());
		linkRole.setParentId(link.getParentId());
		linkRole.setOrderRank(link.getOrderRank());
		linkRole.setShare(link.getIsShare() != null
				&& link.getIsShare() != (byte) 0);
		linkRole.setNeedDataCheck(link.getNeedDataCheck() != null
				&& link.getNeedDataCheck() != (byte) 0);
		linkRole.setType(link.getDbType());

		return linkRole;
	}

	private boolean canShow(String projectName, LinkRoleVO linkRole,
			List<Integer> roleIds, boolean isRoot) {

		if (linkRole.isVirtualMenu()
				|| !LinkType.MENU.equals(linkRole.getType())) {
			return false;
		}
		boolean rootCanshow = isRoot ? checkAuth(linkRole, roleIds, projectName) : false;
		// if (rootCanshow) { // 如果有当前菜单的权限，则会有该菜单和其子菜单的权限
		// filterChildMenu(linkRole);
		// return true;
		// }

		List<LinkRoleVO> children = linkRole.getChildren();
		if (children == null || children.isEmpty()) {
			return rootCanshow;
		}

		List<LinkRoleVO> authChildList = new ArrayList<LinkRoleVO>();
		for (LinkRoleVO child : children) {
			if (!LinkType.MENU.equals(child.getType())) {
				// 忽略掉功能链接，因为不需要显示在菜单上
				continue;
			}
			boolean canshow = checkAuth(child, roleIds, projectName);
			// if (canshow) {
			// rootCanshow = true;
			// filterChildMenu(child);
			// authChildList.add(child);
			// continue;
			// }

			// 检查可以显示的子菜单
			boolean hasChildShow = canShow(projectName, child, roleIds, false);

			if (hasChildShow || canshow) {
				authChildList.add(child);
				rootCanshow = true;
			}
		}

		linkRole.clearChildren();
		linkRole.addChilren(authChildList);

		return rootCanshow;
	}
	
	private boolean projectNameMatched(String projectName, String linkProjectNames){
		if(StringUtils.isNotBlank(linkProjectNames)){
		    String[] pjNames = linkProjectNames.split(",");
		    for(String pjName : pjNames){
			    if(pjName.equalsIgnoreCase(projectName)){
				    return true;
			    }
		    }
		}
		
		return false;
	}

	/**
	 * 
	 * @param linkRole
	 * @return 剔除掉功能菜单
	 */
	// private void filterChildMenu(LinkRoleVO linkRole) {
	// List<LinkRoleVO> children = linkRole.getChildren();
	// if (children == null || children.isEmpty()) {
	// return;
	// }
	//
	// List<LinkRoleVO> needRemovedList = new ArrayList<LinkRoleVO>();
	// for (LinkRoleVO child : children) {
	// if (!LinkType.MENU.equals(child.getType())) {
	// needRemovedList.add(child);
	// continue;
	// }
	//
	// filterChildMenu(child);
	// }
	//
	// for (LinkRoleVO removed : needRemovedList) {
	// linkRole.removeChild(removed);
	// }
	// }

	private boolean checkAuth(LinkRoleVO child, List<Integer> roleIds, String projectName) {
		boolean matched = false;
		if(!projectNameMatched(projectName, child.getProjectName())){ //如果當前工程projectName沒在链接的白名单中，则直接返回false
			return matched;
		}

		if (CollectionUtils.isEmpty(roleIds)) {
			LOG.debug("No authority granted for current User");
		} else {
			if(hasSpecialRole(roleIds)){ //特殊角色，拥有这个角色的用户，所有菜单都可见
				matched = true;
			}else{
				Set<Integer> needRoleIds = child.getRoleIds(UserLoginUtil.getAcId());
				for (int roleId : roleIds) {
					if (needRoleIds.contains(roleId)) {
						LOG.debug(
								"Found matched and granted authority for URL: {}, roleId: {}",
								StringUtils.isBlank(child.getLink()) ? child
										.getName() : child.getLink(), roleId);
						matched = true;
						break;
					}
				}
			}
			if (!matched) {
				LOG.debug("Required but NO granted authority for URL: {}",
						StringUtils.isBlank(child.getLink()) ? child.getName()
								: child.getLink());
			}
		}

		return matched;
	}

	private String getCode(LinkType linkType) {

		return ""
				+ (linkType == null ? LinkType.MENU.name().charAt(0) : linkType
						.name().charAt(0))
				+ (org.apache.commons.lang.math.RandomUtils.nextInt(1000000) + 100);
	}

	@Override
	public List<Integer> findRoleIdByLinkId(int linkId) {

		RoleR2Link rrl = new RoleR2Link();
		rrl.setLinkId(linkId);
		List<RoleR2Link> rrlList = roleR2LinkServiceImpl.findByEntity(rrl);
		List<Integer> roleIds = new ArrayList<Integer>();
		if (rrlList != null && !rrlList.isEmpty()) {
			for (RoleR2Link currRrl : rrlList) {
				roleIds.add(currRrl.getRoleId());
			}
		}
		return roleIds;
	}

	@Override
	public Map<String, Collection<Integer>> loadProjectResource(
			String projectName) {

		List<String> valList = getRelatedProjectNames(projectName);
		String projKey = buildProjKey(valList, projectName);

		String attrStr = ShardJedisTool.hget(DefaultJedisKeyNS.lk, UserLoginUtil.getAcId(), "lpr"
				+ projKey);
		if (StringUtils.isNotBlank(attrStr)) {
			Map<String, Collection<Integer>> currResMap = new Gson()
					.fromJson(
							attrStr,
							new TypeToken<Map<String, Collection<Integer>>>() {
							}.getType());

			LOG.debug("load Spring Security Resource from cache");
			return convert(currResMap);
		}

		Map<String, Collection<Integer>> currResMap = new LinkedHashMap<String, Collection<Integer>>();

		List<LinkRoleVO> linkRoleList = initLinkForProject(projectName);

		List<LinkRoleVO> haslinkList = grabLinkRoleHasLink(linkRoleList);
		// 根据链接的字母顺序倒序
		Collections.sort(haslinkList, new Comparator<LinkRoleVO>() {

			@Override
			public int compare(LinkRoleVO o1, LinkRoleVO o2) {

				if (StringUtils.isBlank(o1.getLink())
						|| StringUtils.isBlank(o2.getLink())) {
					return Integer.valueOf(o2.getOrderRank()).compareTo(
							o1.getOrderRank());
				}
				return o2.getLink().compareTo(o1.getLink());
			}
		});

		for (LinkRoleVO linkRole : haslinkList) {
			addURL2Role(currResMap, linkRole.getLink(), linkRole.getRoleIds(UserLoginUtil.getAcId()));
		}

		if (currResMap != null && !currResMap.isEmpty()) {
			ShardJedisTool
					.hset(DefaultJedisKeyNS.lk, UserLoginUtil.getAcId(),
							"lpr" + projKey,
							new Gson()
									.toJson(currResMap,
											new TypeToken<Map<String, Collection<Integer>>>() {
											}.getType()));
		}

		return convert(currResMap);
	}

	private Map<String, Collection<Integer>> convert(
			Map<String, Collection<Integer>> currResMap) {

		Map<String, Collection<Integer>> resMap = new LinkedHashMap<String, Collection<Integer>>();
		Set<String> keyset = currResMap.keySet();
		for (String key : keyset) {
			Collection<Integer> attrList = new ArrayList<Integer>();
			attrList.addAll(currResMap.get(key));

			resMap.put(key, attrList);
		}

		return resMap;
	}

//	@Override
//	public Map<String, PrivilegeInfo> loadUrl2PrivilegeMap() {
//
//		// TODO Auto-generated method stub
//		return new HashMap<String, PrivilegeInfo>();
//	}

	/**
	 * 
	 * @param linkRoleList
	 * @return 从树形结构linkRoleList中提取带有链接的菜单或功能
	 */
	private List<LinkRoleVO> grabLinkRoleHasLink(List<LinkRoleVO> linkRoleList) {

		List<LinkRoleVO> hasLinkList = new ArrayList<LinkRoleVO>();

		if (linkRoleList != null && !linkRoleList.isEmpty()) {
			for (LinkRoleVO linkRole : linkRoleList) {
				if (StringUtils.isNotBlank(linkRole.getLink())) {
					try {
						hasLinkList.add((LinkRoleVO) linkRole.clone());
					} catch (CloneNotSupportedException e) {
						LOG.error(e.getMessage(), e);
					}
				}
				List<LinkRoleVO> children = linkRole.getChildren();
				if (children != null && !children.isEmpty()) {
					List<LinkRoleVO> childHasLinkList = grabLinkRoleHasLink(children);
					hasLinkList.addAll(childHasLinkList);
				}
			}
		}

		return hasLinkList;
	}

	private void addURL2Role(Map<String, Collection<Integer>> map,
			String url, Set<Integer> roleIds) {

		for (String splitUrl : url.split("\\|")) {
			if (StringUtils.isNotBlank(splitUrl)) {
				splitUrl = splitUrl.trim().replace("?", "*");
				if (!"/".equals(splitUrl) && !splitUrl.endsWith("/**")) {
					splitUrl = splitUrl + "*/**";
				}
				Collection<Integer> configAttributes = map.get(splitUrl);
				if (configAttributes == null) {
					configAttributes = new ArrayList<Integer>();
					map.put(splitUrl, configAttributes);

				}
				if (roleIds != null && !roleIds.isEmpty()) {
					for (Integer roleId : roleIds) {
						configAttributes.add(roleId);
					}
				}
			}
		}
	}

	@Override
	public Collection<Integer> loadResource(String projectName,
			String uri) {

		return loadResource(projectName, uri, true);
	}

	@Override
	public Collection<Integer> loadResource(String projectName,
			String uri, boolean exactEqual) {

		if (StringUtils.isNotBlank(uri)) {
			Map<String, Collection<Integer>> currResMap = loadProjectResource(projectName);
			Iterator<String> ite = currResMap.keySet().iterator();
			while (ite.hasNext()) {
				String resURL = ite.next();
				if (exactEqual) {
					String exactURL = resURL.replaceAll("\\**/\\*+", "");
					if (uri.equals(exactURL) || uri.equals(exactURL + "/")) {
						return currResMap.get(resURL);
					}
				} else if (urlMatcher.match(resURL, uri)) {
					return currResMap.get(resURL);
				}
			}
		}
		return Collections.emptyList();
	}

	@Override
	public boolean checkUserPriv(int userId, String projectName, String uri) {
//		List<Integer> roleIds = userR2RoleServiceImpl.loadByUserId(userId);
		Integer roleId = UserLoginUtil.getRoleId();
		List<Integer> roleIds = roleId == null ? new ArrayList<Integer>() : Arrays.asList(roleId);
		if (roleIds == null || roleIds.isEmpty()) {
			return false;
		}
		if(hasSpecialRole(roleIds)){
			return true;
		}
		
		Collection<Integer> needRoleList = loadResource(projectName,
				uri);
		if (needRoleList == null || needRoleList.isEmpty()) {
			return false;
		}

		for (Serializable needRole : needRoleList) {
//			String needRole = config.getAttribute();
			if (needRole instanceof Integer
					&& roleIds.contains(needRole)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 判断roleIds中是否有指定的特殊角色
	 * @param roleIds
	 * @return
	 */
	private boolean hasSpecialRole(List<Integer> roleIds){
		if(roleIds != null){
			List<Integer> specialRoles = RoleService.getSuperAdminRoles();
			for(Integer roleId : roleIds){
				if(specialRoles.contains(roleId)){
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public List<Integer> findLinkByRoleId(int roleId) {

		this.findLinkByParentId(1);
		RoleR2Link rrl = new RoleR2Link();
		rrl.setRoleId(roleId);
		rrl.setAcId(UserLoginUtil.getAcId());
		List<RoleR2Link> rrlList = roleR2LinkServiceImpl.findByEntity(rrl);
		List<Integer> links = new ArrayList<Integer>();
		if (rrlList != null && !rrlList.isEmpty()) {
			for (RoleR2Link currRrl : rrlList) {
				links.add(currRrl.getLinkId());
			}
		}
		return links;
	}

	@Override
	public List<MenuInfo> findAllLinks(String projectName) {

		List<LinkRoleVO> linkRoleList = initLinkForProject(projectName); // 初始化链接(菜单)树
		List<MenuInfo> rootMenuList = new ArrayList<MenuInfo>();
		// List<Integer> roleIds = userR2RoleServiceImpl.loadByUserId(userId);
		if (linkRoleList != null && !linkRoleList.isEmpty()) {
			for (LinkRoleVO linkRole : linkRoleList) {
				/*if (!projectName.equals(linkRole.getProjectName())) {
					continue;
				}*/

				MenuInfo menuInfo = convertMenu(linkRole, GlobalConstant.IS_WEB_ADMIN ? null : projectName, true);
				if(menuInfo != null){
					rootMenuList.add(menuInfo);
				}
			}
		}

		return rootMenuList;
	}
	
	@Override
	public int findCountByLinkURL(String urlLink) {

		Link link = new Link();
		link.setLink(urlLink);
		link.setDbState(CommonStatus.NORMAL);
		List<Link> result = linkDaoImpl.findByEntity(link);
		return result.size();
	}

	@Override
	public void handleMsg(Message message, ModifyMsg modifyMsg) {
		if(SqlCommandType.UNKNOWN.equals(modifyMsg.getCmdType())){
			Map<String, String> dataMap = dataDictService.getKVMap("ac_id_name");
			if(!CollectionUtils.isEmpty(dataMap)){
				for(String key : dataMap.keySet()){
					if(RegexUtil.isInt(key)){
						delLinkCache(Integer.valueOf(key));
					}
				}
			}
		}
	}

	@Override
	public String supportTables() {
		return "link";
	}

}