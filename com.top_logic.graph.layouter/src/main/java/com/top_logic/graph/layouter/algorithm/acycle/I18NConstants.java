/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.acycle;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * The graph's edge could not be removed.
	 */
	public static ResKey EDGE_COULD_NOT_BE_REMOVED;

	/**
	 * The graph's edge could not be reversed.
	 */
	public static ResKey EDGE_COULD_NOT_BE_REVERSED;

	static {
		initConstants(I18NConstants.class);
	}
}