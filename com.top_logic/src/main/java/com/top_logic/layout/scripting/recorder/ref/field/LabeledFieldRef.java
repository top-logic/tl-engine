/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.field;

import com.top_logic.layout.form.FormField;

/**
 * {@link FieldRef} that identifies a {@link FormField} by its label.
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public interface LabeledFieldRef extends FieldRef {

	/** The label of the referenced field. */
	String getLabel();

	/** @see #getLabel() */
	void setLabel(String label);

}
