/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.command.validation;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.doc.component.WithDocumentationLanguage;
import com.top_logic.doc.model.Page;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.tool.execution.NullModelDisabled;

/**
 * {@link Command} to validate a {@link Page}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ValidatePageCommand extends AbstractCommandHandler {

	private final List<PageValidator> _validators;

	/** {@link ConfigurationItem} for the {@link ValidatePageCommand}. */
	public interface Config extends AbstractCommandHandler.Config, WithDocumentationLanguage {

		@Override
		@FormattedDefault(SimpleBoundCommandGroup.WRITE_NAME)
		CommandGroupReference getGroup();

		/**
		 * Whether the validation must be executed on the complete subtree of the page to validate.
		 */
		boolean isRecursive();

	}
	
	/**
	 * Creates a new {@link ValidatePageCommand}.
	 */
	public ValidatePageCommand(InstantiationContext context, Config config) {
		super(context, config);

		PageValidators validators = ApplicationConfig.getInstance().getConfig(PageValidators.class);
		_validators = TypedConfiguration.getInstanceListReadOnly(context, validators.getValidators());
	}

	/**
	 * Type-safe access to {@link #getConfig()}.
	 */
	protected Config config() {
		return (Config) getConfig();
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {
		if (!(model instanceof Page)) {
			return HandlerResult.error(com.top_logic.doc.command.I18NConstants.SELECTION_IS_NO_PAGE);
		}
		Page page = (Page) model;
		Locale language = config().resolveLanguage(aComponent);
		boolean recursive = config().isRecursive();
		_validators.forEach(validator -> validator.validatePage(aContext, page, language, recursive));
		return HandlerResult.DEFAULT_RESULT;

	}

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return CombinedExecutabilityRule.combine(super.intrinsicExecutability(), this::anyValidatorRule,
			NullModelDisabled.INSTANCE);
	}

	/**
	 * {@link ExecutabilityRule} implementation that hides the command when no
	 * {@link PageValidator}s are given.
	 * 
	 * @param aComponent
	 *        See {@link ExecutabilityRule#isExecutable(LayoutComponent, Object, Map)}.
	 * @param model
	 *        See {@link ExecutabilityRule#isExecutable(LayoutComponent, Object, Map)}.
	 * @param someValues
	 *        See {@link ExecutabilityRule#isExecutable(LayoutComponent, Object, Map)}.
	 */
	private ExecutableState anyValidatorRule(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		if (_validators.isEmpty()) {
			return ExecutableState.NOT_EXEC_HIDDEN;
		}
		return ExecutableState.EXECUTABLE;
	}

}

