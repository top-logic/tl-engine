/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.ui.form;

import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;

/**
 * Analyzer for a {@link FormField} creating a {@link FieldMatcher} that is able to identify such
 * field later on.
 * 
 * <p>
 * Note: A {@link FieldAnalyzer} must be added to the configuration
 * {@link FieldAnalyzers#getAnalyzers()}.
 * </p>
 * 
 * @see FieldAnalyzers
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface FieldAnalyzer {

	/**
	 * Creates a {@link FieldMatcher} that is able to identify the given field later on.
	 * 
	 * @param field
	 *        The field to create matcher for.
	 * @return A {@link FieldMatcher} able to identify the given field, or <code>null</code>, if
	 *         this {@link FieldAnalyzer} cannot identify the given field.
	 */
	FieldMatcher getMatcher(FormMember field);

}
