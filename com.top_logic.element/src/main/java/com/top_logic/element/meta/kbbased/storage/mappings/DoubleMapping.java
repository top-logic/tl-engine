/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage.mappings;

import com.top_logic.model.access.StorageMapping;

/**
 * {@link StorageMapping} that ensures {@link Double} values for all {@link Number}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DoubleMapping extends NormalizeMapping<Double> {

	/**
	 * Singleton {@link DoubleMapping} instance.
	 */
	public static final DoubleMapping INSTANCE = new DoubleMapping();

	private DoubleMapping() {
		// Singleton constructor.
	}

	@Override
	public Class<Double> getApplicationType() {
		return Double.class;
	}

	@Override
	protected Double convert(Object value) {
		return Double.valueOf(((Number) value).doubleValue());
	}

	@Override
	protected boolean canConvert(Object value) {
		return value instanceof Number;
	}

}
