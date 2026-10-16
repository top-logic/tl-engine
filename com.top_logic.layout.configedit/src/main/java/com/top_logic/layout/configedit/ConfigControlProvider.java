/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;

/**
 * Creates the {@link ReactControl} that edits a single configuration property.
 *
 * <p>
 * Implementations are registered in the configuration of the {@link ConfigControlService}: by the
 * Java type of the property value, or by the class of the property's value provider.
 * </p>
 *
 * <p>
 * This is the configuration-side counterpart of {@code ReactFieldControlProvider}.
 * </p>
 */
@FunctionalInterface
public interface ConfigControlProvider {

	/**
	 * Creates the input control for the property the given model is bound to.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param model
	 *        The field model providing value, editability, and change notifications. Its
	 *        {@link ConfigFieldModel#getProperty() property} carries the metadata.
	 * @return The control for the field input widget.
	 */
	ReactControl createControl(ReactContext context, ConfigFieldModel model);

}
