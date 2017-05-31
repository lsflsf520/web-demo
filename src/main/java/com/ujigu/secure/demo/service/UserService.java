package com.ujigu.secure.demo.service;

import com.ujigu.secure.db.dao.IBaseDao;
import com.ujigu.secure.db.service.AbstractBaseService;
import com.ujigu.secure.demo.dao.UserDao;
import com.ujigu.secure.demo.entity.User;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class UserService extends AbstractBaseService<Integer, User> {
    @Resource
    private UserDao userDao;

    @Override
    protected IBaseDao<Integer, User> getBaseDao() {
        return userDao;
    }

    public Integer insertReturnPK(User user) {
        return userDao.insertReturnPK(user);
    }

    public Integer doSave(User user) {
        if (user.getPK() == null) {
            return this.insertReturnPK(user);
        }
        this.update(user);
        return user.getPK();
    }
}