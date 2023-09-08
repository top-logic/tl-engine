/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage.mappings;

/**
 * {@link NormalizeMapping} that uses {@link Float} application values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FloatMapping extends NormalizeMapping<Float> {

	/**
	 * Singleton {@link FloatMapping} instance.
	 */
	public static final FloatMapping INSTANCE = new FloatMapping();

	private FloatMapping() {
		// Singleton constructor.
	}

	@Override
	public Class<Float> getApplicationType() {
		return Float.class;
	}

	@Override
	protected Float convert(Object value) {
		return Float.valueOf(((Number) value).floatValue());
	}

	@Override
	protected boolean canConvert(Object value) {
		return value instanceof Number;
	}

}
