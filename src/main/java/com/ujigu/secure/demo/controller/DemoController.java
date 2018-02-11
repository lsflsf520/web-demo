package com.ujigu.secure.demo.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ujigu.secure.common.bean.ResultModel;
import com.ujigu.secure.common.utils.RandomUtil;
import com.ujigu.secure.demo.entity.Order;
import com.ujigu.secure.demo.remote.IOrderService;

@Controller
@RequestMapping("demo/order")
public class DemoController {
	
	@Resource
	private IOrderService orderService;
	
	@RequestMapping("query")
	@ResponseBody
	public ResultModel queryOrder(int userId, int orderId){
		Order dbData = orderService.loadByUidOrdId(userId, orderId);
		
		return new ResultModel(dbData);
	}
	
	@RequestMapping("queryMyOrders")
	@ResponseBody
	public ResultModel queryMyOrders(int userId){
		List<Order> dbDatas = orderService.loadMyOrders(userId);
		
		return new ResultModel(dbDatas);
	}
	
	@RequestMapping("add")
	@ResponseBody
	public ResultModel addOrder(Order formData){
		formData.setPrice(RandomUtil.rand(10000));
		
		Integer orderId = orderService.doSave(formData);
		
		return new ResultModel(orderId);
	}
	
	

}
