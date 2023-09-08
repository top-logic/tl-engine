/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.model.FormGroup;


/**
 * Builder of a {@link FilterViewControl} of a {@link ConfiguredFilter}.
 * 
 * @author <a href="mailto:sts@top-logic.com">sts</a>
 */
public interface FilterViewBuilder<T extends FilterConfiguration> {
	
	/**
	 * Creates a displayable content control of a {@link ConfiguredFilter}.
	 * 
	 * @param context
	 *        The display context, must not be null
	 * @param config
	 *        The configuration of a {@link ConfiguredFilter}, must not be null
	 * @param form
	 *        The form to create fields in.
	 * @return a displayable {@link Control} of a {@link ConfiguredFilter}
	 */
	public FilterViewControl<?> createFilterViewControl(DisplayContext context, T config, FormGroup form,
			int filterControlId);

}
