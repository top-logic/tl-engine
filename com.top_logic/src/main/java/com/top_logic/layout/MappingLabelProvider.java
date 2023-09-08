/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.basic.col.Mapping;

/**
 * Adaptor class to use a {@link Mapping} with result type {@link String} as
 * {@link LabelProvider}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class MappingLabelProvider implements LabelProvider {

	private final Mapping labelMapping;

	private MappingLabelProvider(Mapping labelMapping) {
		this.labelMapping = labelMapping;
	}
	
	@Override
	public String getLabel(Object object) {
		return (String) labelMapping.map(object);
	}

	/**
	 * Wraps the given {@link Mapping} with return type {@link String} into a
	 * {@link ResourceProvider} that effectively only implements the
	 * {@link LabelProvider} interface.
	 */
	public static LabelProvider getInstance(Mapping labelMapping) {
		return new MappingLabelProvider(labelMapping);
	}

}
