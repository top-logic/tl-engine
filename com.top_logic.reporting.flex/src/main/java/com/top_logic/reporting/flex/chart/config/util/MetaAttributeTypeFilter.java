/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.top_logic.basic.col.Filter;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link Filter} for {@link TLStructuredTypePart}s based on their type.
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class MetaAttributeTypeFilter implements Filter<TLStructuredTypePart> {

	private Set<Integer> _types;

	/**
	 * Creates a new {@link MetaAttributeTypeFilter} for the supported types.
	 * 
	 * @param types
	 *        the accepted meta-attribute-types
	 */
	public MetaAttributeTypeFilter(Integer... types) {
		_types = new HashSet<>(Arrays.asList(types));
	}

	@Override
	public boolean accept(TLStructuredTypePart anObject) {
		int type = AttributeOperations.getMetaAttributeType(anObject);
		return _types.contains(type);
	}

}