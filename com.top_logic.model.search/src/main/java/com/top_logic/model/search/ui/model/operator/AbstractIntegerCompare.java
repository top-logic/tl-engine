/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.operator;

import com.top_logic.basic.config.annotation.Abstract;

/**
 * Base interface for models representing an integer comparisons.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface AbstractIntegerCompare<I extends PrimitiveCompare.Impl<?>> extends PrimitiveCompare<I> {

	// Pure marker interface.

}