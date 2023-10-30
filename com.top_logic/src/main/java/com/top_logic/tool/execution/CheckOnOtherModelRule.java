/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * {@link ExecutabilityRule} that evaluates other {@link ExecutabilityRule}s on a custom model (not
 * the commands target model).
 */
@InApp
@Label("Check on other model")
public class CheckOnOtherModelRule extends AbstractConfiguredInstance<CheckOnOtherModelRule.Config>
		implements ExecutabilityRule {

	/**
	 * Configuration options for {@link CheckOnOtherModelRule}.
	 */
	public interface Config extends PolymorphicConfiguration<CheckOnOtherModelRule>, CommandHandler.ExecutabilityConfig,
			CommandHandler.TargetConfig {
		// Pure sum interface.
	}

	private final ChannelLinking _target;

	private final ExecutabilityRule _rule;

	/**
	 * Creates a {@link CheckOnOtherModelRule} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CheckOnOtherModelRule(InstantiationContext context, Config config) {
		super(context, config);

		_target = context.getInstance(config.getTarget());
		_rule =
			CombinedExecutabilityRule.combine(TypedConfiguration.getInstanceList(context, config.getExecutability()));
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent component, Object model, Map<String, Object> values) {
		return _rule.isExecutable(component, _target.eval(component), values);
	}

}
