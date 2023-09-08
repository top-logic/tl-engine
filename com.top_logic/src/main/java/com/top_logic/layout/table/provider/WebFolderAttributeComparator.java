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

	private final int direction;

	/**
	 * Creates a {@link WebFolderAttributeComparator}.
	 */
	public WebFolderAttributeComparator(int aDirection) {
		this.direction = aDirection;
	}

	@Override
	public int compare(Object o1, Object o2) {
		Integer theVal1 = getCount(o1);
		Integer theVal2 = getCount(o2);

		return this.direction * (theVal1 - theVal2);
	}

	protected Integer getCount(Object aValue) {
		return WebFolderTableUtil.getWebFolderCount(aValue);
	}
}