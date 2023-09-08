/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model;

import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.annotation.DerivedRef;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.layout.form.template.util.ResourceDisplay;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.annotation.UseTemplate;
import com.top_logic.model.TLType;
import com.top_logic.model.search.ui.model.options.ContextExpressionOptions;
import com.top_logic.model.search.ui.model.structure.SearchFilter;

/**
 * {@link SearchFilter} that matches exactly a certain object from a search expression in its
 * context.
 * <p>
 * This is not a test of one of the attributes of an object, but a test about the object (identity)
 * itself.
 * </p>
 * <p>
 * {@link ReferenceValue} represents the starting point of a {@link NavigationValue navigation}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@UseTemplate(ResourceDisplay.class)
public interface ContextFilter extends SearchFilter, NamedConfiguration {

	/**
	 * Property name of {@link #getContextExpression()}.
	 */
	String CONTEXT_EXPRESSION = "context-expression";

	/** Property name of {@link #getType()}. */
	String TYPE = "type";

	/**
	 * Referenced expression with matching type from some {@link #getContext()} ancestor.
	 */
	@Name(CONTEXT_EXPRESSION)
	@Mandatory
	@Options(fun = ContextExpressionOptions.class, args = {
		@Ref({ CONTEXT, CONTEXT }),
		@Ref(CONTEXT_TYPE) })
	ReferenceValue getContextExpression();

	/**
	 * @see #getContextExpression()
	 */
	void setContextExpression(ReferenceValue value);

	@Override
	@DerivedRef({ CONTEXT_EXPRESSION, ReferenceValue.NAME })
	String getName();

	/** The type of the context expression. */
	@Name(TYPE)
	@DerivedRef({ CONTEXT_EXPRESSION, ReferenceValue.VALUE_TYPE })
	TLType getType();

}
