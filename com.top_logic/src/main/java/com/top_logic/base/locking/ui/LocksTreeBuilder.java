/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.locking.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.base.locking.token.LockInfo;
import com.top_logic.base.locking.token.TokenService;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableBuilder;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableNode;

/**
 * Builder for the lock administration view showing all created locks.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LocksTreeBuilder extends DefaultTreeTableBuilder {

	/**
	 * Singleton {@link LocksTreeBuilder} instance.
	 */
	public static final LocksTreeBuilder INSTANCE = new LocksTreeBuilder();

	private LocksTreeBuilder() {
		// Singleton constructor.
	}

	@Override
	public List<DefaultTreeTableNode> createChildList(DefaultTreeTableNode node) {
		Object businessObject = node.getBusinessObject();
		if (businessObject == null) {
			return wrap(node, TokenService.getInstance().getAllLocks());
		}
		if (businessObject instanceof LockInfo) {
			return wrap(node, ((LockInfo) businessObject).getTokens());
		}
		return super.createChildList(node);
	}

	private List<DefaultTreeTableNode> wrap(DefaultTreeTableNode parent, Collection<?> objs) {
		ArrayList<DefaultTreeTableNode> result = new ArrayList<>(objs.size());
		for (Object obj : objs) {
			result.add(createNode(parent.getModel(), parent, obj));
		}
		return result;
	}
}
