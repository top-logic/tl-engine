/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution.service;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.form.values.edit.InAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.AcceptableClassifiers;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link CommandApprovalRule} that can be configured.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
public class ConfiguredApprovalRule extends AbstractConfiguredApprovalRule<ConfiguredApprovalRule.Config> {

	/**
	 * Configuration options for {@link ConfiguredApprovalRule}.
	 */
	public interface Config extends AbstractConfiguredApprovalRule.Config<ConfiguredApprovalRule> {
		/**
		 * @see #getExecutability()
		 */
		String EXECUTABILITY = "executability";

		/**
		 * The {@link ExecutabilityRule} to invoke for computing whether the command can be executed
		 * on the target model.
		 */
		@Name(EXECUTABILITY)
		@Options(fun = InAppImplementations.class)
		@AcceptableClassifiers(CommandApprovalService.APPROVAL_SERVICE_CLASSIFIER)
		List<PolymorphicConfiguration<? extends ExecutabilityRule>> getExecutability();
	}

	private ExecutabilityRule _rule;

	/**
	 * Creates a {@link ConfiguredApprovalRule} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ConfiguredApprovalRule(InstantiationContext context, ConfiguredApprovalRule.Config config) {
		super(context, config);

		_rule =
			CombinedExecutabilityRule.combine(TypedConfiguration.getInstanceList(context, config.getExecutability()));
	}

	@Override
	protected ExecutableState applyRule(LayoutComponent component, BoundCommandGroup commandGroup, String commandId,
			Object model, Map<String, Object> arguments) {
		return _rule.isExecutable(component, model, arguments);
	}

}