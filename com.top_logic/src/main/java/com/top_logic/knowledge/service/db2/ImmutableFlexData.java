/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

/**
 * Immutable variant of {@link AbstractFlexData}.
 * 
 * @see MutableFlexData
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ImmutableFlexData extends AbstractFlexData {

	/**
	 * Constructs a new {@link ImmutableFlexData}.
	 */
	protected ImmutableFlexData() {
		super();
	}

	@Override
	public Object setAttributeValue(String attributeName, Object value) {
		throw new IllegalStateException("Immutable object cannot be modified.");
	}

}

