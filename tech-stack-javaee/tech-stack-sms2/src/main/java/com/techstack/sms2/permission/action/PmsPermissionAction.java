package com.techstack.sms2.permission.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.techstack.sms2.base.annotation.permission.Permission;
import com.techstack.sms2.base.page.PageBean;
import com.techstack.sms2.base.struts.BaseAction;
import com.techstack.sms2.permission.biz.PmsActionBiz;
import com.techstack.sms2.permission.biz.PmsMenuBiz;
import com.techstack.sms2.permission.biz.PmsRoleBiz;
import com.techstack.sms2.permission.entity.PmsAction;
import com.techstack.sms2.permission.entity.PmsMenu;
import com.techstack.sms2.permission.entity.PmsRole;

/**
 * @Title: PmsPermissionAction.java 
 * @Description: 权限管理Action类
 * @author zzh
 */
public class PmsPermissionAction extends BaseAction {

	private static final long serialVersionUID = 5588682213578275029L;

	private static Log log = LogFactory.getLog(PmsPermissionAction.class);

	@Autowired
	private PmsActionBiz pmsActionBiz;
	@Autowired
	private PmsRoleBiz pmsRoleBiz;
	@Autowired
	private PmsMenuBiz pmsMenuBiz;

	/**
	 * @Description: 分页列出pms权限，也可根据权限获权限名称进行查询.
	 * @param @return    
	 * @return String
	 */
	@Permission("pms:action:view")
	public String pmsActionList() {
		try {
			Map<String, Object> paramMap = new HashMap<String, Object>(); // 业务条件查询参数
			paramMap.put("actionName", getString("actionName")); // 权限名称（模糊查询）
			paramMap.put("action", getString("act")); // 权限（精确查询）
			paramMap.put("act", getString("act"));
			paramMap.put("module", "pmsAction");
			PageBean pageBean = pmsActionBiz.listPage(getPageParam(), paramMap);
			this.pushData(pageBean);
			this.pushData(paramMap); // 回显查询条件值
			return "pmsActionList";
		} catch (Exception e) {
			log.error("==== error ==== 查询权限失败：", e);
			return operateError("获取数据失败");
		}
	}

	/**
	 * @Description: 进入添加Pms权限页面 
	 * @param @return    
	 * @return String
	 */
	@Permission("pms:action:add")
	public String pmsActionAdd() {
		return "pmsActionAdd";
	}

	/**
	 * @Description: 将权限信息保存到数据库中
	 * @param @return    
	 * @return String
	 */
	@Permission("pms:action:add")
	public String pmsActionSave() {
		try {
			String actionName = getString("actionName"); // 权限名称
			String action = getString("action"); // 权限标识
			String desc = getString("desc"); // 权限描述
			Long menuId = getLong("menu.id"); // 权限关联的菜单ID
			String menuName = getString("menu.name");	//权限关联的菜单名称
			// 权限
			PmsAction act = new PmsAction();
			act.setActionName(actionName);
			act.setAction(action);
			act.setRemark(desc);
			// 菜单
			PmsMenu menu = new PmsMenu();
			menu.setId(menuId);
			act.setMenuId(menuId); // 设置菜单ID
			act.setMenuName(menuName);

			// 表单数据校验
			String validateMsg = validatePmsAction(act);
			if (StringUtils.isNotBlank(validateMsg)) {
				return operateError(validateMsg); // 返回错误信息
			}
			// 检查权限名称是否已存在
			PmsAction checkName = pmsActionBiz.getByActionName(actionName.trim());
			if (checkName != null) {
				return operateError("权限名称【" + actionName + "】已存在");
			}
			// 检查权限是否已存在
			PmsAction checkAction = pmsActionBiz.getByAction(action.trim());
			if (checkAction != null) {
				return operateError("权限【" + action + "】已存在");
			}

			pmsActionBiz.saveAction(act);
			log.info("==== info ==== 权限【"+action+"】添加成功");
			return operateSuccess(); // 返回operateSuccess视图,并提示“操作成功”
		} catch (Exception e) {
			log.error("==== error ==== 权限添加失败", e);
			return operateError("保存失败");
		}
	}

	/**
	 * @Description: 添加或修改权限时，查找带回权限要关联的菜单ID.
	 * @param @return    
	 * @return String
	 */
	public String pmsMenuLookUpUI() {
		putData("tree", pmsMenuBiz.buildLookUpMenu());
		return "pmsMenuLookUp";
	}

	/**
	 * @Description: 校验Pms权限信息
	 * @param @param pmsAction
	 * @param @return    
	 * @return String
	 */
	private String validatePmsAction(PmsAction pmsAction) {
		String msg = ""; // 用于存放校验提示信息的变量
		String actionName = pmsAction.getActionName(); // 权限名称
		String action = pmsAction.getAction(); // 权限标识
		String desc = pmsAction.getRemark(); // 权限描述
		// 权限名称 actionName
		msg += lengthValidate("权限名称", actionName, true, 3, 90);
		// 权限标识 action
		msg += lengthValidate("权限标识", action, true, 3, 100);
		// 描述 desc
		msg += lengthValidate("描述", desc, true, 3, 60);
		// 校验菜单ID是否存在
		if (null != pmsAction.getMenuId()) {
			PmsMenu menu = pmsMenuBiz.getById(pmsAction.getMenuId());
			if (menu == null) {
				msg += "，请选择权限关联的菜单";
			}
		} else {
			msg += "，请选择权限关联的菜单";
		}
		return msg;
	}

	/**
	 * @Description: 转到权限修改页面 .
	 * @param @return    
	 * @return String
	 */
	@Permission("pms:action:edit")
	public String pmsActionEdit() {
		try {
			Long id = getLong("id");
			PmsAction pmsAction = pmsActionBiz.getById(id);
			this.putData("pmsAction", pmsAction);
			return "pmsActionEdit";
		} catch (Exception e) {
			log.error("==== error ==== 进入权限修改页面失败：", e);
			return operateError("获取数据失败");
		}
	}

	/**
	 * @Description: 保存修改后的权限信息
	 * @param @return    
	 * @return String
	 */
	@Permission("pms:action:edit")
	public String pmsActionUpdate() {
		try {
			Long id = getLong("actionId");
			PmsAction pmsAction = pmsActionBiz.getById(id);
			if (pmsAction == null) {
				return operateError("无法获取要修改的数据");
			} else {

				String actionName = getString("actionName");
				// String action = getString("action");
				String desc = getString("desc");

				pmsAction.setActionName(actionName);
				// pmsAction.setAction(action);
				pmsAction.setRemark(desc);

				// 表单数据校验
				String validateMsg = validatePmsAction(pmsAction);
				if (StringUtils.isNotBlank(validateMsg)) {
					return operateError(validateMsg); // 返回错误信息
				}

				// 检查权限名称是否已存在
				PmsAction checkName = pmsActionBiz.getByActionNameNotEqId(actionName, id);
				if (checkName != null) {
					return operateError("权限名称【" + actionName + "】已存在");
				}
				// 检查权限是否已存在
				// PmsAction checkAction =
				// pmsActionBiz.getByActionNotEqId(action, id);
				// if (checkAction != null){
				// return operateError("权限【"+action+"】已存在");
				// }

				pmsActionBiz.updateAction(pmsAction);
				log.info("==== info ==== 权限【"+actionName+"】修改成功");
				return operateSuccess();
			}
		} catch (Exception e) {
			log.error("==== error ==== 权限修改失败", e);
			return operateError("修改失败");
		}
	}

	/**
	 * @Description: 删除一条权限记录
	 * @param @return    
	 * @return String
	 */
	@Permission("pms:action:delete")
	public String pmsActionDel() {
		try {
			Long actionId = getLong("id");
			PmsAction act = pmsActionBiz.getById(actionId);
			if (act == null) {
				return operateError("无法获取要删除的数据");
			}
			// 判断此权限是否关联有角色，要先解除与角色的关联后才能删除该权限
			List<PmsRole> roleList = pmsRoleBiz.listByActionId(actionId);
			if (roleList != null && !roleList.isEmpty()) {
				return operateError("权限【" + act.getAction() + "】关联了【" + roleList.size() + "】个角色，要解除所有关联后才能删除。其中一个角色名为:" + roleList.get(0).getRoleName());
			}
			pmsActionBiz.deleteActionById(actionId);
			log.info("==== info ==== 删除权限【"+act.getAction()+"】成功");
			return operateSuccess(); // 返回operateSuccess视图,并提示“操作成功”
		} catch (Exception e) {
			log.error("==== error ==== 删除权限失败", e);
			return operateError("删除限权异常");
		}
	}

}