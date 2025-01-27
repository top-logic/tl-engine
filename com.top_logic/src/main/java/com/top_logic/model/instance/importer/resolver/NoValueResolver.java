/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.instance.importer.resolver;

import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.instance.importer.schema.CustomValueConf;

/**
 * Dummy {@link ValueResolver} that does not resolve at all.
 * 
 * <p>
 * This implementation is used internally, if no resolver is found. Error handling is done
 * externally.
 * </p>
 */
public class NoValueResolver implements ValueResolver {

	/**
	 * Singleton {@link NoValueResolver} instance.
	 */
	public static final NoValueResolver INSTANCE = new NoValueResolver();

	private NoValueResolver() {
		// Singleton constructor.
	}

	@Override
	public Object resolve(TLStructuredTypePart attribute, CustomValueConf config) {
		return null;
	}

	@Override
	public CustomValueConf createValueConf(TLStructuredTypePart attribute, Object value) {
		return null;
	}

}
