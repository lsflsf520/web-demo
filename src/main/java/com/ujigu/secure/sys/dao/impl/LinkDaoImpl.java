package com.ujigu.secure.sys.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Repository;

import com.ujigu.secure.db.dao.BaseDao;
import com.ujigu.secure.db.dao.impl.BaseDaoImpl;
import com.ujigu.secure.sys.dao.LinkDao;
import com.ujigu.secure.sys.entity.Link;


@Repository
public class LinkDaoImpl extends BaseDaoImpl<Integer, Link> {
    @Resource
    private LinkDao linkDao;

    @Override
    protected BaseDao<Integer, Link> getProxyBaseDao() {
        return linkDao;
    }
    
    public List<Link> findRootLinks(Link queryEntity){
    	return this.getSqlSessionTemplate().selectList(getMapperNamespace() + ".findRootLinks", queryEntity);
    }
    
   
    
    /**
     * 
     * @param projectName
     * @return 登录系统后加载菜单使用
     */
    public List<Link> loadForProject(String projectName){
    	return this.getSqlSessionTemplate().selectList(getMapperNamespace() + ".loadForProject", projectName);
    }
    
    /**
     * 
     * @param isShare
     * @param linkIds
     * @return
     */
    public boolean updateSharable(byte isShare, List<Integer> linkIds){
    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("isShare", isShare);
    	paramMap.put("linkIds", linkIds);
    	return this.getSqlSessionTemplate().update(getMapperNamespace() + ".updateSharable", paramMap) >= 0;
    }
    
    public boolean updateChildProjectName(String projectName, List<Integer> linkIds){
    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("projectName", projectName);
    	paramMap.put("linkIds", linkIds);
    	return this.getSqlSessionTemplate().update(getMapperNamespace() + ".updateChildProjectName", paramMap) >= 0;
    }
    
    public boolean invalid(List<Integer> linkIds){
    	return this.getSqlSessionTemplate().update(getMapperNamespace() + ".invalid", linkIds) >= 0;
    }
}