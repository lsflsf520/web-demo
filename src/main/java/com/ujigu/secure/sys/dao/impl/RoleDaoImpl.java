package com.ujigu.secure.sys.dao.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Repository;

import com.ujigu.secure.db.dao.BaseDao;
import com.ujigu.secure.db.dao.impl.BaseDaoImpl;
import com.ujigu.secure.sys.dao.RoleDao;
import com.ujigu.secure.sys.entity.Role;

@Repository
public class RoleDaoImpl extends BaseDaoImpl<Integer, Role> {
    @Resource
    private RoleDao roleDao;

    @Override
    protected BaseDao<Integer, Role> getProxyBaseDao() {
        return roleDao;
    }
    
    /**
     * 
     * @param userId
     * @return
     */
    public List<Role> findByUserId(long userId){
    	return this.getSqlSessionTemplate().selectList(getMapperNamespace() + ".findByUserId", userId);
    }
    
    /**
     * 
     * @return 获取可以用于设置用户类型的角色
     */
    public List<Role> findUserRole(){
    	return this.getSqlSessionTemplate().selectList(getMapperNamespace() + ".findUserRole");
    }
    
}