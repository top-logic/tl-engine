/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.field;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.scripting.recorder.ref.FieldRefVisitor;

/**
 * Local reference to a {@link FormField} within a {@link FormContainer}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface FieldRef extends ConfigurationItem {

	/**
	 * Dispatch to {@link FieldRefVisitor} according to the concrete type of this instance.
	 */
	Object visit(FieldRefVisitor v, Object arg);

	/**
	 * E.g. the label of the referenced form field.
	 * 
	 * <p>
	 * This property is optional and only for debugging purpose. The value of this property must not
	 * be used for the action implementation.
	 * </p>
	 */
	String getFieldLabelComment();

	/** @see #getFieldLabelComment() */
	void setFieldLabelComment(String value);
	
}
