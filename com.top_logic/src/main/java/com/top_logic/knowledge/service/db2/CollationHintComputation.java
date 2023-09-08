/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.sql.CollationHint;
import com.top_logic.basic.sql.DBType;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.search.Attribute;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.Reference;
import com.top_logic.knowledge.service.db2.expr.visit.DefaultExpressionVisitor;

/**
 * Computes a {@link CollationHint} from the visited {@link Expression}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CollationHintComputation extends DefaultExpressionVisitor<CollationHint, Void> {

	/** Sole instance */
	public static final CollationHintComputation INSTANCE = new CollationHintComputation();

	/**
	 * Creates a new {@link CollationHintComputation}.
	 */
	protected CollationHintComputation() {
		// singleton instance
	}

	@Override
	public CollationHint visitReference(Reference expr, Void arg) {
		ReferencePart accessType = expr.getAccessType();
		if (accessType == null) {
			return CollationHint.NONE;
		}
		DBAttribute dbColumn = expr.getColumn(accessType);
		return getCollationHint(dbColumn);
	}

	@Override
	public CollationHint visitAttribute(Attribute expr, Void arg) {
		DBAttribute attribute = (DBAttribute) expr.getAttribute();
		return getCollationHint(attribute);
	}

	/**
	 * Returns the {@link CollationHint} for the given attribute.
	 * 
	 * @see #getCollationHint(DBType, boolean)
	 */
	public CollationHint getCollationHint(DBAttribute attribute) {
		return getCollationHint(attribute.getSQLType(), attribute.isBinary());
	}

	/**
	 * Computes {@link CollationHint} for the given {@link DBType}.
	 * 
	 * @param type
	 *        The type to get {@link CollationHint} for.
	 * @param binary
	 *        Whether the type is binary in database or not.
	 */
	public CollationHint getCollationHint(DBType type, boolean binary) {
		switch (type) {
			case CHAR:
			case STRING:
			case CLOB:
				return binary ? CollationHint.BINARY : CollationHint.NATURAL;
			case ID:
				if (IdentifierUtil.SHORT_IDS) {
					/* Binary hint will result in "cast as binary". In that case (at least in MySQL)
					 * 12 is less than 6 (as it is compared as strings). */
					return CollationHint.NONE;
				} else {
					return CollationHint.BINARY;
				}
			default:
				return CollationHint.NONE;
		}
	}

	@Override
	protected CollationHint visitExpression(Expression expr, Void arg) {
		return CollationHint.NONE;
	}
}
