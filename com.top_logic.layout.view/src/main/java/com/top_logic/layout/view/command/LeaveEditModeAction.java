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
 * {@link ViewAction} that switches the form back to view mode after its changes have been stored.
 *
 * <p>
 * Ends the edit session of the form and releases its lock, without storing or discarding
 * anything. It is placed in a save action chain after the changes have been stored, e.g. after a
 * {@link StoreFormStateAction} and the scripts running in the same {@link WithTransactionAction}.
 * </p>
 *
 * <p>
 * A form that still holds unsaved changes is not switched: the action throws a
 * {@link TopLogicException}, which aborts the action chain, and the form stays in edit mode with
 * all its input. Outside edit mode and outside a form, the action has no effect.
 * </p>
 *
 * @implNote Delegates to {@link FormControl#leaveEditMode()}.
 */
@InApp
public class LeaveEditModeAction implements ViewAction {

	/**
	 * Configuration for {@link LeaveEditModeAction}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<LeaveEditModeAction> {

		/**
		 * Tag name of the action in an action chain.
		 */
		String TAG_NAME = "leave-edit-mode";

		@Override
		@ClassDefault(LeaveEditModeAction.class)
		Class<? extends LeaveEditModeAction> getImplementationClass();
	}

	/**
	 * Creates a new {@link LeaveEditModeAction}.
	 */
	@CalledByReflection
	public LeaveEditModeAction(InstantiationContext context, Config config) {
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

		((FormControl) formModel).leaveEditMode();
		return input;
	}

	/**
	 * Ends the edit session of the form.
	 */
	@Override
	public boolean changesViewState() {
		return true;
	}
}
