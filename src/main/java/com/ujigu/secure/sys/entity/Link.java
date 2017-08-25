package com.ujigu.secure.sys.entity;

import java.util.Date;

import com.ujigu.secure.common.bean.BaseEntity;
import com.ujigu.secure.common.bean.CommonStatus;
import com.ujigu.secure.common.checker.annotation.Validation;
import com.ujigu.secure.common.checker.constant.RequiredType;
import com.ujigu.secure.sys.constant.LinkType;

public class Link extends BaseEntity<Integer> {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer id;

    private String name;

    private String code;

    private String link;

    private String projectName;

    private Integer orderRank;

    private Byte isShare;

    private Byte needDataCheck;
    
    private LinkType dbType;

    private Integer parentId;
    
    private Date createTime;

    private Date lastUptime;

    private String remark;

    private CommonStatus dbState;
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Validation(required=RequiredType.INSERT, length="[2,20]", fieldCnName="名称")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    @Validation(required=RequiredType.INSERT)
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code == null ? null : code.trim();
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link == null ? null : link.trim();
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName == null ? null : projectName.trim();
    }

    public Integer getOrderRank() {
        return orderRank;
    }

    public void setOrderRank(Integer orderRank) {
        this.orderRank = orderRank;
    }

    @Validation(required=RequiredType.INSERT)
    public Byte getType() {
        return getDbType() == null ? null : getDbType().getDbCode();
    }

    public void setType(Byte type) {
//        this.type = type;
    	setDbType(LinkType.getByDbCode(type));
    }
    
    public LinkType getDbType() {
		return dbType;
	}

	public void setDbType(LinkType dbType) {
		this.dbType = dbType;
	}

	public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getLastUptime() {
        return lastUptime;
    }

    public void setLastUptime(Date lastUptime) {
        this.lastUptime = lastUptime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public Byte getState() {
        return getDbState() == null ? null : getDbState().getDbCode();
    }

    public void setState(Byte state) {
        setDbState(CommonStatus.getByDbCode(state));
    }
    
    public CommonStatus getDbState() {
		return dbState;
	}

	public void setDbState(CommonStatus dbState) {
		this.dbState = dbState;
	}
	
	public Byte getIsShare() {
		return isShare;
	}

	public void setIsShare(Byte isShare) {
		this.isShare = isShare;
	}

	public Byte getNeedDataCheck() {
		return needDataCheck;
	}

	public void setNeedDataCheck(Byte needDataCheck) {
		this.needDataCheck = needDataCheck;
	}
	
	public String getDisplayBgName(){
		return this.name 
//				+ (StringUtils.isNotBlank(this.getLink()) ? "(" + this.getLink() + ")" : "")
				;
	}

	public boolean isIsParent() {
		return getParentId() == null || LinkType.MENU.equals(getDbType());
	}

	@Override
    public Integer getPK() {
        return id;
    }
}