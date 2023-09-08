/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.options;

import static java.util.Collections.*;

import java.util.List;

import com.top_logic.basic.func.Function1;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLType;

/**
 * Returns all instances in of the given {@link TLType}.
 * <p>
 * Does not support {@link TLPrimitive}.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class InstanceOptions extends Function1<List<? extends Object>, TLType> {

	/** The {@link InstanceOptions} instance. */
	public static final InstanceOptions INSTANCE = new InstanceOptions();

	@Override
	public List<? extends Object> apply(TLType type) {
		if (type == null) {
			return emptyList();
		}
		return type.visitType(InstanceOptionsVisitor.INSTANCE, null);
	}

}
