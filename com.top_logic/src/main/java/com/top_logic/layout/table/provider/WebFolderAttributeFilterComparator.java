/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider;

import java.util.List;

import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.layout.table.filter.FilterComparator;

/**
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class WebFolderAttributeFilterComparator implements FilterComparator {

	/** The {@link WebFolderAttributeFilterComparator} instance. */
	public static final WebFolderAttributeFilterComparator INSTANCE = new WebFolderAttributeFilterComparator();

	@Override
	public int compare(Object o1, Object o2) {
		return WebFolderAttributeComparator.INSTANCE.compare(o1, o2);
	}

	@Override
	public List<Class<?>> getSupportedObjectTypes() {
		return List.of(WebFolder.class);
	}
}