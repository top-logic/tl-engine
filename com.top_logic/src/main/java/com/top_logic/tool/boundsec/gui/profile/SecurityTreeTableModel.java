/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.gui.profile;

import java.util.List;
import java.util.function.Consumer;

import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.tree.model.AbstractTreeTableModel;
import com.top_logic.layout.tree.model.TreeBuilder;

/**
 * {@link AbstractTreeTableModel} for {@link SecurityNode}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
final class SecurityTreeTableModel extends AbstractTreeTableModel<SecurityNode> {

	private final Consumer<? super SecurityNode> _nodeInitializer;
	
	SecurityTreeTableModel(TreeBuilder<SecurityNode> builder, Object rootUserObject, List<String> columnNames,
			TableConfiguration config, Consumer<? super SecurityNode> nodeInitializer) {
		super(builder, rootUserObject, columnNames, config);
		_nodeInitializer = nodeInitializer;
	}

	@Override
	protected void handleInitNode(SecurityNode node) {
		super.handleInitNode(node);
		_nodeInitializer.accept(node);
	}

}
