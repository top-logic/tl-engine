/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.structured.util;

import java.util.Iterator;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.TreeView;
import com.top_logic.element.structured.StructuredElement;

/**
 * TreeView for StructuredElements.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class StructuredElementTreeView implements TreeView {

    public static final StructuredElementTreeView INSTANCE = new StructuredElementTreeView();

    @Override
	public Iterator<?> getChildIterator(Object node) {
        return CollectionUtil.getIterator((node instanceof StructuredElement) ? ((StructuredElement)node).getChildren() : null);
    }

    @Override
	public boolean isLeaf(Object node) {
		return !(node instanceof StructuredElement) || ((StructuredElement) node).hasChildren();
    }

	@Override
	public boolean isFinite() {
		return true;
	}

}
