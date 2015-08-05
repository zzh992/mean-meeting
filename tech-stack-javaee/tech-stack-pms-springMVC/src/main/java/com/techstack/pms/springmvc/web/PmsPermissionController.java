package com.techstack.pms.springmvc.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.techstack.component.dwz.DwzUtils;
import com.techstack.component.mapper.BeanMapper;
import com.techstack.component.springmvc.SpringMVCBaseController;
import com.techstack.pms.biz.PmsActionBiz;
import com.techstack.pms.biz.PmsMenuBiz;
import com.techstack.pms.biz.PmsRoleBiz;
import com.techstack.pms.dao.dto.PmsActionDTO;
import com.techstack.pms.dao.dto.PmsMenuDTO;
import com.techstack.pms.dao.dto.PmsRoleDTO;
import com.techstack.pms.dao.mybatis.entity.PmsMenu;

@Controller
@RequestMapping("/pmsPermission_")
public class PmsPermissionController extends SpringMVCBaseController{

	private static Log log = LogFactory.getLog(PmsPermissionController.class);

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
	//@Permission("pms:action:view")
	@RequestMapping("pmsActionList.action")
	public ModelAndView pmsActionList() {
		try {
			ModelAndView mav = new ModelAndView("page/pms/pmsMenu/pmsMenuList.jsp");
			ModelMap modelMap = new ModelMap();
			Map<String, Object> paramMap = new HashMap<String, Object>(); // 业务条件查询参数
			paramMap.put("actionName", getString("actionName")); // 权限名称（模糊查询）
			paramMap.put("action", getString("act")); // 权限（精确查询）
			paramMap.put("act", getString("act"));
			paramMap.put("module", "pmsAction");
			Page<PmsActionDTO> pageBean = pmsActionBiz.listPage(DwzUtils.getPageNum(getHttpRequest()), DwzUtils.getNumPerPage(getHttpRequest()), paramMap);
			modelMap.putAll(BeanMapper.map(pageBean, Map.class));
			modelMap.putAll(BeanMapper.map(paramMap, Map.class));
			//this.pushData(pageBean);
			//this.pushData(paramMap); // 回显查询条件值
			mav.addAllObjects(modelMap);
			return mav;
		} catch (Exception e) {
			log.error("==== error ==== 查询权限失败：", e);
			return DwzUtils.operateErrorInSpringMVC("获取数据失败", getHttpRequest());
		}
	}

	/**
	 * @Description: 进入添加Pms权限页面 
	 * @param @return    
	 * @return String
	 */
	//@Permission("pms:action:add")
	@RequestMapping("pmsActionAdd.action")
	public String pmsActionAdd() {
		return "page/pms/pmsAction/pmsActionAdd.jsp";
	}

	/**
	 * @Description: 将权限信息保存到数据库中
	 * @param @return    
	 * @return String
	 */
	//@Permission("pms:action:add")
	@RequestMapping("pmsActionSave.action")
	public ModelAndView pmsActionSave() {
		try {
			String actionName = getString("actionName"); // 权限名称
			String action = getString("action"); // 权限标识
			String desc = getString("desc"); // 权限描述
			Long menuId = getLong("menu.id"); // 权限关联的菜单ID
			String menuName = getString("menu.name");	//权限关联的菜单名称
			// 权限
			PmsActionDTO act = new PmsActionDTO();
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
				return DwzUtils.operateErrorInSpringMVC(validateMsg, getHttpRequest()); // 返回错误信息
			}
			// 检查权限名称是否已存在
			PmsActionDTO checkName = pmsActionBiz.getByActionName(actionName.trim());
			if (checkName != null) {
				return DwzUtils.operateErrorInSpringMVC("权限名称【" + actionName + "】已存在", getHttpRequest());
			}
			// 检查权限是否已存在
			PmsActionDTO checkAction = pmsActionBiz.getByAction(action.trim());
			if (checkAction != null) {
				return DwzUtils.operateErrorInSpringMVC("权限【" + action + "】已存在", getHttpRequest());
			}

			pmsActionBiz.saveAction(act);
			log.info("==== info ==== 权限【"+action+"】添加成功");
			return DwzUtils.operateErrorInSpringMVC("操作成功", getHttpRequest()); // 返回operateSuccess视图,并提示“操作成功”
		} catch (Exception e) {
			log.error("==== error ==== 权限添加失败", e);
			return DwzUtils.operateErrorInSpringMVC("保存失败", getHttpRequest());
		}
	}

	/**
	 * @Description: 添加或修改权限时，查找带回权限要关联的菜单ID.
	 * @param @return    
	 * @return String
	 */
	@RequestMapping("pmsMenuLookUpUI.action")
	public ModelAndView pmsMenuLookUpUI() {
		ModelAndView mav = new ModelAndView("page/pms/pmsAction/pmsMenuLookUp.jsp");
		ModelMap modelMap = new ModelMap();
		modelMap.put("tree", pmsMenuBiz.buildLookUpMenu());
		mav.addAllObjects(modelMap);
		return mav;
	}

	/**
	 * @Description: 校验Pms权限信息
	 * @param @param pmsAction
	 * @param @return    
	 * @return String
	 */
	private String validatePmsAction(PmsActionDTO pmsAction) {
		String msg = ""; // 用于存放校验提示信息的变量
		String actionName = pmsAction.getActionName(); // 权限名称
		String action = pmsAction.getAction(); // 权限标识
		String desc = pmsAction.getRemark(); // 权限描述
		// 权限名称 actionName
		msg += DwzUtils.lengthValidate("权限名称", actionName, true, 3, 90);
		// 权限标识 action
		msg += DwzUtils.lengthValidate("权限标识", action, true, 3, 100);
		// 描述 desc
		msg += DwzUtils.lengthValidate("描述", desc, true, 3, 60);
		// 校验菜单ID是否存在
		if (null != pmsAction.getMenuId()) {
			PmsMenuDTO menu = pmsMenuBiz.getById(pmsAction.getMenuId());
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
	//@Permission("pms:action:edit")
	@RequestMapping("pmsActionEdit.action")
	public ModelAndView pmsActionEdit() {
		ModelAndView mav = new ModelAndView("page/pms/pmsAction/pmsActionEdit.jsp");
		ModelMap modelMap = new ModelMap();
		try {
			Long id = getLong("id");
			PmsActionDTO pmsAction = pmsActionBiz.getById(id);
			modelMap.put("pmsAction", pmsAction);
			mav.addAllObjects(modelMap);
			return mav;
		} catch (Exception e) {
			log.error("==== error ==== 进入权限修改页面失败：", e);
			return DwzUtils.operateErrorInSpringMVC("获取数据失败", getHttpRequest());
		}
	}

	/**
	 * @Description: 保存修改后的权限信息
	 * @param @return    
	 * @return String
	 */
	//@Permission("pms:action:edit")
	@RequestMapping("pmsActionUpdate.action")
	public ModelAndView pmsActionUpdate() {
		try {
			Long id = getLong("actionId");
			PmsActionDTO pmsAction = pmsActionBiz.getById(id);
			if (pmsAction == null) {
				return DwzUtils.operateErrorInSpringMVC("无法获取要修改的数据", getHttpRequest());
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
					return DwzUtils.operateErrorInSpringMVC(validateMsg, getHttpRequest()); // 返回错误信息
				}

				// 检查权限名称是否已存在
				PmsActionDTO checkName = pmsActionBiz.getByActionNameNotEqId(actionName, id);
				if (checkName != null) {
					return DwzUtils.operateErrorInSpringMVC("权限名称【" + actionName + "】已存在", getHttpRequest());
				}
				// 检查权限是否已存在
				// PmsAction checkAction =
				// pmsActionBiz.getByActionNotEqId(action, id);
				// if (checkAction != null){
				// return operateError("权限【"+action+"】已存在");
				// }

				pmsActionBiz.updateAction(pmsAction);
				log.info("==== info ==== 权限【"+actionName+"】修改成功");
				return DwzUtils.operateErrorInSpringMVC("操作成功", getHttpRequest());
			}
		} catch (Exception e) {
			log.error("==== error ==== 权限修改失败", e);
			return DwzUtils.operateErrorInSpringMVC("修改失败", getHttpRequest());
		}
	}

	/**
	 * @Description: 删除一条权限记录
	 * @param @return    
	 * @return String
	 */
	//@Permission("pms:action:delete")
	@RequestMapping("pmsActionDel.action")
	public ModelAndView pmsActionDel() {
		try {
			Long actionId = getLong("id");
			PmsActionDTO act = pmsActionBiz.getById(actionId);
			if (act == null) {
				return DwzUtils.operateErrorInSpringMVC("无法获取要删除的数据", getHttpRequest());
			}
			// 判断此权限是否关联有角色，要先解除与角色的关联后才能删除该权限
			List<PmsRoleDTO> roleList = pmsRoleBiz.listByActionId(actionId);
			if (roleList != null && !roleList.isEmpty()) {
				return DwzUtils.operateErrorInSpringMVC("权限【" + act.getAction() + "】关联了【" + roleList.size() + "】个角色，要解除所有关联后才能删除。其中一个角色名为:" + roleList.get(0).getRoleName(), getHttpRequest());
			}
			pmsActionBiz.deleteActionById(actionId);
			log.info("==== info ==== 删除权限【"+act.getAction()+"】成功");
			return DwzUtils.operateErrorInSpringMVC("操作成功", getHttpRequest()); // 返回operateSuccess视图,并提示“操作成功”
		} catch (Exception e) {
			log.error("==== error ==== 删除权限失败", e);
			return DwzUtils.operateErrorInSpringMVC("删除限权异常", getHttpRequest());
		}
	}

}