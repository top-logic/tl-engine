/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.structured.util;

import java.util.Map;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.tool.execution.ExecutableState.CommandVisibility;

/**
 * {@link ExecutabilityRule} that disables or hides a command for a
 * {@link StructuredElement#isRoot() structure root node}.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class StructureRootDisabledExecRule extends AbstractConfiguredInstance<StructureRootDisabledExecRule.Config>
		implements ExecutabilityRule {

	/**
	 * Configuration of a {@link StructureRootDisabledExecRule}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<ExecutabilityRule> {

		/** Configuration name of {@link #getI18NReasonKey()}. */
		String I18N_KEY_PROPERTY = "i18n-reason-key";

		/** Configuration name of {@link #isHidden()}. */
		String HIDDEN_PROPERTY = "hidden";

		/**
		 * Whether the {@link ExecutableState} should be {@link ExecutableState#isHidden() hidden}.
		 */
		@Name(HIDDEN_PROPERTY)
		boolean isHidden();

		/**
		 * Reason to disable the command for {@link StructuredElement#isRoot() root objects}.
		 */
		@Name(I18N_KEY_PROPERTY)
		@InstanceFormat
		/* Default is an I18NConstant which can not used in annotations. */
		ResKey getI18NReasonKey();

	}

	private final ExecutableState _structureRootExecutableState;

	/**
	 * Creates a new {@link StructureRootDisabledExecRule} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link StructureRootDisabledExecRule}.
	 * 
	 */
	public StructureRootDisabledExecRule(InstantiationContext context, Config config) {
		super(context, config);
		CommandVisibility visibility;
		if (config.isHidden()) {
			visibility = ExecutableState.CommandVisibility.HIDDEN;
		} else {
			visibility = ExecutableState.CommandVisibility.DISABLED;
		}
		ResKey i18nReasonKey;
		if (i18nPropertySet(config)) {
			i18nReasonKey = config.getI18NReasonKey();
		} else {
			/* This is actually the default of the property, but it can not be annotated, because
			 * only string literals can be used in annotations. */
			i18nReasonKey = I18NConstants.COMMAND_DISABLED_FOR_ROOT;
		}
		_structureRootExecutableState = new ExecutableState(visibility, i18nReasonKey);
	}

	private boolean i18nPropertySet(Config config) {
		return config.valueSet(config.descriptor().getProperty(Config.I18N_KEY_PROPERTY));
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		if (model instanceof StructuredElement && ((StructuredElement) model).isRoot()) {
			return _structureRootExecutableState;
		}
		return ExecutableState.EXECUTABLE;
	}

}

