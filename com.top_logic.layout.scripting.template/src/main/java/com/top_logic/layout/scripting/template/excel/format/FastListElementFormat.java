/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.excel.format;

import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.model.format.TLClassifierFormat;

/**
 * General handling for fast list element based formatting.
 * <p>
 * The name of the requested {@link FastList} is provided in the constructor. The mapping will be
 * done by the translation of the element names. The used language is defined in
 * {@link #getResources}. The names are compared case-insensitive.
 * </p>
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 * 
 * @deprecated Use {@link TLClassifierFormat}.
 */
@Deprecated
public class FastListElementFormat extends TLClassifierFormat {

	/**
	 * Creates a {@link FastListElementFormat}.
	 * 
	 * @param listName
	 *        The name of the fast list to be used, must not be <code>null</code>.
	 */
	@Deprecated
	public FastListElementFormat(String listName) {
		super(listName);
	}

}
