/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution;

import java.util.Map;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.func.Identity;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler.ExecutabilityConfig;

/**
 * {@link ExecutabilityRule} resolving the {@link ExecutableState} by resolving a configured rule to
 * a (potentially) different component.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DelegateRule extends AbstractConfiguredInstance<DelegateRule.Config> implements ExecutabilityRule {

	/**
	 * Configuration of a {@link DelegateRule}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<DelegateRule>, ExecutabilityConfig {

		/**
		 * Name of the configuration property {@link #getDelegateMapping}.
		 */
		String DELEGATE_MAPPING_NAME = "delegate-mapping";

		/**
		 * A mapping the resolved the component to delegate executability computation to.
		 */
		@InstanceFormat
		@InstanceDefault(Identity.class)
		@Name(DELEGATE_MAPPING_NAME)
		Mapping<LayoutComponent, LayoutComponent> getDelegateMapping();

	}

	private final ExecutabilityRule _rule;

	private final Mapping<LayoutComponent, LayoutComponent> _delegateMapping;

	/**
	 * Creates a new {@link DelegateRule} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link DelegateRule}.
	 */
	public DelegateRule(InstantiationContext context, Config config) {
		super(context, config);
		_rule = ExecutabilityRuleManager.resolveRules(context, config);
		_delegateMapping = config.getDelegateMapping();
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent component, Object model, Map<String, Object> someValues) {
		return _rule.isExecutable(_delegateMapping.map(component), model, someValues);
	}

}
