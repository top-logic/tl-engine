/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action.assertion;

import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.layout.scripting.action.ModelAction;
import com.top_logic.layout.scripting.runtime.action.assertion.ModelNotExistsAssertionOp;

/**
 * Configuration of {@link ModelNotExistsAssertionOp}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ModelNotExistsAssertion extends ModelAction, GuiAssertion {

	/**
	 * Regular expression that must match the expected failure.
	 */
	@Nullable
	String getFailurePattern();

	/**
	 * @see #getFailurePattern()
	 */
	void setFailurePattern(String value);

}
