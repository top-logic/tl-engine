/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.util;

import com.top_logic.basic.col.Mapping;
import com.top_logic.layout.form.FormField;

/**
 * {@link Mapping} adapter that uses {@link BusinessModelMapping} before a custom mapping.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BusinessModelMappingAdapter extends BusinessModelMapping {

	private final Mapping<Object, ?> _custom;

	/**
	 * Creates a {@link BusinessModelMappingAdapter}.
	 *
	 * @param custom The custom {@link Mapping} to apply to the extracted {@link FormField#getValue() values}.
	 */
	public BusinessModelMappingAdapter(Mapping<Object, ?> custom) {
		this._custom = custom;
	}
	
	@Override
	public Object map(Object input) {
		return _custom.map(super.map(input));
	}
	
}
