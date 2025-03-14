/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.bpml.display;

import com.top_logic.basic.util.ResKey;
import com.top_logic.bpe.execution.model.ProcessExecution;

/**
 * Defines conditions that govern sequence flow transitions.
 *
 * @author <a href="mailto:Jonathan.Hüsing@top-logic.com">Jonathan Hüsing</a>
 */
public interface RuleCondition {

	/**
	 * Tests, whether this condition is satisfied.
	 *
	 * @param process
	 *        Current {@link ProcessExecution} object representing the state of the workflow to test
	 *        the condition on.
	 */
	boolean getTestCondition(ProcessExecution process);

	/**
	 * The error or warning message to show, if this condition is not satisfied.
	 */
	ResKey getMessage();

	/**
	 * Specifies the UI behavior to show, when this condition is not satisfied.
	 */
	RuleType getRuleType();

}
