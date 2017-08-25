package com.ujigu.secure.sys.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.ujigu.secure.common.bean.CommonStatus;
import com.ujigu.secure.common.bean.GlobalConstant;
import com.ujigu.secure.common.exception.BaseRuntimeException;
import com.ujigu.secure.common.utils.EncryptTools;
import com.ujigu.secure.common.utils.RegexUtil;
import com.ujigu.secure.db.dao.impl.BaseDaoImpl;
import com.ujigu.secure.db.service.impl.BaseServiceImpl;
import com.ujigu.secure.sys.dao.impl.WorkerDaoImpl;
import com.ujigu.secure.sys.entity.Worker;

@Service
public class WorkerService extends BaseServiceImpl<Integer, Worker> {
    @Resource
    private WorkerDaoImpl workerDaoImpl;

    @Override
    protected BaseDaoImpl<Integer, Worker> getBaseDaoImpl() {
        return workerDaoImpl;
    }
    
    public List<Worker> findWorkers(Integer... workerIds){
    	if (workerIds == null || workerIds.length <= 0) {
			return null;
		}

		return workerDaoImpl.findWorkers(workerIds);
    }
    
    /**
     * 判断手机号、登录名和邮箱是否已经存在
     * @param worker
     * @return
     */
    public boolean canAdd(Worker worker){
    	List<Worker> dbWorkers = workerDaoImpl.findWorkerByUniqFlag(worker);
    	if(dbWorkers != null && !dbWorkers.isEmpty()){
    		StringBuilder errorMsg = new StringBuilder();
    		boolean existLoginName = false, existPhone = false, existEmail = false;
    		for(Worker dbwk : dbWorkers){
    			if(StringUtils.isNotBlank(worker.getLoginName()) && worker.getLoginName().equals(dbwk.getLoginName())){
    				existLoginName = true;
    			}
    			if(StringUtils.isNotBlank(worker.getPhone()) && worker.getPhone().equals(dbwk.getPhone())){
    				existPhone = true;
    			}
    			if(StringUtils.isNotBlank(worker.getEmail()) && worker.getEmail().equals(dbwk.getEmail())){
    				existEmail = true;
    			}
    		}
    		if(existLoginName){
    			errorMsg.append("登录名、");
    		}
    		if(existPhone){
    			errorMsg.append("手机号、");
    		}
    		if(existEmail){
    			errorMsg.append("邮箱、");
    		}
    		if(errorMsg.length() > 0){
    			errorMsg.setLength(errorMsg.length() - 1);
    		}
    		throw new BaseRuntimeException("DATA_EXIST", errorMsg.toString() + "已存在！");
    	}
    	
    	return true;
    }
    
    
    
	public Worker getWorkerByLoginName(int acId, String loginName) {

		Worker query = new Worker();
		query.setLoginName(loginName);
		query.setAcId(acId);
		
		return findOneByEntity(query);
	}
	
	public Worker getWorkerByPhone(int acId, String phone) {

		Worker query = new Worker();
		query.setPhone(phone);
		query.setAcId(acId);
		
		return findOneByEntity(query);
	}
	
	public Worker getWorkerByEmail(int acId, String email) {

		Worker query = new Worker();
		query.setEmail(email);
		query.setAcId(acId);
		
		return findOneByEntity(query);
	}

	public boolean resetPasswd(int userId, String password) {

		// String encPasswd = encoder.encodePassword(password, userId);
		Worker worker = new Worker();
		worker.setId(userId);
		worker.setPassword(EncryptTools.encrypt(password));

		return this.update(worker);
	}

	public boolean checkPasswd(int userId, String password) {

		Worker user = this.findById(userId);

		if (user == null) {
			return false;
		}

		String encPasswd = EncryptTools.encrypt(password);

		return StringUtils.isNotBlank(encPasswd)
				&& encPasswd.equals(user.getPassword());
	}

	public Worker checkPasswd(int acId, String loginName, String password) {

		if (StringUtils.isBlank(password) || StringUtils.isBlank(loginName)) {
			throw new BaseRuntimeException("ILLEGAL_PARAM", "用户名和密码均不能为空");
		}
		Worker user = null;
		if (RegexUtil.isPhone(loginName)) {
			user = this.getWorkerByPhone(acId, loginName);
		} else if (RegexUtil.isEmail(loginName)) {
			user = this.getWorkerByEmail(acId, loginName);
		} else {
			user = this.getWorkerByLoginName(acId, loginName);
		}

		if (user == null) {
			throw new BaseRuntimeException("NOT_EXIST", "用户名 '" + loginName
					+ "' 不存在" + (!GlobalConstant.IS_WEB_ADMIN ? ",请确认用户名和合作企业编号是否正确！" : ""));
		}

		if (!CommonStatus.NORMAL.equals(user.getStatus())) {
			throw new BaseRuntimeException("NOT_FORBIDDEN", "用户名 '" + loginName
					+ "' 已被禁用，请联系管理员！");
		}

		String encPasswd =  EncryptTools.encrypt(password);

		if (encPasswd.equals(user.getPassword())) {
			return user;
		}

		throw new BaseRuntimeException("ILLEGAL_PARAM", "密码不正确",
				"password invalid for loginName " + loginName);
	}
}