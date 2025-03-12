/*
 * Copyright (c) 2024 Business Operation Systems GmbH. All Rights Reserved
 */
package com.top_logic.bpe.bpml.display;

import com.top_logic.bpe.bpml.model.SequenceFlow;

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
	 * This is a special Condition, where the {@link SequenceFlow} will be hidden.
	 */
	HIDDEN,

	/**
	 * This is a special Condition, where a Warning Message pops up before Continuing with the next
	 * Node.
	 */
	WARNING
}
