/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.bpml.display;

import java.util.List;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.container.ConfigPart;

/**
 * Defines configuration rules for sequence flows in business process execution. Manages conditions
 * that determine process flow behavior and transitions.
 *
 * @author <a href="mailto:Jonathan.Hüsing@top-logic.com">Jonathan Hüsing</a>
 */
public interface SequenceFlowRule extends ConfigPart {

	/**
	 * 
	 * Retrieves the list of rule conditions that govern this sequence flow.
	 *
	 *
	 */
	@Label("Conditions")
	List<PolymorphicConfiguration<? extends RuleCondition>> getRuleConditions();
}
