/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.bpml.display;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.util.ResKey;
import com.top_logic.bpe.execution.model.ProcessExecution;

/**
 * Defines conditions that govern sequence flow transitions
 *
 * @author <a href="mailto:Jonathan.Hüsing@top-logic.com">Jonathan Hüsing</a>
 */

@Label("SequenceFlow Rule Definition")
public interface RuleCondition {


	/**
	 * Evaluates condition against process state.
	 *
	 * @param process
	 *        Current processExecution
	 * @return True if condition is satisfied
	 */
	Boolean getCondition(ProcessExecution process);

	/**
	 * Gets the error or warning message for this condition
	 * 
	 * @return Message resource key
	 */
	ResKey getMessage();

	/**
	 * Gets the classification type of this Condition.
	 * 
	 * @return Rule type
	 */
	RuleType getRuleType();

}
