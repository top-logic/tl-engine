/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.field;

import com.top_logic.layout.form.FormField;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.misc.AttributeRef;

/**
 * {@link FieldRef} that identifies a {@link FormField} that contains an
 * attribute value.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface AttributeFieldRef extends FieldRef {

	/**
	 * Reference to the base object, may be <code>null</code>, if the attribute
	 * field is used for object creation.
	 */
	ModelName getSelfRef();

	/** @see #getSelfRef() */
	void setSelfRef(ModelName selfRef);
	
	/**
	 * Reference to the attribute itself. 
	 */
	AttributeRef getAttributeRef();
	void setAttributeRef(AttributeRef value);

}
