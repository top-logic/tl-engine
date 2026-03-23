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
import com.top_logic.layout.view.form.TLObjectOverlay;
import com.top_logic.model.TLObject;

/**
 * {@link ViewAction} that applies the form overlay's edits back to the base object.
 *
 * <p>
 * Reads the current object from the form model (the overlay in edit mode), applies its changes to
 * the underlying base object, and returns the base object. This is needed before copying a transient
 * object to persistent storage, so that user edits are transferred to the base object first.
 * </p>
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
		TLObjectOverlay overlay = formControl.getOverlay();
		if (overlay == null) {
			return input;
		}

		// Apply overlay edits to the base object (transient -> transient, no DB involved).
		TLObject base = overlay.getBase();
		overlay.applyTo(base);
		return base;
	}
}
