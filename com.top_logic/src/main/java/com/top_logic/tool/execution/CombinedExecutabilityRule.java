/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * {@link ExecutabilityRule} combining multiple other {@link ExecutabilityRule}s to a single one.
 * 
 * <p>
 * The rules are evaluated in the given order. Checking aborts with the first non-executable state
 * returned.
 * </p>
 * 
 * @see #combine(ExecutabilityRule...)
 * 
 * @author <a href=mailto:fsc@top-logic.com>fsc</a>
 */
public class CombinedExecutabilityRule
		implements ExecutabilityRule, ConfiguredInstance<CombinedExecutabilityRule.Config> {

	/**
	 * Configuration of {@link CombinedExecutabilityRule}.
	 */
	public interface Config extends PolymorphicConfiguration<CombinedExecutabilityRule>,
			CommandHandler.ExecutabilityConfig {
		// Pure sum interface.
	}

	private static final ExecutabilityRule[] RULE_ARRAY = {};

	private final ExecutabilityRule _first;

	private final ExecutabilityRule _next;

	private Config _config;

	/**
	 * Creates a {@link CombinedExecutabilityRule} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CombinedExecutabilityRule(InstantiationContext context, Config config) {
		this(TypedConfiguration.getInstanceList(context, config.getExecutability()));
		_config = config;
	}

	@Override
	public Config getConfig() {
		return _config;
	}
	
	/**
	 * Creates a {@link CombinedExecutabilityRule}.
	 *
	 * @param first
	 *        The first rule to evaluate.
	 * @param next
	 *        The second rule to evaluate.
	 * 
	 * @see #combine(ExecutabilityRule...)
	 */
	private CombinedExecutabilityRule(ExecutabilityRule first, ExecutabilityRule next) {
		_first = first;
		_next = next;
	}

	/**
	 * Creates a {@link CombinedExecutabilityRule}.
	 *
	 * @param rules
	 *        The {@link ExecutabilityRule}s to execute.
	 * 
	 * @deprecated Use {@link #combine(ExecutabilityRule...)}
	 */
	@Deprecated
	protected CombinedExecutabilityRule(ExecutabilityRule... rules) {
		if (rules.length == 0) {
			_first = AlwaysExecutable.INSTANCE;
			_next = AlwaysExecutable.INSTANCE;
		} else {
			_first = rules[0];
			_next = combine(1, rules);
		}
	}

	/**
	 * Creates a {@link CombinedExecutabilityRule}.
	 *
	 * @param rules
	 *        The {@link ExecutabilityRule}s to execute.
	 * 
	 * @deprecated Use {@link #combine(List)}, or {@link #combine(ExecutabilityRule...)}
	 */
	@Deprecated
	public CombinedExecutabilityRule(List<ExecutabilityRule> rules) {
		this(rules.toArray(RULE_ARRAY));
	}
	
	/**
	 * Access to the internal rules, this {@link CombinedExecutabilityRule} is composed of.
	 */
	public List<ExecutabilityRule> getCombinedRules() {
		return Collections.unmodifiableList(Arrays.asList(_first, _next));
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		ExecutableState state = _first.isExecutable(aComponent, model, someValues);
		if (state.isHidden()) {
			// Return the first hidden state, no need to compare more states.
			return state;
		}

		return state.combine(_next.isExecutable(aComponent, model, someValues));
	}

	/**
	 * Combines the given {@link ExecutabilityRule} into a single rule.
	 * 
	 * @param rules
	 *        The rules to check in order.
	 */
	public static ExecutabilityRule combine(ExecutabilityRule... rules) {
		return combine(0, rules);
	}

	private static ExecutabilityRule combine(int start, ExecutabilityRule... rules) {
		switch (rules.length - start) {
			case 0:
				return AlwaysExecutable.INSTANCE;
			case 1:
				return rules[start];
			default:
				return combine(rules[start], combine(start + 1, rules));
		}
	}

	/**
	 * Combines the given {@link ExecutabilityRule} into a single rule.
	 */
	public static ExecutabilityRule combine(ExecutabilityRule r1, ExecutabilityRule r2) {
		if (r1 == AlwaysExecutable.INSTANCE) {
			return r2;
		}
		if (r2 == AlwaysExecutable.INSTANCE) {
			return r1;
		}
		return new CombinedExecutabilityRule(r1, r2);
	}

	/**
	 * Combines the given {@link ExecutabilityRule} into a single rule.
	 */
	public static ExecutabilityRule combine(ExecutabilityRule r1, ExecutabilityRule r2, ExecutabilityRule r3) {
		return combine(r1, combine(r2, r3));
	}

	/**
	 * Combines the given {@link ExecutabilityRule} into a single rule.
	 */
	public static ExecutabilityRule combine(ExecutabilityRule r1, ExecutabilityRule r2, ExecutabilityRule r3,
			ExecutabilityRule r4) {
		return combine(r1, combine(r2, r3, r4));
	}

	/**
	 * Combines the given {@link ExecutabilityRule} into a single rule.
	 * 
	 * @param rules
	 *        The rules to check in order.
	 */
	public static ExecutabilityRule combine(List<ExecutabilityRule> rules) {
		return combine(rules.toArray(RULE_ARRAY));
	}

}
