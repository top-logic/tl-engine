/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.function;

import com.top_logic.basic.func.Function2;
import com.top_logic.layout.form.model.FieldMode;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLType;
import com.top_logic.model.search.ui.model.options.SearchTypeUtil;

/**
 * {@link FieldMode#INVISIBLE}, if it is not possible to navigate from the given {@link TLType}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class VisibleIfNavigable extends Function2<FieldMode, TLType, String> {

	/** The {@link VisibleIfNavigable} instance. */
	public static final VisibleIfNavigable INSTANCE = new VisibleIfNavigable();

	@Override
	public FieldMode apply(TLType type, String searchName) {
		if ((type == null) || (searchName == null)) {
			return FieldMode.INVISIBLE;
		}
		if (!(type instanceof TLStructuredType)) {
			return FieldMode.INVISIBLE;
		}
		TLStructuredType structuredType = (TLStructuredType) type;
		if (!isNavigable(searchName, structuredType)) {
			return FieldMode.INVISIBLE;
		}
		return FieldMode.ACTIVE;
	}

	private boolean isNavigable(String searchName, TLStructuredType structuredType) {
		return SearchTypeUtil.hasParts(structuredType, searchName)
			|| SearchTypeUtil.isReferenced(structuredType, searchName)
			|| isAssociated(structuredType, searchName);
	}

	private boolean isAssociated(TLStructuredType structuredType, String searchName) {
		if (!(structuredType instanceof TLClass)) {
			return false;
		}
		return SearchTypeUtil.isAssociated((TLClass) structuredType, searchName);
	}

}
