package com.ujigu.secure.demo.remote;

import java.util.List;

import com.ujigu.secure.demo.entity.Order;

public interface IOrderService {
	
	public Order loadByUidOrdId(Integer userId, int orderId);
	
	public Integer doSave(Order formData);
	
	public List<Order> loadMyOrders(int userId);

}
