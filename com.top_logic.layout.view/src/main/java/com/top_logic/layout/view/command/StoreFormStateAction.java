/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.FormModel;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ViewAction} that validates the form, reveals all errors, and stores all form changes.
 *
 * <p>
 * On execution:
 * </p>
 * <ol>
 * <li>Reveals all field validation errors (sets {@code revealed = true} on all fields).</li>
 * <li>If the form has validation errors, throws a {@link TopLogicException} to abort the action
 * chain and display the error in the snackbar.</li>
 * <li>Otherwise, stores the form changes the same way the form's own save does and returns the
 * edited object.</li>
 * </ol>
 *
 * <p>
 * For a persistent form object, the changes (including new rows of composition tables) are written
 * in a KB transaction. Within a {@link WithTransactionAction}, that transaction joins the outer
 * one, which decides whether the changes are committed. Without an outer transaction, the action
 * commits the changes itself. For a transient form object (e.g. in a create dialog), the changes
 * are applied to the transient object only.
 * </p>
 *
 * @implNote The changes are stored by {@link FormControl#executeStoreState()}.
 */
@InApp
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
	public boolean appliesFormState() {
		return true;
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
		// The stored values live in the base object now - continue with a clean edit session so
		// the form no longer reports the already-stored values as unsaved changes.
		formControl.refreshEditSession();
		return result != null ? result : input;
	}
}
