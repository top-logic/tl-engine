/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.list;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.FormModel;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ViewAction} that links the chain's current input value to the enclosing
 * {@link ObjectListElement &lt;object-list&gt;}'s container.
 *
 * <p>
 * Runs the list's configured link function with the container and the input value in a
 * transaction, then resets the list's new-element channel to a fresh transient element. Typical
 * usage in a new-element template, after the entered field values have been written to the
 * transient element:
 * </p>
 *
 * <pre>
 * &lt;generic-command placement="TOOLBAR"&gt;
 *   &lt;store-form-state/&gt;
 *   &lt;link-element/&gt;
 * &lt;/generic-command&gt;
 * </pre>
 */
public class LinkElementAction implements ViewAction {

	/**
	 * Configuration for {@link LinkElementAction}.
	 */
	@TagName("link-element")
	public interface Config extends PolymorphicConfiguration<LinkElementAction> {

		@Override
		@ClassDefault(LinkElementAction.class)
		Class<? extends LinkElementAction> getImplementationClass();
	}

	/**
	 * Creates a new {@link LinkElementAction}.
	 */
	@CalledByReflection
	public LinkElementAction(InstantiationContext context, Config config) {
		// No configuration state.
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		ObjectListScope scope = listScope(context);
		Object result = scope.linkElement(input);

		// The entered values are stored in the linked element now: drop the enclosing form's edit
		// session before switching the new-element channel, so the reset is not vetoed as
		// discarding unsaved changes.
		FormModel formModel = ((ViewContext) context).getFormModel();
		if (formModel instanceof FormControl formControl) {
			formControl.executeCancel();
		}

		scope.resetNewElement();
		return result;
	}

	/**
	 * The {@link ObjectListScope} of the given context.
	 *
	 * @throws TopLogicException
	 *         if the action executes outside an {@link ObjectListElement} template.
	 */
	static ObjectListScope listScope(ReactContext context) {
		ObjectListScope scope = ((ViewContext) context).getObjectListScope();
		if (scope == null) {
			throw new TopLogicException(I18NConstants.ERROR_NO_OBJECT_LIST_SCOPE);
		}
		return scope;
	}

}
