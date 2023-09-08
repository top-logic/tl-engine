/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage.mappings;

/**
 * {@link NormalizeMapping} that uses {@link Integer} application values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IntMapping extends NormalizeMapping<Integer> {

	/**
	 * Singleton {@link IntMapping} instance.
	 */
	public static final IntMapping INSTANCE = new IntMapping();

	private IntMapping() {
		// Singleton constructor.
	}

	@Override
	public Class<Integer> getApplicationType() {
		return Integer.class;
	}

	@Override
	protected Integer convert(Object value) {
		return Integer.valueOf(((Number) value).intValue());
	}

	@Override
	protected boolean canConvert(Object value) {
		return value instanceof Number;
	}

}
