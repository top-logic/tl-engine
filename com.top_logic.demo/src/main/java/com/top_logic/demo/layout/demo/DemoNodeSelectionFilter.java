/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.demo;

import java.io.Serializable;

import com.top_logic.basic.col.Filter;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;
import com.top_logic.layout.tree.model.TLTreeNode;

/**
 * {@link Filter} that accepts {@link DemoTreeNodeModel#isSelectable() selectable}
 * {@link DefaultMutableTLTreeNode}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DemoNodeSelectionFilter implements Serializable,Filter {
	
	public static final DemoNodeSelectionFilter INSTANCE = 
		new DemoNodeSelectionFilter();
	
	private DemoNodeSelectionFilter() {
		// Singleton constructor.
	}

	@Override
	public boolean accept(Object anObject) {
		return ((DemoTreeNodeModel) ((TLTreeNode<?>) anObject).getBusinessObject()).isSelectable();
	}

}
