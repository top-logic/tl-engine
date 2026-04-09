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
import java.util.stream.Collectors;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.util.Utils;
import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutComponent.Config;
import com.top_logic.mig.html.layout.LayoutConfigTreeNode;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundLayout;
import com.top_logic.tool.boundsec.SecurityObjectProviderConfig;
import com.top_logic.tool.boundsec.WithSecurityMaster;
import com.top_logic.tool.boundsec.compound.CompoundSecurityLayout;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.PersBoundComp;
import com.top_logic.util.Resources;
import com.top_logic.util.model.ModelService;

/**
 * {@link SecurityNode} representing a {@link LayoutConfigTreeNode}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConfigNode extends SecurityNode {

	static final String GLOBAL_DOMAIN = "SecurityStructure";

	private List<BoundCommandGroup> _commandGroups;

	private Set<TLClass> _securityObjectTypes;

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

	CompoundSecurityLayout.Config securityLayout() {
		Config layoutConf = config();
		if (layoutConf instanceof CompoundSecurityLayout.Config securityLayout) {
			return layoutNode().getModel().getSecurityDefiningLayout(securityLayout);
		}
		return null;
	}

	LayoutComponent.Config config() {
		return layoutNode().getConfig();
	}

	LayoutConfigTreeNode layoutNode() {
		return (LayoutConfigTreeNode) getBusinessObject();
	}

	boolean hasRight(BoundedRole role, BoundCommandGroup group) {
		PersBoundComp secComp = layoutNode().getSecurityComponent();
		if (secComp == null) {
			return false;
		}
		return secComp.rolesForCommandGroup(group).contains(role);
	}

	void storeRight(BoundedRole role, BoundCommandGroup group, boolean hasRight) {
		PersBoundComp secComp = layoutNode().getSecurityComponent();
		if (secComp == null) {
			return;
		}
		storeLocalRights(secComp, role, group, hasRight);
	}

	private void storeLocalRights(PersBoundComp persBoundComp, BoundedRole role, BoundCommandGroup group,
			boolean hasRight) {
		if (hasRight) {
			persBoundComp.addAccess(group, role);
		} else {
			persBoundComp.removeAccess(group, role);
		}
	}

	List<BoundCommandGroup> getCommandGroups() {
		if (_commandGroups == null) {
			_commandGroups = CommandGroupCollector.collectNonSystemGroups(layoutNode());
		}
		return _commandGroups;
	}

	Set<TLClass> getSecurityObjectTypes() {
		if (_securityObjectTypes == null) {
			_securityObjectTypes =
				findSecurityObjectTypes(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config());
		}
		return _securityObjectTypes;
	}

	private Set<TLClass> findSecurityObjectTypes(InstantiationContext context, LayoutComponent.Config config) {
		if (config instanceof BoundLayout.Config boundLayoutConfig) {
			Set<Config> stopConfigs = getChildren().stream()
				.filter(ConfigNode.class::isInstance)
				.map(ConfigNode.class::cast)
				.map(ConfigNode::config)
				.collect(Collectors.toSet());
			WithSecurityMaster securityMaster = findSecurityMaster(boundLayoutConfig, stopConfigs);
			if (securityMaster != null) {
				return securityMaster.resolveSecurityObject(context).getPossibleSecurityObjectTypes();
			}
			List<? extends Config> childConfigurations = boundLayoutConfig.getChildConfigurations();
			if (childConfigurations.size() == 1) {
				Config child = childConfigurations.get(0);
				if (child instanceof SecurityObjectProviderConfig) {
					// See logic in BoundLayout#hideReason.
					return findSecurityObjectTypes(context, child);
				}
			}
		}
		if (config instanceof SecurityObjectProviderConfig withSecurityObjectProvider) {
			return withSecurityObjectProvider.resolveSecurityObject(context).getPossibleSecurityObjectTypes();
		}
		return Collections.emptySet();
	}

	private WithSecurityMaster findSecurityMaster(BoundLayout.Config boundLayoutConfig,
			Set<Config> stopConfigs) {
		List<? extends Config> children = boundLayoutConfig.getChildConfigurations();
		List<BoundLayout.Config> childBoundLayouts = new ArrayList<>();
		for (Config child : children) {
			if (stopConfigs.contains(child)) {
				continue;
			}
			if (child instanceof WithSecurityMaster securityMaster) {
				if (securityMaster.getIsSecurityMaster()) {
					return securityMaster;
				}
			}
			if (child instanceof BoundLayout.Config childBoundLayout) {
				childBoundLayouts.add(childBoundLayout);
			}
		}
		for (BoundLayout.Config childBoundLayout : childBoundLayouts) {
			WithSecurityMaster securityMaster = findSecurityMaster(childBoundLayout, stopConfigs);
			if (securityMaster != null) {
				return securityMaster;
			}
		}
		return null;
	}

	private String domain() {
		return findDomain(layoutNode());
	}

	boolean needsCheckBox(BoundedRole role) {
		Set<TLClass> securityObjectTypes = getSecurityObjectTypes();
		AccessManager accessManager = AccessManager.getInstance();
		for (TLClass type : securityObjectTypes) {
			if (accessManager.canHaveRole(type, role)) {
				return true;
			}
		}
		return false;
	}

	@Override
	List<SecurityNode> createChildList(SecurityTreeTableBuilder builder) {
		LayoutConfigTreeNode bo = layoutNode();
		List<LayoutConfigTreeNode> children = filterChildren(bo);
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

	private List<LayoutConfigTreeNode> filterChildren(LayoutConfigTreeNode bo) {
		List<LayoutConfigTreeNode> children = new ArrayList<>();
		fillChildren(bo, children);
		return children;
	}

	private void fillChildren(LayoutConfigTreeNode configNode, List<LayoutConfigTreeNode> children) {
		for (LayoutConfigTreeNode child : configNode.getChildren()) {
			if (accept(child)) {
				children.add(child);
			} else {
				fillChildren(child, children);
			}
		}
	}

	private boolean accept(LayoutConfigTreeNode child) {
		LayoutComponent.Config bo = child.getConfig();
		return bo instanceof CompoundSecurityLayout.Config;
	}

	private String findDomain(LayoutConfigTreeNode child) {
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

	/**
	 * Access to the security domain for this node.
	 */
	public static class DomainAccessor extends ReadOnlyAccessor<ConfigNode> {

		@Override
		public Object getValue(ConfigNode object, String property) {
			TLModel model = ModelService.getApplicationModel();
			return model.getModule(object.domain());
		}

	}
}
