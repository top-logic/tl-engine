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
import com.top_logic.layout.view.form.FormModelListener;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ViewExecutabilityRule} that disables a command while the enclosing form displays
 * validation errors.
 *
 * <p>
 * Disables only a command whose executability states it. Without it, a command storing the
 * entered values stays executable while the form displays errors: executing it stores nothing and
 * names the errors. A command that must not be offered while errors are on display - Next of a
 * wizard step, say - states the rule, and its disabled button names the errors as reason. Errors
 * the user cannot see yet do not disable the command.
 * </p>
 *
 * @implNote A storing command refuses to store through {@link FormControl#validateOrThrow()}.
 */
public class FormValid implements ViewExecutabilityRule, ContextDependentRule, ObservableRule {

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

	@Override
	public void bind(ViewContext context) {
		FormModel formModel = context.getFormModel();
		if (formModel instanceof FormControl form) {
			_form = form;
		}
	}

	/**
	 * Follows the form: the errors becoming visible is what turns the command off, and fixing them
	 * is what turns it on again.
	 */
	@Override
	public Runnable observe(Runnable revalidate) {
		if (_form == null) {
			return () -> {
				// No form to follow.
			};
		}
		FormControl form = _form;
		FormModelListener listener = new FormModelListener() {
			@Override
			public void onFormStateChanged(FormModel source) {
				revalidate.run();
			}

			@Override
			public void onValidityChanged(FormModel source) {
				revalidate.run();
			}
		};
		form.addFormModelListener(listener);
		return () -> form.removeFormModelListener(listener);
	}

	/**
	 * Disables the command while the form shows errors, giving {@link FormControl#visibleErrorMessage()}
	 * as reason: the button names the errors that keep it from being used.
	 */
	@Override
	public ExecutableState isExecutable(Object input) {
		if (_form == null || !_form.hasVisibleErrors()) {
			return ExecutableState.EXECUTABLE;
		}
		return ExecutableState.createDisabledState(_form.visibleErrorMessage());
	}
}
