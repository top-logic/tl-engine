/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.schema.config;

/**
 * Visitor for the {@link AttributeConfig} hierarchy.
 * 
 * @see AttributeConfig#visit(AttributeConfigVisitor, Object)
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface AttributeConfigVisitor<R, A> {

	/**
	 * Visits a {@link ReferenceAttributeConfig}.
	 * 
	 * @param self
	 *        The visited {@link ReferenceAttributeConfig}.
	 */
	R visitReferenceAttributeConfig(ReferenceAttributeConfig self, A arg);

	/**
	 * Visits a {@link PrimitiveAttributeConfig}.
	 * 
	 * @param self
	 *        The visited {@link PrimitiveAttributeConfig}.
	 */
	R visitPrimitiveAttributeConfig(PrimitiveAttributeConfig self, A arg);

}

