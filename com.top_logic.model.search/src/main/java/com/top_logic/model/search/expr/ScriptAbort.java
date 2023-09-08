/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import com.top_logic.basic.util.ResKey;
import com.top_logic.util.error.TopLogicException;

/**
 * Special {@link TopLogicException} marker class that prevents reporting script execution stack
 * traces to the UI.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ScriptAbort extends TopLogicException {

	/**
	 * Creates a {@link ScriptAbort}.
	 */
	public ScriptAbort(ResKey errorKey) {
		super(errorKey);
	}

}
