/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

/**
 * Marker interface for {@link SearchExpression} that are statically known to be of {@link Boolean} type.
 * 
 * <p>
 * {@link BooleanExpression}s are handled especially in certain transformations.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface BooleanExpression {

	// Marker interface for transformation.

}
