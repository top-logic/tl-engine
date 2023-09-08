/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.gui.profile;

import java.util.List;

import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.mig.html.layout.LayoutConfigTreeNode;
import com.top_logic.tool.boundsec.BoundCommandGroup;

/**
 * {@link TreeBuilder} creating the {@link SecurityNode}s for the
 * {@link EditSecurityProfileComponent}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class SecurityTreeTableBuilder implements TreeBuilder<SecurityNode> {

	private final String _domain;

	public SecurityTreeTableBuilder(String domain) {
		_domain = domain;
	}

	@Override
	public SecurityNode createNode(AbstractMutableTLTreeModel<SecurityNode> model, SecurityNode parent,
			Object userObject) {
		if (userObject instanceof BoundCommandGroup) {
			return new CommandNode(model, parent, (BoundCommandGroup) userObject);
		} else {
			return new ConfigNode(model, parent, (LayoutConfigTreeNode) userObject);
		}
	}

	@Override
	public List<SecurityNode> createChildList(SecurityNode node) {
		return node.createChildList(this);
	}

	@Override
	public boolean isFinite() {
		return true;
	}

	String securityDomain() {
		return _domain;
	}

}
