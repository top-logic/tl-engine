/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.configedit.ConfigFieldModel;
import com.top_logic.layout.configedit.ConfigFormControl;
import com.top_logic.layout.form.values.edit.Labels;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.form.ValidationErrors;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ViewExecutabilityRule} disabling a command while a configuration form of the enclosing
 * element displays errors.
 *
 * <p>
 * The counterpart of {@link FormValid} for configuration forms, for a command saving what such a
 * form edits: the command would be refused anyway, so offering it while the problems are on screen
 * is misleading. Errors the user cannot see yet do not disable the command - the save attempt,
 * through {@link CheckConfigFormAction}, is what makes them visible.
 * </p>
 */
public class ConfigFormValid implements ViewExecutabilityRule, ContextDependentRule, ObservableRule {

	/**
	 * Configuration for {@link ConfigFormValid}.
	 */
	@TagName("config-form-valid")
	public interface Config extends ViewExecutabilityRule.Config {

		@Override
		@ClassDefault(ConfigFormValid.class)
		Class<? extends ViewExecutabilityRule> getImplementationClass();
	}

	private ConfigFormScope _scope;

	/**
	 * Creates a {@link ConfigFormValid} rule from configuration.
	 */
	@CalledByReflection
	public ConfigFormValid(InstantiationContext context, Config config) {
		// The forms are resolved from the command's context.
	}

	@Override
	public void bind(ViewContext context) {
		_scope = context.getScope(ConfigFormScope.class);
	}

	/**
	 * Follows the forms: a form appearing, and errors appearing at its fields, is what turns the
	 * command off, and fixing them is what turns it on again.
	 */
	@Override
	public Runnable observe(Runnable revalidate) {
		if (_scope == null) {
			return () -> {
				// No forms to follow.
			};
		}
		ConfigFormScope scope = _scope;
		List<Runnable> formObservations = new ArrayList<>();
		Runnable observeForms = () -> {
			formObservations.forEach(Runnable::run);
			formObservations.clear();
			for (ConfigFormControl form : scope.getForms()) {
				formObservations.add(form.observeValidity(revalidate));
			}
		};
		observeForms.run();
		Runnable scopeObservation = scope.observe(() -> {
			observeForms.run();
			revalidate.run();
		});
		return () -> {
			scopeObservation.run();
			formObservations.forEach(Runnable::run);
			formObservations.clear();
		};
	}

	/**
	 * Disables the command while a form shows errors, giving as reason the errors of all forms,
	 * each named by the label of its field, see {@link ValidationErrors#text(ResKey, List)}.
	 */
	@Override
	public ExecutableState isExecutable(Object input) {
		if (_scope == null) {
			return ExecutableState.EXECUTABLE;
		}
		List<ResKey> errors = new ArrayList<>();
		for (ConfigFormControl form : _scope.getForms()) {
			for (ConfigFieldModel field : form.fieldsWithVisibleError()) {
				errors.add(I18NConstants.ERROR_FORM_FIELD__LABEL_MESSAGE
					.fill(Labels.propertyLabel(field.getProperty(), false), field.getError()));
			}
		}
		if (errors.isEmpty()) {
			return ExecutableState.EXECUTABLE;
		}
		return ExecutableState.createDisabledState(
			ValidationErrors.text(I18NConstants.ERROR_FORM_HAS_VALIDATION_ERRORS, errors));
	}

}
