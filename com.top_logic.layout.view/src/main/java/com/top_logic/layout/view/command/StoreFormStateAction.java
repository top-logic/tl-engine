/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.FormModel;

/**
 * {@link ViewAction} that validates the form, reveals all errors, and applies edits to the base
 * object.
 *
 * <p>
 * On execution:
 * </p>
 * <ol>
 * <li>Reveals all field validation errors (sets {@code revealed = true} on all fields).</li>
 * <li>If the form has validation errors, throws a {@link TopLogicException} to abort the action
 * chain and display the error in the snackbar.</li>
 * <li>Otherwise, applies overlay edits to the base object and returns it.</li>
 * </ol>
 */
public class StoreFormStateAction implements ViewAction {

	/**
	 * Configuration for {@link StoreFormStateAction}.
	 */
	@TagName("store-form-state")
	public interface Config extends PolymorphicConfiguration<StoreFormStateAction> {

		@Override
		@ClassDefault(StoreFormStateAction.class)
		Class<? extends StoreFormStateAction> getImplementationClass();
	}

	/**
	 * Creates a new {@link StoreFormStateAction}.
	 */
	@CalledByReflection
	public StoreFormStateAction(InstantiationContext context, Config config) {
		// No configuration.
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		if (!(context instanceof ViewContext)) {
			return input;
		}

		FormModel formModel = ((ViewContext) context).getFormModel();
		if (!(formModel instanceof FormControl)) {
			return input;
		}

		FormControl formControl = (FormControl) formModel;
		Object result = formControl.executeStoreState();
		return result != null ? result : input;
	}
}
