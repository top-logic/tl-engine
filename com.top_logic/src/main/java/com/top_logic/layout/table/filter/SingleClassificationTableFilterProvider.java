/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.Collections;

import com.top_logic.knowledge.wrap.list.FastList;

/**
 * Convenience base class for {@link AbstractClassificationTableFilterProvider}s for single
 * classifications.
 * 
 * @deprecated Use XML-configured {@link DefaultClassificationTableFilterProvider}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Deprecated
public abstract class SingleClassificationTableFilterProvider extends AbstractClassificationTableFilterProvider {

	/**
	 * Creates a {@link SingleClassificationTableFilterProvider}.
	 *
	 */
	public SingleClassificationTableFilterProvider(FastList classification) {
		super(Collections.singletonList(classification));
	}

}
