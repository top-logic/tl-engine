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

/**
 * {@link ViewAction} that takes the enclosing form out of edit mode without discarding anything.
 *
 * <p>
 * It follows a {@link StoreFormStateAction} in a form's own save chain, which takes the place of
 * the default save and therefore has to end the edit session itself: after the changes are stored,
 * the form shows the stored values of its object and releases its lock, exactly as after the
 * default save. Its input is passed through as its output.
 * </p>
 *
 * <p>
 * The form must not hold unsaved changes when the action runs: a change entered but not stored
 * would be lost. The action then fails with an error message, the form stays in edit mode with all
 * its changes, and the remaining actions of the chain are skipped. A form that is not in edit mode
 * is left as it is.
 * </p>
 *
 * <pre>
 * &lt;form input="item"&gt;
 *   &lt;save-action&gt;
 *     &lt;store-form-state/&gt;
 *     &lt;execute-script function="..."/&gt;
 *     &lt;leave-edit-mode/&gt;
 *   &lt;/save-action&gt;
 * &lt;/form&gt;
 * </pre>
 *
 * @implNote Delegates to {@link FormControl#executeLeaveEditMode()}.
 */
@InApp
public class LeaveEditModeAction implements ViewAction {

	/**
	 * Configuration for {@link LeaveEditModeAction}.
	 */
	@TagName("leave-edit-mode")
	public interface Config extends PolymorphicConfiguration<LeaveEditModeAction> {

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
		if (formModel instanceof FormControl formControl) {
			formControl.executeLeaveEditMode();
		}
		return input;
	}
}
