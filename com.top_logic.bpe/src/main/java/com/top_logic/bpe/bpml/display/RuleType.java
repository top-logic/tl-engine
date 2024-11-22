/*
 * Copyright (c) 2024 Business Operation Systems GmbH. All Rights Reserved
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
