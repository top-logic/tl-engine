/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.execution.engine;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.top_logic.basic.util.ResKey;
import com.top_logic.bpe.bpml.display.ConfigurableCondition;
import com.top_logic.bpe.bpml.display.RuleCondition;
import com.top_logic.bpe.bpml.display.RuleType;
import com.top_logic.bpe.bpml.display.SequenceFlowRule;
import com.top_logic.bpe.bpml.model.Edge;
import com.top_logic.bpe.bpml.model.SequenceFlow;
import com.top_logic.bpe.execution.model.ProcessExecution;

/**
 * Evaluation of the {@link SequenceFlowRule} conditions of an {@link Edge} for a
 * {@link ProcessExecution}.
 *
 * <p>
 * A condition of type {@link RuleType#DEFAULT} that fails blocks the edge, one of type
 * {@link RuleType#HIDDEN} hides it from the choices offered to the user, one of type
 * {@link RuleType#WARNING} lets the user confirm a warning before walking it.
 * </p>
 */
public final class FlowConditions {

	private FlowConditions() {
		// Static utility.
	}

	/**
	 * The conditions of the given types configured for the given edge.
	 *
	 * <p>
	 * An edge that is no {@link SequenceFlow}, or that has no {@link SequenceFlow#getRule() rule},
	 * has no conditions.
	 * </p>
	 */
	public static Stream<RuleCondition> conditions(Edge edge, Set<RuleType> types) {
		if (!(edge instanceof SequenceFlow flow)) {
			return Stream.empty();
		}
		SequenceFlowRule rule = flow.getRule();
		if (rule == null) {
			return Stream.empty();
		}
		return rule.getRuleConditions().stream()
			.map(config -> (RuleCondition) new ConfigurableCondition(null, (ConfigurableCondition.Config<?>) config))
			.filter(condition -> types.contains(condition.getRuleType()));
	}

	/**
	 * Whether all conditions of the given types configured for the given edge hold for the given
	 * {@link ProcessExecution}.
	 */
	public static boolean holds(Edge edge, ProcessExecution execution, RuleType type, RuleType... moreTypes) {
		return conditions(edge, EnumSet.of(type, moreTypes))
			.allMatch(condition -> condition.getTestCondition(execution));
	}

	/**
	 * The messages of the conditions of the given type configured for the given edge that fail for
	 * the given {@link ProcessExecution}.
	 */
	public static List<ResKey> failures(Edge edge, ProcessExecution execution, RuleType type) {
		return conditions(edge, EnumSet.of(type))
			.filter(condition -> !condition.getTestCondition(execution))
			.map(condition -> condition.getMessage(execution))
			.toList();
	}

}
