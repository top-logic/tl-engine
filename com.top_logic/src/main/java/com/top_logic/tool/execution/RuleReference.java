/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ExecutabilityRule} that delegates to a globally configured rule in
 * {@link ExecutabilityRuleManager}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RuleReference extends AbstractConfiguredInstance<RuleReference.Config> implements ExecutabilityRule {

	/**
	 * Configuration interface for {@link RuleReference}.
	 */
	@TagName("reference")
	public interface Config extends PolymorphicConfiguration<RuleReference> {
		/**
		 * @see #getRuleId()
		 */
		String RULE_ID_PROPERTY = "rule-id";

		@ClassDefault(RuleReference.class)
		@Override
		public Class<? extends RuleReference> getImplementationClass();

		/**
		 * The ID of the referenced {@link ExecutabilityRule}.
		 * 
		 * @see ExecutabilityRuleManager#getExecutabilityRule(String)
		 */
		@Name(RULE_ID_PROPERTY)
		String getRuleId();

		/**
		 * @see #getRuleId()
		 */
		void setRuleId(String value);
	}

	/**
	 * Creates a {@link RuleReference} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public RuleReference(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * The ID of the referenced rule.
	 */
	public String getRuleId() {
		return getConfig().getRuleId();
	}


	/**
	 * Note: This method should not be called during normal operation, since a rule reference is
	 * resolved to the target rule before operation.
	 * 
	 * @see ExecutabilityRuleManager#resolveRules(InstantiationContext,
	 *      com.top_logic.tool.boundsec.CommandHandler.ExecutabilityConfig)
	 * @see ExecutabilityRule#isExecutable(LayoutComponent, Object, Map)
	 */
	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		ExecutabilityRule delegate = ExecutabilityRuleManager.getRule(getRuleId());
		return delegate.isExecutable(aComponent, model, someValues);
	}

}
