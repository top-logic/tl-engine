/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.compile.transform;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.TypeContext;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.search.expr.All;
import com.top_logic.model.search.expr.KBQuery;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressions;
import com.top_logic.model.search.expr.interpreter.Rewriter;

/**
 * Transformation replacing {@link All} expressions by unions of {@link KBQuery} expressions directly
 * accessing corresponding tables.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CreateTableAccess extends Rewriter<Void> {

	private final TypeContext _typeContext;

	/**
	 * Creates a {@link CreateTableAccess}.
	 *
	 * @param typeContext
	 *        The table types.
	 */
	public CreateTableAccess(TypeContext typeContext) {
		_typeContext = typeContext;
	}

	@Override
	public SearchExpression visitAll(All expr, Void arg) {
		TLStructuredType type = expr.getInstanceType();
		if (type.getModelKind() == ModelKind.CLASS) {
			TLClass classType = (TLClass) type;
			Map<String, Set<TLClass>> typesByTableName = AttributeOperations.typesByTableName(classType);
			SearchExpression result = SearchExpressions.literalEmptySet();
			for (Entry<String, Set<TLClass>> tableEntry : typesByTableName.entrySet()) {
				String tableName = tableEntry.getKey();
				Set<TLClass> types = tableEntry.getValue();

				try {
					MOClass tableType = (MOClass) _typeContext.getType(tableName);
					SetExpression tableQuery = AttributeOperations.tableQuery(tableType, types);
					result = union(result, query(classType, tableQuery));
				} catch (UnknownTypeException ex) {
					throw new IllegalArgumentException("Reference to undefined table type '" + tableName + "'.", ex);
				}
			}
			return result;
		} else {
			return expr;
		}
	}

}
