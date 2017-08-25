package com.ujigu.secure.sys.vo;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class MenuInfo {

	private String id;

	/** 菜单代码，用于前端JS控制 */
	private String code;

	/** 菜单名称，菜单显示的字面值. */
	private String name;
	
	//layui menu will use the attributes
	private String title;
	private String href;
	private boolean spread;

	/** 图标，基于Bootstrap3，如fa-user */
	private String icon;
	/**
	 * 菜单URL.
	 */
	private String url = "";

	private String projectDomain;
	
	/** 父节点. */
	private MenuInfo parent;

	/** 孩子节点. */
	private List<MenuInfo> children = new ArrayList<MenuInfo>();

	/** 是否默认展开菜单组 */
	private Boolean open = Boolean.FALSE;

	// public static enum MenuTypeEnum {
	// RELC,
	// RELD,
	// ABS;
	// }
	
	public String getId() {

		return id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public boolean isSpread() {
		return spread;
	}

	public void setSpread(boolean spread) {
		this.spread = spread;
	}

	public void setId(String id) {

		this.id = id;
	}

	public String getUrl() {

		return url;
	}

	public void setUrl(String url) {

		this.url = url;
	}

	public String getProjectDomain() {

		return projectDomain;
	}

	public void setProjectDomain(String projectDomain) {

		this.projectDomain = projectDomain;
	}

	public String getName() {

		return name;
	}

	public void setName(String name) {

		this.name = name;
	}

	public MenuInfo getParent() {

		return parent;
	}

	public void setParent(MenuInfo parent) {

		this.parent = parent;
	}

	public List<MenuInfo> getChildren() {

		return children;
	}

	public void setChildren(List<MenuInfo> children) {

		this.children = children;
	}

	public Boolean getOpen() {

		return open;
	}

	public void setOpen(Boolean open) {

		this.open = open;
	}

	public int getChildrenSize() {

		return children == null ? 0 : children.size();
	}

	public String getIcon() {

		return icon;
	}

	public void setIcon(String icon) {

		this.icon = icon;
	}

	public boolean isHasIcon() {

		return StringUtils.isNotBlank(icon);
	}

	public String getCode() {

		return code;
	}

	public void setCode(String code) {

		this.code = code;
	}
}