/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.Collection;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;

/**
 * {@link ViewCommandConfirmation} for delete operations.
 *
 * <p>
 * For a single object, produces "Do you really want to delete '...'?". For a collection, produces
 * "Do you really want to delete N elements?".
 * </p>
 */
public class DefaultDeleteConfirmation implements ViewCommandConfirmation {

	/**
	 * Configuration for {@link DefaultDeleteConfirmation}.
	 */
	@TagName("delete-confirmation")
	public interface Config extends ViewCommandConfirmation.Config {

		@Override
		@ClassDefault(DefaultDeleteConfirmation.class)
		Class<? extends ViewCommandConfirmation> getImplementationClass();
	}

	/**
	 * Creates a new {@link DefaultDeleteConfirmation} from configuration.
	 */
	@CalledByReflection
	public DefaultDeleteConfirmation(InstantiationContext context, Config config) {
		// No configuration needed.
	}

	@Override
	public ResKey getConfirmation(ResKey commandLabel, Object input) {
		if (input instanceof Collection<?>) {
			Collection<?> collection = (Collection<?>) input;
			return I18NConstants.CONFIRM_DELETE_MULTI__COUNT.fill(collection.size());
		}
		return I18NConstants.CONFIRM_DELETE__LABEL.fill(commandLabel);
	}
}
