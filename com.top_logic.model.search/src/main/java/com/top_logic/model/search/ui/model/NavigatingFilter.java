/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.model.search.ui.model.structure.SearchFilter;

/**
 * {@link SearchFilter} that navigates to another value which is filtered.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface NavigatingFilter extends SearchFilter, OperatorContainer {
	// Pure sum interface.
}
