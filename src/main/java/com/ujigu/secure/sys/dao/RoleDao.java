package com.ujigu.secure.sys.dao;

import org.springframework.stereotype.Repository;

import com.ujigu.secure.db.dao.BaseDao;
import com.ujigu.secure.sys.entity.Role;

@Repository
public interface RoleDao extends BaseDao<Integer, Role> {
}