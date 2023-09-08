/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.provider;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLDefaultValue;

/**
 * Provider for the default value of an {@link TLStructuredTypePart}.
 * 
 * @see TLDefaultValue
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Label("Default value computation")
public interface DefaultProvider {

	/**
	 * Creates the default value for the given {@link TLStructuredTypePart attribute}.
	 * 
	 * @param context
	 *        The context in which the default for a new object is needed, e.g. when the default for
	 *        a new child of an structured element is needed the context is the parent. May be
	 *        <code>null</code>.
	 * @param attribute
	 *        The attribute to create default value for.
	 * @param createForUI
	 *        Whether the default value must be created for the UI. If <code>true</code> the default
	 *        value is created whereas no object for the value is created yet, otherwise the object
	 *        is created before the default value is created.
	 * @return Default value for the given attribute. May be <code>null</code>.
	 */
	Object createDefault(Object context, TLStructuredTypePart attribute, boolean createForUI);

}

