package com.ujigu.secure.sys.dao;
import org.springframework.stereotype.Repository;

import com.ujigu.secure.db.dao.BaseDao;
import com.ujigu.secure.sys.entity.Worker;

@Repository
public interface WorkerDao extends BaseDao<Integer, Worker> {
}