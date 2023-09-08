/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.function.part;

import com.top_logic.basic.func.Function2;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.search.ui.model.options.SearchTypeUtil;

/**
 * {@link Function2} deciding, whether the given {@link TLType} has any {@link TLStructuredTypePart}
 * s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class HasParts extends Function2<Boolean, TLType, String> {

	/** The {@link HasParts} instance. */
	public static final HasParts INSTANCE = new HasParts();

	@Override
	public Boolean apply(TLType type, String searchName) {
		if (!(type instanceof TLStructuredType)) {
			return false;
		}
		return SearchTypeUtil.hasParts((TLStructuredType) type, searchName);
	}

}
