package com.ujigu.secure.sys.vo;



import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ujigu.secure.common.bean.BaseEntity;
import com.ujigu.secure.sys.constant.LinkType;


public class LinkRoleVO extends BaseEntity<Integer> implements Cloneable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Map<Integer/*acId*/, Set<Integer/*roleId*/>> roleIdMap = new HashMap<Integer, Set<Integer>>();
	
	private int id;
	private String name;
	private String code;
	private String link;
	private LinkType type;
	private int orderRank;
	private String projectName;
	private Integer parentId;
	private boolean isShare;
	private boolean needDataCheck;
	private List<LinkRoleVO> children = new ArrayList<LinkRoleVO>(15);
	
	private boolean isVirtualMenu;
	
	public LinkRoleVO() {
		this(false);
	}
	
	public LinkRoleVO(boolean isVirtualMenu){
		this.isVirtualMenu = isVirtualMenu;
	}

	public Set<Integer> getRoleIds(int acId) {
		Set<Integer> roleIds = roleIdMap.get(acId);
		return roleIds == null ? new HashSet<Integer>() : Collections.unmodifiableSet(roleIds);
	}

	public void addRoleId(int acId, Integer roleId) {
		Set<Integer> roleIds = roleIdMap.get(acId);
		if(roleIds == null){
			roleIds = new HashSet<>();
			roleIdMap.put(acId, roleIds);
		}
		roleIds.add(roleId);
	}
	
	public void addRoleIds(int acId, Set<Integer> roleIds){
		Set<Integer> existRoleIds = roleIdMap.get(acId);
		if(existRoleIds == null){
			existRoleIds = new HashSet<>();
			roleIdMap.put(acId, existRoleIds);
		}
		existRoleIds.addAll(roleIds);
	}
	
	private void addAllRoleIds(Map<Integer, Set<Integer>> roleIdMap){
		if(roleIdMap != null && !roleIdMap.isEmpty()){
			this.roleIdMap.putAll(roleIdMap);
		}
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public LinkType getType() {
		return type;
	}

	public void setType(LinkType type) {
		this.type = type;
	}

	public int getOrderRank() {
		return orderRank;
	}

	public void setOrderRank(int orderRank) {
		this.orderRank = orderRank;
	}

	public List<LinkRoleVO> getChildren() {
		return Collections.unmodifiableList(children);
	}

	public void addChild(LinkRoleVO child) {
		this.children.add(child);
	}
	
	public void addChilren(List<LinkRoleVO> children){
		this.children.addAll(children);
	}
	
	public void removeChild(LinkRoleVO child){
		this.children.remove(child);
	}
	
	public void clearChildren(){
		this.children.clear();
	}

	public boolean isVirtualMenu() {
		return isVirtualMenu;
	}
	
	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}
	
	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public boolean isShare() {
		return isShare;
	}

	public void setShare(boolean isShare) {
		this.isShare = isShare;
	}

	public boolean isNeedDataCheck() {
		return needDataCheck;
	}

	public void setNeedDataCheck(boolean needDataCheck) {
		this.needDataCheck = needDataCheck;
	}

	@Override
	public Integer getPK() {
		return this.id;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		LinkRoleVO linkRole = new LinkRoleVO();
		linkRole.setId(this.id);
		linkRole.setCode(this.code);
		linkRole.setLink(this.link);
		linkRole.setName(this.name);
		linkRole.setNeedDataCheck(this.needDataCheck);
		linkRole.setOrderRank(this.orderRank);
		linkRole.setParentId(this.parentId);
		linkRole.setProjectName(this.projectName);
		linkRole.setShare(this.isShare);
		linkRole.setType(this.type);
		linkRole.addAllRoleIds(this.roleIdMap);
		
		return linkRole;
	}
	
	@Override
	public String toString() {
		return "{id=" + this.getId() + ",name='" + this.getName() + "'}";
	}
}
