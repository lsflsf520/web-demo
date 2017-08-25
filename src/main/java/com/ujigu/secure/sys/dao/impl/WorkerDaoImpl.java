package com.ujigu.secure.sys.dao.impl;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

import com.ujigu.secure.db.dao.BaseDao;
import com.ujigu.secure.db.dao.impl.BaseDaoImpl;
import com.ujigu.secure.sys.dao.WorkerDao;
import com.ujigu.secure.sys.entity.Worker;

@Repository
public class WorkerDaoImpl extends BaseDaoImpl<Integer, Worker> {
    @Resource
    private WorkerDao workerDao;

    @Override
    protected BaseDao<Integer, Worker> getProxyBaseDao() {
        return workerDao;
    }
    
    public List<Worker> findWorkers(Integer... userIds) {

		return this.getSqlSessionTemplate().selectList(getMapperNamespace() + ".findWorkers", userIds);
	}
    
    /**
     * 根据登录名、手机号和邮箱号进行查找
     * @param worker
     * @return
     */
    public List<Worker> findWorkerByUniqFlag(Worker worker){
    	if(StringUtils.isBlank(worker.getLoginName())){
    		worker.setLoginName(null);
    	}else{
    		worker.setLoginName(worker.getLoginName().trim());
    	}
    	if(StringUtils.isBlank(worker.getPhone())){
    		worker.setPhone(null);
    	}else{
    		worker.setPhone(worker.getPhone().trim());
    	}
    	if(StringUtils.isBlank(worker.getEmail())){
    		worker.setEmail(null);
    	}else{
    		worker.setEmail(worker.getEmail().trim());
    	}
    	return this.getSqlSessionTemplate().selectList(getMapperNamespace() + ".findWorkerByUniqFlag", worker);
    }
}