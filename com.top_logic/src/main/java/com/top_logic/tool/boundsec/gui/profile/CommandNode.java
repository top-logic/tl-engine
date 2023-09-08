/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.gui.profile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.util.Resources;

/**
 * {@link SecurityNode} representing a {@link BoundCommandGroup}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class CommandNode extends SecurityNode {

	/**
	 * Creates a new {@link CommandNode}.
	 */
	public CommandNode(AbstractMutableTLTreeModel<SecurityNode> model, SecurityNode parent,
			BoundCommandGroup businessObject) {
		super(model, parent, businessObject);
	}

	@Override
	List<SecurityNode> createChildList(SecurityTreeTableBuilder builder) {
		return Collections.emptyList();
	}

	/**
	 * Type-safe access to the {@link BoundCommandGroup}.
	 * 
	 * @see #getBusinessObject()
	 */
	BoundCommandGroup group() {
		return (BoundCommandGroup) getBusinessObject();
	}

	@Override
	String getLabel(Resources resources) {
		String id = group().getID();
		ResPrefix prefix = I18NConstants.COMMAND_GROUP;
		ResKey key = prefix.append(id).key(".toolTip");
		return resources.getString(key, resources.getString(prefix.key(id), id));
	}

	boolean hasRight(Set<BoundedRole> set) {
		return configNode().hasRight(set, group());
	}

	ConfigNode configNode() {
		return (ConfigNode) getParent();
	}

	String getRoleNamesAsTooltip(Set<BoundedRole> colRoles) {
		ArrayList<BoundedRole> roles = new ArrayList<>(colRoles);
		Collections.sort(roles);

		StringBuilder res = new StringBuilder();
		Resources resources = Resources.getInstance();
		for (BoundedRole role : roles) {
			if (!configNode().isRoleRelevantForDomain(role)) {
				continue;
			}
			ResKey securityStructureKey =
				TLModelNamingConvention.getModuleLabelKey(role.getScope());
			String label = resources.getString(securityStructureKey);
			res.append(label).append("<br/>");
		}
		return res.toString();
	}

}
