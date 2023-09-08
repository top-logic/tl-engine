/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.treeview;

import java.util.List;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.layout.window.WindowComponent;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ComponentTreeViewNoDialogs} that also excludes {@link WindowComponent}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ComponentTreeViewNoDialogsOrWindows extends ComponentTreeViewNoDialogs {

	@Override
	protected List<LayoutComponent> children(LayoutComponent node) {
		Filter<? super Object> onlyWindows = FilterFactory.createClassFilter(WindowComponent.class);
		Filter<? super Object> noWindows = FilterFactory.not(onlyWindows);
		return FilterUtil.filterList(noWindows, super.children(node));
	}

}

