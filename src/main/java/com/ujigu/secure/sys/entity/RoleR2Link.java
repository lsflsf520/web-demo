package com.ujigu.secure.sys.entity;

import com.ujigu.secure.common.bean.BaseEntity;

public class RoleR2Link extends BaseEntity<Integer> {
    private Integer id;

    private Integer acId;
    
    private Integer roleId;

    private Integer linkId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getAcId() {
		return acId;
	}

	public void setAcId(Integer acId) {
		this.acId = acId;
	}

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public Integer getLinkId() {
        return linkId;
    }

    public void setLinkId(Integer linkId) {
        this.linkId = linkId;
    }

    @Override
    public Integer getPK() {
        return id;
    }
}