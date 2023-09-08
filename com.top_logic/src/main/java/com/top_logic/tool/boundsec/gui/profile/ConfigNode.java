/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.gui.profile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.Utils;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutConfigTreeNode;
import com.top_logic.model.TLModule;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.compound.CompoundSecurityLayout;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.PersBoundComp;
import com.top_logic.util.Resources;

/**
 * {@link SecurityNode} representing a {@link LayoutConfigTreeNode}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConfigNode extends SecurityNode {

	static final String GLOBAL_DOMAIN = "SecurityStructure";

	private List<BoundCommandGroup> _commandGroups;

	/**
	 * Creates a new {@link ConfigNode}.
	 */
	public ConfigNode(AbstractMutableTLTreeModel<SecurityNode> model, SecurityNode parent,
			LayoutConfigTreeNode businessObject) {
		super(model, parent, businessObject);
	}

	@Override
	String getLabel(Resources resources) {
		if (this == getModel().getRoot()) {
			return StringServices.EMPTY_STRING;
		}
		return resources.getString(LayoutComponent.Config.getEffectiveTitleKey(config()));
	}

	@Override
	public ConfigNode getParent() {
		return (ConfigNode) super.getParent();
	}

	LayoutComponent.Config config() {
		return layoutNode().getConfig();
	}

	LayoutConfigTreeNode layoutNode() {
		return (LayoutConfigTreeNode) getBusinessObject();
	}

	boolean hasRight(Set<BoundedRole> set, BoundCommandGroup group) {
		PersBoundComp secComp = layoutNode().getSecurityComponent();
		if (secComp == null) {
			return false;
		}
		Collection<?> roles = secComp.rolesForCommandGroup(group);
		return CollectionUtil.containsAny(set, roles);
	}

	void storeRight(Set<BoundedRole> roles, BoundCommandGroup group, boolean hasRight) {
		PersBoundComp secComp = layoutNode().getSecurityComponent();
		if (secComp == null) {
			return;
		}
		boolean changes = storeLocalRights(secComp, roles, group, hasRight);
		if (changes) {
			CommandGroupDistributor.distributeNeededRolesToChildren(layoutNode());
		}

	}

	private boolean storeLocalRights(PersBoundComp persBoundComp, Set<BoundedRole> roles, BoundCommandGroup group,
			boolean hasRight) {
		boolean changes = false;
		for (BoundedRole role : roles) {
			if (!isRoleRelevantForDomain(role)) {
				continue;
			}
			if (hasRight) {
				persBoundComp.addAccess(group, role);
			} else {
				persBoundComp.removeAccess(group, role);
			}
			changes = true;
		}
		return changes;
	}

	List<BoundCommandGroup> getCommandGroups() {
		if (_commandGroups == null) {
			_commandGroups = CommandGroupCollector.collectNonSystemGroups(layoutNode());
		}
		return _commandGroups;
	}

	boolean isRoleRelevantForDomain(BoundedRole role) {
		return isRoleForDomain(role, domain());
	}

	private String domain() {
		return findDomain(layoutNode());
	}

	private boolean isRoleForDomain(BoundedRole role, String domain) {
		TLModule scope = role.getScope();
		if (scope == null) {
			return false;
		}
		return scope.getName().equals(domain);
	}

	boolean needsCheckBox(Set<BoundedRole> set) {
		String domain = domain();
		return set.stream()
			.filter(role -> isRoleForDomain(role, domain))
			.findFirst()
			.isPresent();
	}

	@Override
	List<SecurityNode> createChildList(SecurityTreeTableBuilder builder) {
		LayoutConfigTreeNode bo = layoutNode();
		List<LayoutConfigTreeNode> children = filterChildren(bo, builder.securityDomain());
		List<SecurityNode> result = new ArrayList<>();
		if (children.isEmpty()) {
			addChildren(builder, result, getCommandGroups());
		} else {
			addCommandsForNonLeafNodes(builder, result, children);
			addChildren(builder, result, children);
		}
		return result;
	}

	private void addCommandsForNonLeafNodes(SecurityTreeTableBuilder builder, List<SecurityNode> newChildren,
			Collection<? extends LayoutConfigTreeNode> layoutChildren) {
		List<BoundCommandGroup> allCommandGroups = getCommandGroups();
		List<?> filteredGroups;
		if (onlyDialogChildren(layoutChildren)) {
			// Always show all commands
			filteredGroups = allCommandGroups;
		} else {
			/* In this compound security layout there is no component with special commands. Do not
			 * show command groups */
			if (allCommandGroups.size() == 1 && SimpleBoundCommandGroup.READ.equals(allCommandGroups.get(0))) {
				filteredGroups = Collections.emptyList();
			} else {
				filteredGroups = allCommandGroups;
			}
		}
		addChildren(builder, newChildren, filteredGroups);
	}

	private boolean onlyDialogChildren(Collection<? extends LayoutConfigTreeNode> layoutChildren) {
		LayoutConfigTreeNode dialogParent = layoutNode().getDialogParent();
		for (LayoutConfigTreeNode potentialChildNode : layoutChildren) {
			LayoutConfigTreeNode childDialog = potentialChildNode.getDialogParent();
			if (Utils.equals(childDialog, dialogParent)) {
				/* There is a <b>real</b> child, not just dialogs. */
				return false;
			}
		}
		/* The only children of the given layout node are dialogs, therefore the config is actually
		 * a leaf node */
		return true;
	}

	private void addChildren(SecurityTreeTableBuilder builder, List<SecurityNode> newChildren,
			List<?> businessObjects) {
		businessObjects.stream()
			.map(childBO -> builder.createNode(getModel(), this, childBO))
			.forEach(newNode -> newChildren.add(newNode));
	}

	private List<LayoutConfigTreeNode> filterChildren(LayoutConfigTreeNode bo, String securityDomain) {
		List<LayoutConfigTreeNode> children = new ArrayList<>();
		fillChildren(bo, securityDomain, children);
		return children;
	}

	private void fillChildren(LayoutConfigTreeNode configNode, String domain,
			List<LayoutConfigTreeNode> children) {
		for (LayoutConfigTreeNode child : configNode.getChildren()) {
			if (accept(child, domain)) {
				children.add(child);
			} else {
				fillChildren(child, domain, children);
			}
		}
	}

	private boolean accept(LayoutConfigTreeNode child, String domain) {
		LayoutComponent.Config bo = child.getConfig();
		if (!(bo instanceof CompoundSecurityLayout.Config)) {
			return false;
		}
		return domain.equals(findDomain(child));
	}

	String findDomain(LayoutConfigTreeNode child) {
		child = CompoundSecurityLayoutConfigDescender.findCompoundSecurityNode(child);
		if (child == null) {
			return ConfigNode.GLOBAL_DOMAIN;
		}
		CompoundSecurityLayout.Config conf = (CompoundSecurityLayout.Config) child.getConfig();
		String securityDomain = conf.getSecurityDomain();
		if (securityDomain != null) {
			return securityDomain;
		}
		return findDomain(child.getParent());
	}

}
