/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider;

import java.util.Comparator;

/**
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class WebFolderAttributeComparator implements Comparator<Object> {

	/** The {@link WebFolderAttributeComparator} instance. */
	public static final WebFolderAttributeComparator INSTANCE = new WebFolderAttributeComparator();

	@Override
	public int compare(Object o1, Object o2) {
		return Integer.compare(getCount(o1), getCount(o2));
	}

	protected int getCount(Object aValue) {
		return WebFolderTableUtil.getWebFolderCount(aValue);
	}
}