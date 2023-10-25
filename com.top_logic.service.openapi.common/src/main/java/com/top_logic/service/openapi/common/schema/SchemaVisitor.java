/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.schema;

/**
 * Visitor interface for {@link Schema}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface SchemaVisitor<R, A> {

	/**
	 * Visit case for {@link PrimitiveSchema}.
	 *
	 * @param schema
	 *        The visited {@link Schema}.
	 * @param arg
	 *        Argument delivered by {@link Schema#visit(SchemaVisitor, Object)}.
	 * @return Value that is returned to by {@link Schema#visit(SchemaVisitor, Object)}.
	 */
	R visitPrimitiveSchema(PrimitiveSchema schema, A arg);

	/**
	 * Visit case for {@link ObjectSchema}.
	 *
	 * @param schema
	 *        The visited {@link Schema}.
	 * @param arg
	 *        Argument delivered by {@link Schema#visit(SchemaVisitor, Object)}.
	 * @return Value that is returned to by {@link Schema#visit(SchemaVisitor, Object)}.
	 */
	R visitObjectSchema(ObjectSchema schema, A arg);

	/**
	 * Visit case for {@link ArraySchema}.
	 *
	 * @param schema
	 *        The visited {@link Schema}.
	 * @param arg
	 *        Argument delivered by {@link Schema#visit(SchemaVisitor, Object)}.
	 * @return Value that is returned to by {@link Schema#visit(SchemaVisitor, Object)}.
	 */
	R visitArraySchema(ArraySchema schema, A arg);

}
