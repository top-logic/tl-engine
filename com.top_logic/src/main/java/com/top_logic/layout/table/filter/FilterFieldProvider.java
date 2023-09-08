/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import com.top_logic.layout.form.FormField;

/**
 * Provider of {@link FormField}s, used for display of {@link ComparableFilter}s
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public interface FilterFieldProvider {

	/**
	 * {@link FormField} with given name and value.
	 */
	FormField createField(String name, Object value);
}
