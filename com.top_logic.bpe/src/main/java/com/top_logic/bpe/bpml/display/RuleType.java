/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.bpml.display;

/**
 * Types of conditions that can be applied to sequence flows.
 * 
 * @see RuleCondition#getRuleType()
 *
 * @author <a href="mailto:Jonathan.Hüsing@top-logic.com">Jonathan Hüsing</a>
 */
public enum RuleType {
	/**
	 * Default condition that show an error message if not met.
	 */
	DEFAULT,

	/**
	 * Condition that hides the edge when not fulfilled.
	 */
	HIDDEN,

	/**
	 * Condition showing a warning message before continuing with the next step.
	 */
	WARNING
}
