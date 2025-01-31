/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.bpml.display;

/**
 * Defines the types of conditions that can be applied to sequence flows.
 *
 * @author <a href="mailto:Jonathan.Hüsing@top-logic.com">Jonathan Hüsing</a>
 */
public enum RuleType {
	/**
	 * This is the default Condition with an Error Message.
	 */
	DEFAULT,

	/**
	 * This is a special Condition, where the @Link{SequenceFlow} will be hidden.
	 */
	HIDDEN,

	/**
	 * This is a special Condition, where a Warning Message pops up before Continuing with the next
	 * Node.
	 */
	WARNING
}
