/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.component.AbstractCreateComponent;

/**
 * JSP tag properties that can be set on a {@link FormTag}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface FormTagProperties {

	/**
	 * Whether the form should be printed even if the component has no model.
	 * 
	 * <p>
	 * This is e.g. useful for selector components. By default all {@link AbstractCreateComponent}s
	 * have {@link #setDisplayWithoutModel(boolean)} set to <code>true</code>.
	 * </p>
	 */
	void setDisplayWithoutModel(boolean value);

	/**
	 * Whether to suppress the component's model, implies {@link #setDisplayWithoutModel(boolean)}.
	 */
	void setIgnoreModel(boolean value);

	/**
	 * The resource key suffix to the component resource prefix that is displayed, if the form is
	 * not rendered.
	 */
	void setNoModelKeySuffix(String value);

	/**
	 * The resource key that is displayed, if the form is not rendered.
	 * 
	 * @see #setNoModelKeyConst(ResKey)
	 */
	void setNoModelKey(String value);

	/**
	 * The resource key that is displayed, if the form is not rendered.
	 * 
	 * @see #setNoModelKey(String)
	 */
	void setNoModelKeyConst(ResKey value);

	/**
	 * Dynamic expression selecting whether the form should be displayed or not.
	 */
	void setDisplayCondition(boolean value);

}
