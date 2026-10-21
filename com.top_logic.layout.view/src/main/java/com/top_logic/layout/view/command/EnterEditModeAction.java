/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
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
 * {@link ViewAction} that switches the form to edit mode.
 *
 * <p>
 * Starts an edit session on the object the form displays, the same way the form's own Edit
 * command does: it acquires the lock and is subject to the edit permission of the form (the
 * model right to write the object and the form's edit executability rules).
 * </p>
 *
 * <p>
 * When the form displays no object or the edit permission denies editing it, the action throws a
 * {@link TopLogicException}, which aborts the action chain, so that no later action of the chain
 * runs on a form that cannot be edited. A form already in edit mode keeps its edit session and its
 * input; outside a form, the action has no effect.
 * </p>
 *
 * @implNote Delegates to {@link FormControl#enterEditMode()} after checking
 *           {@link FormControl#getCurrentObject()} and {@link FormControl#editPermission()}.
 */
@InApp
public class EnterEditModeAction implements ViewAction {

	/**
	 * Configuration for {@link EnterEditModeAction}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<EnterEditModeAction> {

		/**
		 * Tag name of the action in an action chain.
		 */
		String TAG_NAME = "enter-edit-mode";

		@Override
		@ClassDefault(EnterEditModeAction.class)
		Class<? extends EnterEditModeAction> getImplementationClass();
	}

	/**
	 * Creates a new {@link EnterEditModeAction}.
	 */
	@CalledByReflection
	public EnterEditModeAction(InstantiationContext context, Config config) {
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

		FormControl form = (FormControl) formModel;
		if (form.isEditMode()) {
			return input;
		}
		if (form.getCurrentObject() == null || !form.editPermission().isExecutable()) {
			throw new TopLogicException(I18NConstants.ERROR_FORM_NOT_EDITABLE);
		}
		form.enterEditMode();
		return input;
	}

	/**
	 * Starts the edit session of the form.
	 */
	@Override
	public boolean changesViewState() {
		return true;
	}
}
