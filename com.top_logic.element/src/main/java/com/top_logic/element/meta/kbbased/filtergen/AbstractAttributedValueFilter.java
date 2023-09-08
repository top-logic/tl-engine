/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.filtergen;

import com.top_logic.element.config.annotation.TLOptions;
import com.top_logic.model.annotate.TLConstraints;
import com.top_logic.model.annotate.util.ConstraintCheck;

/**
 * Implement the standard accept method to use the new one.
 * 
 * @author <a href="mailto:kbu@top-logic.com>Karsten Buch</a>
 * 
 * @deprecated Use {@link ConstraintCheck} in combination with the {@link TLConstraints} annotation,
 *             or a {@link Generator} in combination with a {@link TLOptions} annotation.
 */
@Deprecated
public abstract class AbstractAttributedValueFilter implements AttributedValueFilter {

	// For legacy compatibility only.

}
