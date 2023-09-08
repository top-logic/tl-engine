/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.misc;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.scripting.action.FormAction;
import com.top_logic.layout.scripting.action.FormInput;
import com.top_logic.layout.scripting.action.FormRawInput;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.field.FieldRef;

/**
 * Description of {@link FormField} and associated value.
 * 
 * <p>
 * A {@link FieldValue} serves as input to actions that access (set/check/modify) {@link FormField}
 * s.
 * </p>
 * 
 * @see FormAction
 * @deprecated Use {@link FormInput}, or {@link FormRawInput}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Deprecated
public interface FieldValue extends ConfigurationItem {

	/**
	 * Global reference to the target {@link FormField} from the {@link FormContext} of the context.
	 */
	@EntryTag("field")
	List<FieldRef> getFieldPath();

	/** @see #getFieldPath() */
	void setFieldPath(List<? extends FieldRef> fieldPath);
	
	/**
	 * Description of some value.
	 * 
	 * <p>
	 * The context action defines the semantics and usage of the value.
	 * </p>
	 */
	ModelName getValue();

	/** @see #getValue() */
	void setValue(ModelName valueRef);

}
