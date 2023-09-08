/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.view.component.property;

import com.top_logic.basic.col.Filter;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.BoundObject;

/**
 * @author     <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class AllowViewFilter implements Filter<Object> {
	
	/** May use this as singleton. */
	public static AllowViewFilter INSTANCE = new AllowViewFilter();

	@Override
	public boolean accept(Object anObject) {
		if (anObject instanceof TLTreeNode) {
			anObject = ((TLTreeNode<?>) anObject).getBusinessObject();
		}
		if (anObject instanceof BoundObject) {
			BoundHelper theBoundHelper = BoundHelper.getInstance();
			return theBoundHelper.allowView((BoundObject) anObject, theBoundHelper.getRootChecker());
		}
		
		return true;
	}
}