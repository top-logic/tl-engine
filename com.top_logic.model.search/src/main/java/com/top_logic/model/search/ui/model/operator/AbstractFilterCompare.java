/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.operator;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.model.search.ui.model.FilterContainer;
import com.top_logic.model.search.ui.model.structure.InheritedContextType;

/**
 * Base config for filter comparisions.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface AbstractFilterCompare<I extends Operator.Impl<?>>
		extends Operator<I>, FilterContainer, InheritedContextType {

	// Pure sum type.

}
