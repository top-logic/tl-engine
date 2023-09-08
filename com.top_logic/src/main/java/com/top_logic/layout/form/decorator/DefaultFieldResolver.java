/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import java.util.NoSuchElementException;

import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormGroup;

/**
 * Default field resolver that finds the field by the {@link FormMember#getQualifiedName() qualified
 * name}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultFieldResolver implements FieldResolver {

	/** Instance of the {@link DefaultFieldResolver} */
	public static DefaultFieldResolver INSTANCE = new DefaultFieldResolver();

	/**
	 * Returns the field to compare given field to.
	 * 
	 * @param field
	 *        The original field
	 * @return The field with the compare value, or <code>null</code>
	 */
	@Override
	public FormField findCompareField(FormField field, FormContext compareContext) {
		return fieldForQualifiedName(compareContext, field.getQualifiedName());
	}

	/**
	 * Searches for the {@link FormField} in the given {@link FormContext} with the given name.
	 * 
	 * <p>
	 * Note: Just calls
	 * {@link FormGroup#getMemberByQualifiedName(com.top_logic.layout.form.FormContainer, String)}
	 * but returns <code>null</code> instead of throwing a {@link NoSuchElementException}.
	 * </p>
	 * 
	 * @param context
	 *        The context to search member in.
	 * @param qualifiedName
	 *        The qualified name of the name to search.
	 * 
	 * @return The {@link FormField} in the given {@link FormContext} with the given qualified name,
	 *         or <code>null</code> when there is no such member.
	 * 
	 * @see FormGroup#getMemberByQualifiedName(com.top_logic.layout.form.FormContainer, String)
	 */
	public static FormField fieldForQualifiedName(FormContext context, String qualifiedName) {
		try {
			return (FormField) FormGroup.getMemberByQualifiedName(context, qualifiedName);
		} catch (NoSuchElementException ex) {
			// No such field in compare context. This may occur, e.g. when an attribute in historic
			// context does not exists.
			return null;
		}
	}

}
