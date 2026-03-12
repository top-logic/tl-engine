/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Creates a {@link ReactControl} for editing a model attribute.
 *
 * <p>
 * Implementations are instantiated by {@link FieldControlService} based on the
 * {@link TLInputControl} annotation or the service's global type-to-provider map.
 * </p>
 */
@FunctionalInterface
public interface ReactFieldControlProvider {

	/**
	 * Creates an input control for the given attribute.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param part
	 *        The model attribute whose type can be inspected for additional metadata.
	 * @param model
	 *        The field model providing value, editability, and change notifications.
	 * @return A React control for the field input widget.
	 */
	ReactControl createControl(ReactContext context, TLStructuredTypePart part, FieldModel model);

}
