/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.configedit.ConfigFormControl;
import com.top_logic.layout.configedit.ConfigValidation;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ViewAction} checking the configuration forms of the enclosing element before the actions
 * after it save what the forms edit.
 *
 * <p>
 * A configuration form without edit mode writes straight through to the configuration it edits;
 * the command saving that configuration is configured beside it, e.g. in the button bar of a
 * dialog. Placed in front of the saving action, this action does what the Apply of a form with
 * edit mode does: it puts each violation - a mandatory value missing, a constraint failing - on its
 * field and refuses while there is one, or while a field rejected what was typed into it. The
 * actions after it do not run then, so a dialog stays open with the problems on display.
 * </p>
 *
 * <pre>
 * &lt;generic-command ...&gt;
 *   &lt;check-config-form/&gt;
 *   &lt;action class="..."/&gt;
 *   &lt;close-dialog/&gt;
 * &lt;/generic-command&gt;
 * </pre>
 *
 * @see ConfigFormValid
 */
@InApp
public class CheckConfigFormAction implements ViewAction {

	/**
	 * Configuration for {@link CheckConfigFormAction}.
	 */
	@TagName("check-config-form")
	public interface Config extends PolymorphicConfiguration<CheckConfigFormAction> {

		@Override
		@ClassDefault(CheckConfigFormAction.class)
		Class<? extends CheckConfigFormAction> getImplementationClass();
	}

	/**
	 * Creates a {@link CheckConfigFormAction}.
	 */
	@CalledByReflection
	public CheckConfigFormAction(InstantiationContext context, Config config) {
		// No configuration.
	}

	/**
	 * @return The given input, for the next action, when all forms may be saved.
	 * @throws TopLogicException
	 *         When a form refuses to be saved.
	 */
	@Override
	public Object execute(ReactContext context, Object input) {
		if (!(context instanceof ViewContext viewContext)) {
			return input;
		}
		ConfigFormScope scope = viewContext.getScope(ConfigFormScope.class);
		if (scope == null) {
			return input;
		}
		ConfigValidation.Refusal first = null;
		for (ConfigFormControl form : scope.getForms()) {
			// Every form is checked, so that all their problems are on display at once.
			ConfigValidation.Refusal refusal = form.checkForSave();
			if (first == null) {
				first = refusal;
			}
		}
		if (first != null) {
			throw refusal(first);
		}
		return input;
	}

	/**
	 * The exception reporting the given refusal, its individual violations chained as causes.
	 *
	 * <p>
	 * The message of a refusal only says that the form has errors. A violation of a property shown
	 * without a field of its own - a nested item, a list - is on display nowhere else, so the
	 * violations are listed below the message, where the error display shows the messages of the
	 * causes.
	 * </p>
	 */
	private static TopLogicException refusal(ConfigValidation.Refusal refusal) {
		List<ResKey> details = refusal.details();
		TopLogicException cause = null;
		for (int n = details.size() - 1; n >= 0; n--) {
			cause = cause == null ? new TopLogicException(details.get(n))
				: new TopLogicException(details.get(n), cause);
		}
		return cause == null ? new TopLogicException(refusal.message())
			: new TopLogicException(refusal.message(), cause);
	}

}
