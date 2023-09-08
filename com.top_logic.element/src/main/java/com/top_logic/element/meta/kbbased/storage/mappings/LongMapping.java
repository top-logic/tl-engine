/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage.mappings;

/**
 * {@link NormalizeMapping} that uses {@link Long} application values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LongMapping extends NormalizeMapping<Long> {

	/**
	 * Singleton {@link LongMapping} instance.
	 */
	public static final LongMapping INSTANCE = new LongMapping();

	private LongMapping() {
		// Singleton constructor.
	}

	@Override
	public Class<Long> getApplicationType() {
		return Long.class;
	}

	@Override
	protected Long convert(Object value) {
		return Long.valueOf(((Number) value).longValue());
	}

	@Override
	protected boolean canConvert(Object value) {
		return value instanceof Number;
	}

}
