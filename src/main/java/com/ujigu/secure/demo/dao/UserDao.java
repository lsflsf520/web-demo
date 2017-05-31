package com.ujigu.secure.demo.dao;

import com.ujigu.secure.db.dao.IBaseDao;
import com.ujigu.secure.demo.entity.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDao extends IBaseDao<Integer, User> {
    int updateByPrimaryKeyWithBLOBs(User record);

    Integer insertReturnPK(User user);
}