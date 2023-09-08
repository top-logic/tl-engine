/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.filtergen;

import com.top_logic.basic.col.Filter;
import com.top_logic.element.config.annotation.TLOptions;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.model.annotate.TLConstraints;
import com.top_logic.model.annotate.util.ConstraintCheck;

/**
 * {@link AttributedValueFilter} that does not consider its context.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 * 
 * @deprecated Use {@link ConstraintCheck} in combination with the {@link TLConstraints} annotation,
 *             or a {@link Generator} in combination with a {@link TLOptions} annotation.
 */
@Deprecated
public abstract class ContextFreeAttributeValueFilter implements AttributedValueFilter, Filter<Object> {

	@Override
	public final boolean accept(Object value, EditContext editContext) {
		return accept(value);
	}
}
