/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.structured.util;

/**
 * {@link DynamicSequenceName} selecting the context of the created object.
 * 
 * <p>
 * Using {@link ContextAwareSequenceName} ensures that generated IDs start a fresh sequence for each
 * unique context in which an object is created.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ContextAwareSequenceName implements DynamicSequenceName {

	/**
	 * Singleton {@link ContextAwareSequenceName} instance.
	 */
	public static final ContextAwareSequenceName INSTANCE = new ContextAwareSequenceName();

	private ContextAwareSequenceName() {
		// Singleton constructor.
	}

	@Override
	public Object getSequenceName(Object context) {
		return context;
	}

}
