package com.ujigu.secure.sys.controller;


import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.ujigu.secure.common.bean.CommonStatus;
import com.ujigu.secure.common.bean.GlobalConstant;
import com.ujigu.secure.common.bean.PageInfo.OrderDirection;
import com.ujigu.secure.common.bean.ResultModel;
import com.ujigu.secure.common.exception.BaseRuntimeException;
import com.ujigu.secure.common.utils.EncryptTools;
import com.ujigu.secure.sys.entity.Role;
import com.ujigu.secure.sys.entity.Worker;
import com.ujigu.secure.sys.service.RoleMgrService;
import com.ujigu.secure.sys.service.impl.RoleService;
import com.ujigu.secure.sys.service.impl.WorkerService;
import com.ujigu.secure.web.util.UserLoginUtil;


@Controller
@RequestMapping("/sys/worker")
public class WorkerController {

	private final static Logger LOG = LoggerFactory
			.getLogger(WorkerController.class);

	@Resource
	private WorkerService workerService;

	@Resource
	private RoleMgrService roleMgrService;
	
	@RequestMapping("list")
	public ModelAndView userList(Worker worker, Integer pageNum) {

		if(!GlobalConstant.IS_WEB_ADMIN){
			worker.setAcId(UserLoginUtil.getAcId());
			worker.setDepartId(UserLoginUtil.getAdId());
		}
		Page<Worker> workerPager = workerService.findByPage(worker, pageNum == null ? 0 : pageNum, 30, "create_time", OrderDirection.desc);
		
		ModelAndView mav = new ModelAndView("sys/worker");
		mav.addObject("workerPager", workerPager);
		mav.addObject("worker", worker);

		return mav;
	}
	
	@RequestMapping("save")
	@ResponseBody
	public ResultModel add(Worker worker){
		try{
			if(worker.getDepartId() != null && worker.getDepartId() == 0){
				worker.setDepartId(null);
			}
			workerService.canAdd(worker); 
			if(worker.getId() == null){
				if(StringUtils.isNotBlank(worker.getPassword())){
					worker.setPassword(EncryptTools.encrypt(worker.getPassword()));
				}
				worker.setStatus(CommonStatus.NORMAL);
				worker.setCreateTime(new Date());
				workerService.insert(worker);
			} else {
				workerService.update(worker);
			}
		} catch(BaseRuntimeException e){
			return new ResultModel(e.getErrorCode(), e.getFriendlyMsg());
		}
		
		return new ResultModel(true);
	}
	
	@RequestMapping("toedit")
	public ModelAndView toedit(Integer pk){
		ModelAndView mav = new ModelAndView("sys/worker_edit");
		Worker worker = null;
		if(pk != null){
			worker = workerService.findById(pk);
			mav.addObject("worker", worker);
		}
		
//		if(GlobalConstant.IS_WEB_ADMIN){
//			List<AgentCompany> acList = agentCompanyService.findAvailAC();
//			mav.addObject("acList", acList);
//		}else{
//			List<AgentDepart> departs = agentDepartService.findMyDeparts();
//			mav.addObject("departs", departs);
			
			mav.addObject("superAdminRoleId", ((RoleService)roleMgrService).getMySuperAdminRoleId());
			mav.addObject("departOperRoleId", ((RoleService)roleMgrService).getMyDepartOperRoleId());
//		}
		List<Role> roleList = roleMgrService.findAvailRoles();
		mav.addObject("roleList", roleList);
		
		return mav;
	}
	
	@RequestMapping("delete")
	public ModelAndView delete(Integer... pks){
		RoleService roleService = (RoleService)roleMgrService;
		Integer adminRoleId = roleService.getMySuperAdminRoleId();
		Integer departRoleId = roleService.getMyDepartOperRoleId();
		for(Integer pk : pks){
			Worker dbWorker = workerService.findById(pk);
			if(dbWorker == null){
				throw new BaseRuntimeException("NOT_EXIST", "对不起，数据不存在！");
			}
			if(dbWorker.getRoleId() == adminRoleId || dbWorker.getRoleId() == departRoleId){
				throw new BaseRuntimeException("NOT_EXIST", "该账号属于特殊角色，不能删除!");
			}
			Worker worker = new Worker();
			worker.setId(pk);
			worker.setStatus(CommonStatus.DELETED);
			workerService.update(worker);
		}
		
		return new ModelAndView("redirect:list.do");
	}
	
	@RequestMapping("resetPasswd")
	@ResponseBody
	public ResultModel resetPasswd(int workerId, String password){
		if(StringUtils.isBlank(password)){
			return new ResultModel("ILLEGAL_PARAM", "密码不能为空！");
		}
		Worker worker = new Worker();
		worker.setId(workerId);
		worker.setPassword(EncryptTools.encrypt(password));
		
		workerService.update(worker);
		
		return new ResultModel(true);
	}

	/*@RequestMapping("resetPasswd")
	public void resetPasswd(HttpServletRequest request,
			HttpServletResponse response, @RequestParam Long userId,
			@RequestParam String password) {

		boolean result = workerService.resetPasswd(userId, password);
		if (result) {
			WebUtils.writeJson(new ResultModel(GlobalResultCode.SUCCESS),
					request, response);
			return;
		}

		WebUtils.writeJson(new ResultModel(GlobalResultCode.UNKNOWN_ERROR),
				request, response);
	}*/

	/*@RequestMapping("changePwd")
	public void change(HttpServletRequest request, HttpServletResponse response) {

		String oldPassword = WebUtils.getParam(request, "oldpassword", "");

		String newPassword = WebUtils.getParam(request, "newpassword2", "");
		long userId = LoginSesionUtil.getUserId();
		boolean boo = workerService.checkPasswd(userId, oldPassword);
		if (!boo) {
			WebUtils.writeJson(new ResultModel("PWD_ERROR", "旧密码错误,请重新输入"), request, response);
			return;
		}

		boo = workerService.resetPasswd(userId, newPassword);

		WebUtils.writeJson(
				boo ? new ResultModel(GlobalResultCode.SUCCESS) : new ResultModel("PWD_ERROR", "密码修改失败，请重试！"),
				request, response);
	}*/

	// 重置密码
	/*@RequestMapping("resetPwd4JL")
	public void resetPwd2(HttpServletRequest request, HttpServletResponse response) {

		String cardNum = request.getParameter("cardNum");
		// TbStudent tbStudent=studentService.getStudentByCardNum(cardNum);
		TblAuthUser student = userRpcService.getUserInfoBySignName(cardNum);
		if (student != null && student.isStudent()) {
			List<Long> schoolIdList = AclCodeUtil.getSchoolIdListForCurrUser();
			if (schoolIdList == null || schoolIdList.isEmpty()) {
				return;
			}
			com.yisi.stiku.basedata.entity.TbStudent std = studentRpcService.findByStudentId(student.getId());
			boolean boo = schoolIdList.contains(std.getSchoolId());
			if (boo != true) {
				TbSchool school = schoolRpcService.findTbSchoolById(std.getSchoolId());
				WebUtils.writeJsonForJPA(school == null ? "未知学校名称" : school.getName(), request, response);
				return;
			}
			TblAuthUser user = new TblAuthUser();
			user.setPassword("123456");// 这个地方的password用明文即可
			user.setId(student.getId());// 学生的id
			userRpcService.update(user);
			WebUtils.writeJson(1, request, response);
		}
		else {
			WebUtils.writeJson(0, request, response);
		}
	}*/

	/*@RequestMapping("saveUserR2Roles")
	public void saveUserR2Roles(HttpServletRequest request,
			HttpServletResponse response, @RequestParam Long userId) {

		String roleIdStr = request.getParameter("roleIds");
		String[] roleIds = new String[0];
		if (StringUtils.isNotBlank(roleIdStr)) {
			roleIds = roleIdStr.split(",");
		}
		try {
			workerService.updateUser2Role(userId, roleIds);

			WebUtils.writeJson(new ResultModel(GlobalResultCode.SUCCESS),
					request, response);
		} catch (Exception ex) {
			WebUtils.writeJson(new ResultModel("ERROR", "操作失败"),
					request, response);
		}
	}*/

	/*@RequestMapping("kickOff")
	public void kickOff(HttpServletRequest request,
			HttpServletResponse response, long userId) {

		LoginSesionUtil.kickOff(userId);

		WebUtils.writeJson(OperationResult.buildSuccessResult("操作成功！"),
				request, response);
	}*/


	/*@RequestMapping("findByPage")
	public void findByPage(HttpServletRequest request,
			HttpServletResponse response) {

		TblAuthUser authUser = new TblAuthUser();

		// 对应角色列表
		List<Long> usersOfRole = null;
		String userTypeStr = request.getParameter("type");

		String roleTypeStr = request.getParameter("roletype");

		if (StringUtils.isNotBlank(roleTypeStr) && !"-1".equals(roleTypeStr)) {
			usersOfRole = roleMgrRpcService.findUsersByRoles(Integer
					.parseInt(roleTypeStr));
			if (usersOfRole == null || usersOfRole.size() == 0) {
				Page<TblAuthUser> pager = new PageImpl<TblAuthUser>(
						new ArrayList<TblAuthUser>());
				WebUtils.writeJsonForJPA(pager, request, response);
				return;
			}
		}
		if (StringUtils.isNotBlank(userTypeStr) && !"-1".equals(userTypeStr)) {
			authUser.setType(Integer.valueOf(userTypeStr));
		}

		int rows = StringUtils.isBlank(request.getParameter("rows")) ? 20
				: Integer.valueOf(request.getParameter("rows"));

		int currPage = StringUtils.isBlank(request.getParameter("page")) ? 1
				: Integer.valueOf(request.getParameter("page"));

		if (StringUtils.isNotBlank(request.getParameter("keyword"))) {
			currPage = 1;
			rows = Integer.MAX_VALUE;
		}
		
		 * Page<TblAuthUser> pager = userRpcService.searchUser(authUser,
		 * request.getParameter("keyword"), currPage, rows);
		 
		Page<TblAuthUser> pager = userRpcService.searchUserByFilterUsers(
				authUser, usersOfRole, request.getParameter("keyword"),
				currPage, rows);
		try {
			pager.getContent();
		} catch (NullPointerException e) {
			pager = new PageImpl<TblAuthUser>(new ArrayList<TblAuthUser>());
		}

		WebUtils.writeJsonForJPA(pager, request, response);
	}*/

	/*@RequestMapping("saveAcl")
	public void saveAcl(HttpServletRequest request,
			HttpServletResponse response, @RequestParam Long userId) {

		String aclCode = request.getParameter("aclCode");
		// if(StringUtils.isBlank(aclCode)){
		// WebUtils.writeJson(OperationResult.buildFailureResult("对不起，参数错误！"),
		// request, response);
		// return;
		// }

		String errorMsg = "对不起，操作失败！";
		try {
			boolean success = dataPrivRpcService.saveAclCode(userId, aclCode);
			if (success) {
				WebUtils.writeJson(OperationResult.buildSuccessResult("操作成功！"),
						request, response);
				return;
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			errorMsg = e instanceof BaseRuntimeException ? ((BaseRuntimeException) e)
					.getResultCode().getErrorMsg() : errorMsg;
		}

		WebUtils.writeJson(OperationResult.buildFailureResult(errorMsg),
				request, response);

		// User user = this.getEntityService().findOne(userId);
		// user.setAclCode(aclCode);

		// super.doSave(request, response, user);
	}*/

	/*@RequestMapping("getAclCode")
	public void getAclCode(HttpServletRequest request,
			HttpServletResponse response, @RequestParam Long userId) {

		String aclCodeStr = dataPrivRpcService.getAclCodeStr(userId);

		if (StringUtils.isNotBlank(aclCodeStr)) {
			WebUtils.writeJson(
					OperationResult.buildSuccessResult("操作成功！", aclCodeStr),
					request, response);
			return;
		}

		WebUtils.writeJson(
				OperationResult.buildFailureResult("对不起，用户数据权限查询失败！"), request,
				response);
	}*/

	/*@RequestMapping("doSave")
	public void doSave(HttpServletRequest request,
			HttpServletResponse response, TblAuthUser entity) {

		TblAuthUser currUser = userRpcService.getUserInfoById(AuthContextHolder
				.getAuthUserDetails().getUid());
		if (currUser.getType() != null && currUser.getType() == UserType.COACH) {
			List<Long> schoolIds = AclCodeUtil.getSchoolIdListForCurrUser();
			if (schoolIds == null || schoolIds.isEmpty()) {
				WebUtils.writeJsonForJPA(OperationResult
						.buildFailureResult("请先找管理员分配学校，才能执行此操作!"), request,
						response);
				return;
			}
		}
		TblAuthUser pUser = userRpcService.getUserInfoById(entity.getId());
		// if(pUser != null && pUser.getType() != entity.getType()){
		// WebUtils.writeJsonForJPA(OperationResult.buildFailureResult("用户类型不能修改！"),
		// request, response);
		// return;
		// }
		if (pUser == null) {
			pUser = new TblAuthUser();
		} else if (!EntityState.NORMAL.equals(pUser.getDbState())) {
			WebUtils.writeJsonForJPA(
					OperationResult.buildFailureResult("该账号已被禁用，不支持修改"),
					request, response);
			return;
		}
		// User existUser = getEntityService().findByProperty("signName",
		// entity.getSignName());
		TblAuthUser authUser = userRpcService.getUserInfoBySignName(entity
				.getSignName());
		if (authUser != null
				&& (pUser.getId() == null || !authUser.getSignName().equals(
						pUser.getSignName()))) {
			WebUtils.writeJsonForJPA(OperationResult.buildFailureResult("用户名'"
					+ entity.getSignName() + "'已存在，请修改后重试", entity), request,
					response);

			return;
		}
		TblAuthUser saveUser = new TblAuthUser();
		saveUser.setId(pUser.getId());
		saveUser.setEmail(entity.getEmail());
		saveUser.setNick(entity.getRealName());
		saveUser.setSignName(entity.getSignName());
		saveUser.setRealName(entity.getRealName());
		saveUser.setMobile(entity.getMobile());
		// saveUser.setState(EntityState.NORMAL);
		saveUser.setType(entity.getType());
		saveUser.setLastUptime(new Date());
		try {
			userRpcService.save(saveUser);

			// if(entity.getType() == 2 && saveUser.getId() == null){
			// saveUser =
			// userRpcService.getUserInfoBySignName(entity.getSignName());
			//
			// pUser.setId(saveUser.getId());
			// Teacher pTeacher = teacherService.findByUserId(saveUser.getId());
			// if(pTeacher == null){ //如果老师表没有记录才保存
			// Teacher teacher = new Teacher();
			// teacher.setUser(pUser);
			// teacher.setName(entity.getNick());
			// teacherService.save(teacher);
			// }
			// }

		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			WebUtils.writeJson(
					OperationResult
							.buildFailureResult("操作失败! "
									+ (e instanceof BaseRuntimeException ? ((BaseRuntimeException) e)
											.getResultCode().getErrorMsg()
											: "请检查登录账号的长度是否在2到30之间")), request,
					response);
			return;
		}

		WebUtils.writeJsonForJPA(
				OperationResult.buildSuccessResult("数据保存成功", entity), request,
				response);
	}*/

	/*@RequestMapping("resetNormal")
	public void resetNormal(HttpServletRequest request,
			HttpServletResponse response, long userId) {

		boolean canreset = userRpcService.canResetNormal(userId);
		if (!canreset) {
			WebUtils.writeJsonForJPA(OperationResult
					.buildFailureResult("该用户的邮箱或者手机正在被另一账号使用，操作失败！"), request,
					response);
			return;
		}
		TblAuthUser user = new TblAuthUser();
		user.setId(userId);
		user.setDbState(EntityState.NORMAL);

		userRpcService.update(user);

		WebUtils.writeJsonForJPA(OperationResult.buildSuccessResult("操作成功"),
				request, response);
	}*/

	/*@RequestMapping("doDelete")
	public void doDelete(HttpServletRequest request,
			HttpServletResponse response, Long id) {

		TblAuthUser user = new TblAuthUser();
		user.setId(id);
		user.setDbState(EntityState.INVALID);

		userRpcService.update(user);

		WebUtils.writeJsonForJPA(OperationResult.buildSuccessResult("操作成功"),
				request, response);
		LoginSesionUtil.kickOff(id);
	}*/
}
