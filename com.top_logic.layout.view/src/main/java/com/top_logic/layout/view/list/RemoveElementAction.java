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
import com.top_logic.layout.view.command.ViewAction;

/**
 * {@link ViewAction} that removes the chain's current input value from the enclosing
 * {@link ObjectListElement &lt;object-list&gt;}'s container.
 *
 * <p>
 * Runs the list's configured remove function with the container and the input value in a
 * transaction. Typical usage in an item template, with the item channel as command input:
 * </p>
 *
 * <pre>
 * &lt;generic-command placement="TOOLBAR" input="comment"&gt;
 *   &lt;remove-element/&gt;
 * &lt;/generic-command&gt;
 * </pre>
 */
public class RemoveElementAction implements ViewAction {

	/**
	 * Configuration for {@link RemoveElementAction}.
	 */
	@TagName("remove-element")
	public interface Config extends PolymorphicConfiguration<RemoveElementAction> {

		@Override
		@ClassDefault(RemoveElementAction.class)
		Class<? extends RemoveElementAction> getImplementationClass();
	}

	/**
	 * Creates a new {@link RemoveElementAction}.
	 */
	@CalledByReflection
	public RemoveElementAction(InstantiationContext context, Config config) {
		// No configuration state.
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		return LinkElementAction.listScope(context).removeElement(input);
	}

}
