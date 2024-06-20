/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.provider;

import java.util.function.Supplier;

import com.top_logic.basic.StringServices;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;

/**
 * A {@link DefaultProvider} to create random UUIDs.
 * 
 * @author <a href="mailto:dpa@top-logic.com">dpa</a>
 */
@TargetType(TLTypeKind.STRING)
public class UuidDefaultProvider implements DefaultProvider, Supplier<String> {
	
	/**
	 * Singleton {@link UuidDefaultProvider} instance.
	 */
	public static final UuidDefaultProvider INSTANCE = new UuidDefaultProvider();

	private UuidDefaultProvider() {
		// Singleton constructor.
	}

	@Override
	public Object createDefault(Object context, TLStructuredTypePart attribute, boolean createForUI) {
		return get();
	}

	@Override
	public String get() {
		return StringServices.randomUUID();
	}

}
