/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * A transformation in a component context that can be added to UI actions transforming the command
 * result further before using it in the UI.
 */
public interface ValueTransformation {

	/**
	 * The identity transformation.
	 */
	ValueTransformation IDENTITY = (c, x) -> x;

	/**
	 * Transforms the given model before using it in a UI action.
	 */
	Object transform(LayoutComponent component, Object model);

	/**
	 * Creates a {@link ValueTransformation} from an optional configuration.
	 * 
	 * @param config
	 *        The {@link ValueTransformation} configuration, or <code>null</code> meaning
	 *        "identity".
	 */
	static ValueTransformation getInstance(InstantiationContext context,
			PolymorphicConfiguration<ValueTransformation> config) {
		ValueTransformation result = context.getInstance(config);
		if (result == null) {
			return ValueTransformation.IDENTITY;
		}
		return result;
	}

}
