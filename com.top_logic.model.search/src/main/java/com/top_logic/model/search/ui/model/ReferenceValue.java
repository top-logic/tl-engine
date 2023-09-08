/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model;

import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.ReadOnly;
import com.top_logic.layout.form.template.util.SpanResourceDisplay;
import com.top_logic.layout.form.values.edit.annotation.PropertyEditor;
import com.top_logic.layout.form.values.edit.annotation.UseTemplate;
import com.top_logic.layout.form.values.edit.editor.ValueDisplay;
import com.top_logic.model.TLType;
import com.top_logic.model.format.TLObjectFormat;

/**
 * Reference to some {@link NamedDefinition definition expression}.
 * <p>
 * This represents the starting point of a {@link NavigationValue navigation}.
 * </p>
 * <p>
 * {@link ContextFilter} has a similar function: "The tested object is one of the context values."
 * It is therefore not a test of one of the attributes, but a test about the object (identity)
 * itself.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@UseTemplate(SpanResourceDisplay.class)
public interface ReferenceValue extends ValueContext {

	/**
	 * Property name of {@link #getName()}.
	 */
	String NAME = NamedDefinition.NAME;

	/**
	 * The {@link NamedDefinition#getName()} of the expression referred to.
	 */
	@Name(NAME)
	@ReadOnly
	String getName();

	/**
	 * @see #getName()
	 */
	void setName(String name);

	@Override
	@ReadOnly
	@PropertyEditor(ValueDisplay.class)
	@Format(TLObjectFormat.class)
	TLType getValueType();

	/**
	 * @see #getValueType()
	 */
	void setValueType(TLType value);

	@Override
	@ReadOnly
	boolean getValueMultiplicity();

	/**
	 * @see #getValueMultiplicity()
	 */
	void setValueMultiplicity(boolean value);

}
