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
import com.top_logic.layout.view.form.FormModel;

/**
 * {@link ViewAction} that reads the current object from the enclosing form.
 *
 * <p>
 * Returns {@link FormModel#getCurrentObject()}, which in edit mode is the overlay containing the
 * user's edits. The input is ignored.
 * </p>
 */
public class ReadFormObjectAction implements ViewAction {

	/**
	 * Configuration for {@link ReadFormObjectAction}.
	 */
	@TagName("read-form-object")
	public interface Config extends PolymorphicConfiguration<ReadFormObjectAction> {

		@Override
		@ClassDefault(ReadFormObjectAction.class)
		Class<? extends ReadFormObjectAction> getImplementationClass();
	}

	/**
	 * Creates a new {@link ReadFormObjectAction}.
	 */
	@CalledByReflection
	public ReadFormObjectAction(InstantiationContext context, Config config) {
		// No configuration.
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		if (context instanceof ViewContext) {
			FormModel formModel = ((ViewContext) context).getFormModel();
			if (formModel != null) {
				return formModel.getCurrentObject();
			}
		}
		return null;
	}
}
