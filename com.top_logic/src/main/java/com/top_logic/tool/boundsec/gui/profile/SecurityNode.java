/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.gui.profile;

import java.util.List;

import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.AbstractTreeTableModel.AbstractTreeTableNode;
import com.top_logic.util.Resources;

/**
 * Super class for {@link SecurityNode} used in {@link EditSecurityProfileComponent}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
abstract class SecurityNode extends AbstractTreeTableNode<SecurityNode> {

	public SecurityNode(AbstractMutableTLTreeModel<SecurityNode> model, SecurityNode parent,
			Object businessObject) {
		super(model, parent, businessObject);
	}

	abstract String getLabel(Resources resources);

	abstract List<SecurityNode> createChildList(SecurityTreeTableBuilder builder);

}
