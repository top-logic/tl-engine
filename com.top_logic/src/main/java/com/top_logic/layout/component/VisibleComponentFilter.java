/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component;

import com.top_logic.basic.col.Filter;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * The class {@link VisibleComponentFilter} accepted exactly {@link LayoutComponent}s which are visible.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class VisibleComponentFilter implements Filter {
	
	public static final VisibleComponentFilter INSTANCE = new VisibleComponentFilter();
	
	private VisibleComponentFilter() {
		// sole instance
	}

	@Override
	public boolean accept(Object anObject) {
		return (anObject instanceof LayoutComponent) && ((LayoutComponent) anObject).isVisible();
	}

}

