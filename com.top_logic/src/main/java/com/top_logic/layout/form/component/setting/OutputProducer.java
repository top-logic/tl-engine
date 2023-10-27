/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component.setting;

import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Algorithm creating a component output.
 */
public interface OutputProducer {

	/**
	 * Creates a new value to publish at the component's output channel.
	 *
	 * @param context
	 *        The current {@link DisplayContext}.
	 * @param component
	 *        The context component.
	 * @param model
	 *        The model to operate on.
	 * @return The new output value.
	 */
	Object createOutput(DisplayContext context, LayoutComponent component, Object model);

}