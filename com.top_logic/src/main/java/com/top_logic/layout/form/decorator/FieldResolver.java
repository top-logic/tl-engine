/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormContext;

/**
 * Resolver for fields in comparison context.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface FieldResolver {

	/**
	 * Searches for the {@link FormField} in the given <code>compareContext</code> corresponding to
	 * the given {@link FormField}.
	 * 
	 * @param field
	 *        The field to find corresponding field to.
	 * @param compareContext
	 *        The context to find the corresponding field in.
	 * @return The {@link FormField} that corresponds to <code>field</code> in
	 *         <code>compareContext</code>. May be <code>null</code>, when there is no corresponding
	 *         field.
	 */
	FormField findCompareField(FormField field, FormContext compareContext);
}
