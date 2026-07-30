/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.FormModel;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ViewExecutabilityRule} that disables a command while the enclosing form displays
 * validation errors.
 *
 * <p>
 * Guards commands that store the entered values: such a command is rejected by the form anyway, so
 * offering it while the problems are on screen is misleading. Errors the user cannot see yet do not
 * disable the command - the save attempt is what makes them visible in the first place.
 * </p>
 *
 * <p>
 * Applied automatically to a form command whose action chain contains a
 * {@link StoreFormStateAction}. Configure it explicitly for a command that applies form values
 * through some other action.
 * </p>
 */
public class FormValid implements ViewExecutabilityRule, ContextDependentRule {

	/**
	 * Configuration for {@link FormValid}.
	 */
	@TagName("form-valid")
	public interface Config extends ViewExecutabilityRule.Config {

		@Override
		@ClassDefault(FormValid.class)
		Class<? extends ViewExecutabilityRule> getImplementationClass();
	}

	private FormControl _form;

	/**
	 * Creates a {@link FormValid} rule from configuration.
	 */
	@CalledByReflection
	public FormValid(InstantiationContext context, Config config) {
		// The guarded form is resolved from the command's context.
	}

	/**
	 * Creates a {@link FormValid} rule for the given form.
	 */
	public FormValid(FormControl form) {
		_form = form;
	}

	@Override
	public void bind(ViewContext context) {
		FormModel formModel = context.getFormModel();
		if (formModel instanceof FormControl form) {
			_form = form;
		}
	}

	@Override
	public ExecutableState isExecutable(Object input) {
		if (_form == null || !_form.hasVisibleErrors()) {
			return ExecutableState.EXECUTABLE;
		}
		return ExecutableState.createDisabledState(I18NConstants.ERROR_FORM_HAS_VALIDATION_ERRORS);
	}
}
