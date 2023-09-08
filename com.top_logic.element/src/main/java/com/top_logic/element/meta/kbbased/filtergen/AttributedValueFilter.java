/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.filtergen;

import com.top_logic.element.config.annotation.TLConstraint;
import com.top_logic.element.config.annotation.TLOptions;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.model.annotate.TLConstraints;
import com.top_logic.model.annotate.util.ConstraintCheck;

/**
 * Extend the Filter with the possibility to set an object to allow individual checks.
 * 
 * @deprecated Use {@link ConstraintCheck} in combination with the {@link TLConstraints} annotation,
 *             or a {@link Generator} in combination with a {@link TLOptions} annotation.
 * 
 * @see TLConstraint
 */
@Deprecated
public interface AttributedValueFilter {

    /**
	 * Check acceptance of a value for the given attribute of the given object.
	 * 
	 * @param value
	 *        The value to be checked. May be <code>null</code>.
	 * @param editContext
	 *        The edit context. May be <code>null</code>.
	 * @return <code>true</code> if the value is accepted.
	 */
    public boolean accept(Object value, EditContext editContext);
}
