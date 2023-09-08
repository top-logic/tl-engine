/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.layout.table.filter.FilterComparator;

/**
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class WebFolderAttributeFilterComparator implements FilterComparator {

	private final Comparator<Object> inner;

	/**
	 * Creates a {@link WebFolderAttributeFilterComparator}.
	 */
	public WebFolderAttributeFilterComparator() {
		this(new WebFolderAttributeComparator(1));
	}

	/**
	 * Creates a {@link WebFolderAttributeFilterComparator}.
	 */
	public WebFolderAttributeFilterComparator(Comparator<Object> aComparator) {
		this.inner = aComparator;
	}

	@Override
	public int compare(Object o1, Object o2) {
		return this.inner.compare(o1, o2);
	}

	@Override
	public List<Class<?>> getSupportedObjectTypes() {
		ArrayList<Class<?>> theResult = new ArrayList<>();

		theResult.add(WebFolder.class);

		return theResult;
	}
}