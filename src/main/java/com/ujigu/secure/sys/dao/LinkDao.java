package com.ujigu.secure.sys.dao;


import org.springframework.stereotype.Repository;

import com.ujigu.secure.db.dao.BaseDao;
import com.ujigu.secure.sys.entity.Link;

@Repository
public interface LinkDao extends BaseDao<Integer, Link> {
}