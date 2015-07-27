package com.techstack.pms.dao.jpa.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "PMS_ACTION")
public class Action extends BaseEntity {

	@Column(name="ACTION_NAME")
	private String actionName; 

	/** Permission identification */
	@Column(name="ACTION")
	private String action;
	
	@Column(name="REMARK")
	private String remark;
	
	@ManyToMany(mappedBy = "actions")
	private List<Role> roles = new ArrayList<Role>();

	@OneToOne
	@JoinColumn(name = "MENU_ID", referencedColumnName = "ID")
	private Menu relevantMenu;
	
	@Column(name="MENU_NAME")
	private String menuName;

	public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Menu getRelevantMenu() {
		return relevantMenu;
	}

	public void setRelevantMenu(Menu relevantMenu) {
		this.relevantMenu = relevantMenu;
	}

	public String getMenuName() {
		return menuName;
	}

	public void setMenuName(String menuName) {
		this.menuName = menuName;
	}

	public List<Role> getRoles() {
		return roles;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}

}
