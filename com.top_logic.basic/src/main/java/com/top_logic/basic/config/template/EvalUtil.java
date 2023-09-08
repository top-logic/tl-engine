/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.template;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.template.Eval.EvalException;
import com.top_logic.basic.config.template.TemplateExpression.ConfigExpression;
import com.top_logic.basic.config.template.TemplateExpression.TemplateStructure;

/**
 * Common utilities for evaluating {@link TemplateExpression}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class EvalUtil {

	/**
	 * Whether the given value is considered non-empty (in the context of a conditional expression).
	 * 
	 * @param value
	 *        The value to test.
	 * @return Whether the given value is non-empty.
	 */
	public static Boolean isNonEmpty(TemplateExpression expr, Object value) {
		if (value == null) {
			return Boolean.FALSE;
		}
		if (value instanceof CharSequence) {
			return Boolean.valueOf(((String) value).length() > 0);
		}
		if (value instanceof Collection<?>) {
			return Boolean.valueOf(((Collection<?>) value).size() > 0);
		}
		if (value instanceof Map<?, ?>) {
			return Boolean.valueOf(((Map<?, ?>) value).size() > 0);
		}
		if (value instanceof TemplateExpression) {
			assert value instanceof TemplateStructure : "Simple expressions should have been evaluated.";
			throw new Eval.EvalException(
				"Only simple expressions may be used in a boolean context" + expr.location() + ".");
		}
		return Boolean.TRUE;
	}

	/**
	 * Whether a value is interpreted as <code>true</code> in a boolean context.
	 * 
	 * @param value
	 *        The value to interpret.
	 * @return Whether it is treated as <code>true</code>.
	 */
	public static Boolean isTrue(TemplateExpression expr, Object value) {
		if (value instanceof Boolean) {
			return (Boolean) value;
		}
		return isNonEmpty(expr, value);
	}

	/**
	 * Access an indexed property of a collection value.
	 *
	 * @param collection
	 *        The collection in the widest sense.
	 * @param index
	 *        An index value in the widest sense.
	 * @return The value of the given collection at the given index.
	 */
	public static Object accessCollection(ConfigExpression collectionExpr, ConfigExpression indexExpr,
			Object collection, Object index) {
		if (collection instanceof List<?>) {
			return accessList(indexExpr, (List<?>) collection, index);
		} else if (collection instanceof Map<?, ?>) {
			return ((Map<?, ?>) collection).get(index);
		} else if (collection.getClass().isArray()) {
			return accessArray(indexExpr, collection, index);
		} else {
			throw new EvalException("Not an indexed value " + collectionExpr.location() + ": " + collection);
		}
	}

	private static Object accessList(ConfigExpression indexExpr, List<?> list, Object indexValue) {
		int size = list.size();
		int index = normalizeIndex(indexExpr, indexValue, size);
		if (index < 0 || index >= size) {
			return null;
		}
		return list.get(index);
	}

	private static Object accessArray(TemplateExpression indexExpr, Object list, Object indexValue) {
		int size = Array.getLength(list);
		int index = normalizeIndex(indexExpr, indexValue, size);
		if (index < 0 || index >= size) {
			return null;
		}
		return Array.get(list, index);
	}

	private static int normalizeIndex(TemplateExpression indexExpr, Object indexValue, int size) {
		if (!(indexValue instanceof Number)) {
			throw new EvalException("List index must be a number " + indexExpr.location() + ": " + indexValue);
		}
		int index = ((Number) indexValue).intValue();
		if (index < 0) {
			// Access with negative index means accessing relative to the end of the list.
			index = size + index;
		}
		return index;
	}

}
