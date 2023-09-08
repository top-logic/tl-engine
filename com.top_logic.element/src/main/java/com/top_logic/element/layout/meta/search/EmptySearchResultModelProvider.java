/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta.search;

import java.util.Collections;

import com.top_logic.layout.component.model.ModelProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;

/**
 * {@link ModelProvider} delivering the empty search result set as initial model value.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class EmptySearchResultModelProvider implements ModelProvider {

	/**
	 * Singleton {@link EmptySearchResultModelProvider} instance.
	 */
	public static final EmptySearchResultModelProvider INSTANCE = new EmptySearchResultModelProvider();

	private EmptySearchResultModelProvider() {
		// Singleton constructor.
	}

	@Override
	public Object getBusinessModel(LayoutComponent businessComponent) {
		return new AttributedSearchResultSet(Collections.<TLObject> emptyList(), Collections.<TLClass> emptySet(),
			Collections.<String> emptyList(),
			Collections.EMPTY_LIST);
	}

}
