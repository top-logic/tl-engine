/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.LazyTypedAnnotatable;
import com.top_logic.basic.config.XmlDateTimeFormat;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.basic.thread.StackTrace;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.model.search.expr.interpreter.SearchExpressionPart;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.ToString;
import com.top_logic.model.search.expr.visit.Visitor;
import com.top_logic.util.error.TopLogicException;

/**
 * Part of an executable search model.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class SearchExpression extends LazyTypedAnnotatable implements SearchExpressionPart {

	/**
	 * Creates a {@link SearchExpression}.
	 */
	protected SearchExpression() {
		super();
	}

	/**
	 * Evaluates this expression.
	 * 
	 * @param context
	 *        The context information for the search.
	 * @return The evaluation result.
	 */
	public final Object eval(EvalContext context) {
		return evalWith(context, Args.none());
	}

	/**
	 * Evaluates this expression.
	 * 
	 * @param context
	 *        The context information for the search.
	 * @param arg
	 *        The single function argument.
	 * @return The evaluation result.
	 */
	public final Object eval(EvalContext context, Object arg) {
		return evalWith(context, Args.some(arg));
	}

	/**
	 * Evaluates this expression.
	 * 
	 * @param context
	 *        The context information for the search.
	 * @param args
	 *        Function arguments.
	 * @return The evaluation result.
	 */
	public final Object eval(EvalContext context, Object... args) {
		return evalWith(context, Args.some(args));
	}

	/**
	 * Evaluates this expression.
	 * 
	 * @param context
	 *        The context information for the search.
	 * @param args
	 *        Function arguments.
	 * 
	 * @return The evaluation result.
	 */
	public final Object evalWith(EvalContext context, Args args) {
		try {
			return internalCheckValue(internalEval(context, args));
		} catch (ScriptAbort ex) {
			throw ex;
		} catch (I18NRuntimeException ex) {
			throw ex;
		} catch (Error | Exception ex) {
			throw new TopLogicException(
				I18NConstants.ERROR_EVALUATION_FAILED__EXPR_ARGS_DEFS.fill(this, args, context), ex);
		}
	}

	/**
	 * Checks the given value to adhere to TL-Script number semantics.
	 */
	public static Object internalCheckValue(Object result) {
		if (result instanceof Integer || result instanceof Long || result instanceof Short || result instanceof Byte) {
			Logger.error(
				"Numbers in TL-Script must be represented as Double only. Use SearchExpression.toNumber() before returning a number from a TL-Script function.",
				new StackTrace());
			return toNumber(((Number) result).longValue());
		}
		if (result instanceof Float) {
			Logger.error(
				"Numbers in TL-Script must be represented as Double only. Use SearchExpression.toNumber() before returning a number from a TL-Script function.",
				new StackTrace());
			return toNumber(((Number) result).floatValue());
		}
		return result;
	}

	/**
	 * Normalizes a (numeric) value to TL-Script semantics.
	 */
	public static Object normalizeValue(Object result) {
		if (result instanceof Integer || result instanceof Long || result instanceof Short || result instanceof Byte) {
			return toNumber(((Number) result).longValue());
		}
		if (result instanceof Float) {
			return toNumber(((Number) result).floatValue());
		}
		return result;
	}

	/**
	 * Implementation of {@link #evalWith(EvalContext, Args)}
	 */
	protected abstract Object internalEval(EvalContext definitions, Args args);

	/**
	 * Visit method.
	 * 
	 * @param visitor
	 *        The {@link Visitor} to use.
	 * @param arg
	 *        The argument to the visit.
	 * @return The result of the visit.
	 */
	public abstract <R, A> R visit(Visitor<R, A> visitor, A arg);

	/**
	 * Converts the given value to a {@link List}.
	 */
	public static List<?> asList(Object value) {
		if (value instanceof List<?>) {
			return (List<?>) value;
		} else if (value instanceof Collection<?>) {
			return new ArrayList<>((Collection<?>) value);
		} else if (value instanceof Object[]) {
			return Arrays.asList((Object[]) value);
		} else {
			return CollectionUtilShared.singletonOrEmptyList(value);
		}
	}

	/**
	 * Cast to {@link Collection} ensuring a non-<code>null</code> result.
	 * <p>
	 * If the value is not null and not a {@link Collection}, it is put into one. This is a
	 * workaround for search expressions that should return a collection but instead return a single
	 * value. This can happen in navigations, for example. Solving them the right way is not worth
	 * the effort, currently.
	 * </p>
	 */
	public static Collection<?> asCollection(Object value) {
		if (value instanceof Collection<?>) {
			return (Collection<?>) value;
		} else if (value instanceof Map<?, ?>) {
			return ((Map<?, ?>) value).entrySet();
		} else if (value == null) {
			return Collections.emptyList();
		} else if (value.getClass().isArray()) {
			return Arrays.asList((Object[]) value);
		} else {
			return Collections.singletonList(value);
		}
	}

	/**
	 * Unwraps a given single element collection.
	 * 
	 * @param value
	 *        The potential single-element collection.
	 */
	public Object asSingleElement(Object value) {
		return asSingleElement(this, value);
	}

	/**
	 * Unwraps a given single element collection.
	 * 
	 * @param context
	 *        The {@link SearchExpressionPart} context in which the evaluation occurs (for error
	 *        reporting).
	 * @param value
	 *        The potential single-element collection.
	 */
	public static Object asSingleElement(SearchExpressionPart context, Object value) {
		if (value instanceof Collection<?>) {
			Collection<?> collection = (Collection<?>) value;
			int size = collection.size();
			switch (size) {
				case 0:
					return null;
				case 1:
					if (collection instanceof List<?>) {
						return ((List<?>) collection).get(0);
					} else {
						return collection.iterator().next();
					}
				default:
					throw new TopLogicException(
						I18NConstants.ERROR_MORE_THAN_A_SINGLE_ELEMENT__VAL_EXPR.fill(value, context));
			}
		} else {
			return value;
		}
	}

	/**
	 * Converts the given object to an {@link TLObject}.
	 * 
	 * <p>
	 * If the value is an {@link KnowledgeItem} its {@link KnowledgeItem#getWrapper()} is returned.
	 * </p>
	 * 
	 * @param value
	 *        Must be <code>null</code>, a {@link TLObject}, or convertible to an {@link TLObject}.
	 */
	public TLObject asTLObject(Object value) {
		return asTLObject(this, value);
	}

	/**
	 * Converts the given object to an {@link TLObject} and ensures the value is not
	 * <code>null</code>.
	 * 
	 * <p>
	 * If the value is an {@link KnowledgeItem} its {@link KnowledgeItem#getWrapper()} is returned.
	 * </p>
	 * 
	 * @param value
	 *        Must be <code>null</code>, a {@link TLObject}, or convertible to an {@link TLObject}.
	 */
	public TLObject asTLObjectNonNull(Object value) {
		return asTLObjectNonNull(this, value);
	}

	/**
	 * Converts the given object to an {@link TLObject} and ensures the value is not
	 * <code>null</code>.
	 * 
	 * <p>
	 * If the value is an {@link KnowledgeItem} its {@link KnowledgeItem#getWrapper()} is returned.
	 * </p>
	 * 
	 * @param context
	 *        The {@link SearchExpressionPart} context in which the evaluation occurs (for error
	 *        reporting).
	 * @param value
	 *        Must be <code>null</code>, a {@link TLObject}, or convertible to an {@link TLObject}.
	 * 
	 * @see #asTLObject(SearchExpressionPart, Object)
	 */
	public static TLObject asTLObjectNonNull(SearchExpressionPart context, Object value) {
		return notNull(context, asTLObject(context, value));
	}

	/**
	 * Dynamic cast to {@link TLStructuredTypePart} with user-readable error message.
	 *
	 * @param expr
	 *        The evaluated expression.
	 * @param value
	 *        The evaluation value.
	 * @return The given value cast to {@link TLStructuredTypePart}.
	 */
	public static TLStructuredTypePart asTypePart(SearchExpression expr, Object value) {
		if (!(value instanceof TLStructuredTypePart)) {
			throw new TopLogicException(
				I18NConstants.ERROR_NOT_A_TYPE_PART__EXPR_VALUE.fill(expr, value));
		}
		return (TLStructuredTypePart) value;
	}

	/**
	 * Dynamic cast to {@link TLReference} with user-readable error message.
	 *
	 * @param expr
	 *        The evaluated expression.
	 * @param value
	 *        The evaluation value.
	 * @return The given value cast to {@link TLReference}.
	 */
	public static TLReference asReference(SearchExpression expr, Object value) {
		if (!(value instanceof TLReference)) {
			throw new TopLogicException(
				I18NConstants.ERROR_NOT_A_REFERENCE__EXPR_VALUE.fill(expr, value));
		}
		return (TLReference) value;
	}

	/**
	 * Dynamic cast to {@link TLType} with user-readable error message and ensures the value is not
	 * <code>null</code>.
	 *
	 * @param value
	 *        The evaluation value.
	 * @return The given value cast to {@link TLType}.
	 */
	public TLType asTypeNonNull(Object value) {
		return asTypeNonNull(this, value);
	}

	/**
	 * Dynamic cast to {@link TLType} with user-readable error message.
	 *
	 * @param value
	 *        The evaluation value. Must be <code>null</code> or a {@link TLType}.
	 * @return The given value cast to {@link TLType}.
	 */
	public TLType asType(Object value) {
		return asType(this, value);
	}

	/**
	 * Dynamic cast to {@link TLType} with user-readable error message and ensures the value is not
	 * <code>null</code>.
	 *
	 * @param context
	 *        The {@link SearchExpression} context in which the evaluation occurs (for error
	 *        reporting).
	 * @param value
	 *        The evaluation value.
	 * @return The given value cast to {@link TLType}.
	 */
	public static TLType asTypeNonNull(SearchExpression context, Object value) {
		return asType(context, notNull(context, value));
	}

	/**
	 * Dynamic cast to {@link TLType} with user-readable error message.
	 *
	 * @param context
	 *        The {@link SearchExpression} context in which the evaluation occurs (for error
	 *        reporting).
	 * @param value
	 *        The evaluation value. Must be <code>null</code> or a {@link TLType}.
	 * @return The given value cast to {@link TLType}.
	 */
	public static TLType asType(SearchExpression context, Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof TLType) {
			return (TLType) value;
		}
		throw new TopLogicException(I18NConstants.ERROR_NOT_A_TYPE__VAL_EXPR.fill(value, context));
	}

	/**
	 * Dynamic cast to {@link TLStructuredType} with user-readable error message and ensures the
	 * value is not <code>null</code>.
	 *
	 * @param value
	 *        The evaluation value.
	 * @param expr
	 *        The evaluated expression.
	 * @return The given value cast to {@link TLStructuredType}.
	 */
	public static TLStructuredType asStructuredTypeNonNull(Object value, SearchExpression expr) {
		return asStructuredType(notNull(expr, value), expr);
	}

	/**
	 * Dynamic cast to {@link TLStructuredType} with user-readable error message.
	 *
	 * @param value
	 *        The evaluation value. Must be <code>null</code> or a {@link TLStructuredType}.
	 * @param expr
	 *        The evaluated expression.
	 * @return The given value cast to {@link TLStructuredType}.
	 */
	public static TLStructuredType asStructuredType(Object value, SearchExpression expr) {
		if (value == null) {
			return null;
		}
		if (value instanceof TLStructuredType) {
			return (TLStructuredType) value;
		}
		throw new TopLogicException(I18NConstants.ERROR_NOT_A_TYPE__VAL_EXPR.fill(value, expr));
	}

	/** 
	 * Ensures that the given value is non-null.
	 */
	public static <T> T notNull(SearchExpressionPart context, T result) {
		if (result == null) {
			throw new TopLogicException(I18NConstants.ERROR_UNEXPECTED_NULL_VALUE__EXPR.fill(context));
		}
		return result;
	}

	/**
	 * Converts the given object to an {@link TLObject} of a concrete sub-type not allowing
	 * <code>null</code>.
	 * 
	 * <p>
	 * If the value is an {@link KnowledgeItem} its {@link KnowledgeItem#getWrapper()} is returned.
	 * </p>
	 * 
	 * @param expectedType
	 *        The expected type.
	 * @param context
	 *        The {@link SearchExpressionPart} context in which the evaluation occurs (for error
	 *        reporting).
	 * @param value
	 *        Must be <code>null</code>, a {@link TLObject}, or convertible to an {@link TLObject}.
	 */
	public static <T extends TLObject> T asTLObjectNotNull(Class<T> expectedType, SearchExpressionPart context,
			Object value) {
		return notNull(context, asTLObject(expectedType, context, value));
	}

	/**
	 * Converts the given object to an {@link TLObject} of a concrete sub-type.
	 * 
	 * <p>
	 * If the value is an {@link KnowledgeItem} its {@link KnowledgeItem#getWrapper()} is returned.
	 * </p>
	 * 
	 * @param expectedType
	 *        The expected type.
	 * @param context
	 *        The {@link SearchExpressionPart} context in which the evaluation occurs (for error
	 *        reporting).
	 * @param value
	 *        Must be <code>null</code>, a {@link TLObject}, or convertible to an {@link TLObject}.
	 */
	public static <T extends TLObject> T asTLObject(Class<T> expectedType, SearchExpressionPart context, Object value) {
		TLObject result = asTLObject(context, value);
		if (result == null) {
			return null;
		}

		if (!expectedType.isInstance(result)) {
			throw new TopLogicException(I18NConstants.ERROR_UNEXPECTED_TYPE__EXPECTED_VAL_EXPR.fill(expectedType,
				value, context));
		}

		return expectedType.cast(result);
	}

	/**
	 * Converts the given object to an {@link TLObject}.
	 * 
	 * <p>
	 * If the value is an {@link KnowledgeItem} its {@link KnowledgeItem#getWrapper()} is returned.
	 * </p>
	 * 
	 * @param context
	 *        The {@link SearchExpressionPart} context in which the evaluation occurs (for error
	 *        reporting).
	 * @param value
	 *        Must be <code>null</code>, a {@link TLObject}, or convertible to an {@link TLObject}.
	 */
	public static TLObject asTLObject(SearchExpressionPart context, Object value) {
		Object singleton = asSingleElement(context, value);

		TLObject self;
		if (singleton instanceof TLObject) {
			self = (TLObject) singleton;
		} else if (singleton instanceof KnowledgeItem) {
			self = ((KnowledgeItem) singleton).getWrapper();
		} else if (singleton == null) {
			self = null;
		} else {
			throw new TopLogicException(I18NConstants.ERROR_NOT_A_TL_OBJECT__VAL_EXPR.fill(value, context));
		}
		return self;
	}

	/**
	 * Converts the given value to a {@link SearchExpression}.
	 */
	public SearchExpression asSearchExpression(Object value) {
		if (!(value instanceof SearchExpression)) {
			throw new TopLogicException(I18NConstants.ERROR_NOT_A_FUNCTION__VAL_EXPR.fill(value, this));
		}
		return (SearchExpression) value;
	}

	/**
	 * Converts the given value to a {@link Map}.
	 */
	public Map<?, ?> asMap(Object value) {
		if (!(value instanceof Map<?, ?>)) {
			throw new TopLogicException(I18NConstants.ERROR_NOT_A_STRUCT__VAL_EXPR.fill(value, this));
		}
		return (Map<?, ?>) value;
	}

	/**
	 * Converts the given value to a {@link Entry}.
	 */
	public Entry<?, ?> asEntry(Object value) {
		if (!(value instanceof Entry<?, ?>)) {
			throw new TopLogicException(I18NConstants.ERROR_NOT_A_STRUCT_ENTRY__VAL_EXPR.fill(value, this));
		}
		return (Entry<?, ?>) value;
	}

	/**
	 * Converts the given value to an <code>long</code> value.
	 */
	public static long asLong(SearchExpression context, Object value) {
		return asLong(context, value, 0L);
	}

	/**
	 * Converts the given value to an <code>long</code> value with a given default value for
	 * <code>null</code>.
	 */
	public static long asLong(SearchExpression context, Object value, long defaultValue) {
		value = asSingleElement(context, value);
		if (value == null) {
			return defaultValue;
		} else if (value instanceof Number) {
			return ((Number) value).longValue();
		} else if (value instanceof String) {
			try {
				return Long.parseLong((String) value);
			} catch (NumberFormatException ex) {
				throw new TopLogicException(I18NConstants.ERROR_NUMBER_REQUIRED__VALUE_EXPR.fill(value, context), ex);
			}
		} else {
			throw new TopLogicException(I18NConstants.ERROR_NUMBER_REQUIRED__VALUE_EXPR.fill(value, context));
		}
	}

	/**
	 * Converts the given value to an <code>long</code> value.
	 */
	public final long asLong(Object value) {
		return asLong(this, value);
	}

	/**
	 * Converts the given value to an <code>long</code> value with a given default value for
	 * <code>null</code>.
	 */
	public final long asLong(Object value, long defaultValue) {
		return asLong(this, value, defaultValue);
	}

	/**
	 * Converts the given value to an <code>int</code> value.
	 */
	public static int asInt(SearchExpression context, Object value) {
		value = asSingleElement(context, value);
		if (value == null) {
			return 0;
		} else if (value instanceof Number) {
			return ((Number) value).intValue();
		} else if (value instanceof String) {
			try {
				return Integer.parseInt((String) value);
			} catch (NumberFormatException ex) {
				throw new TopLogicException(I18NConstants.ERROR_NUMBER_REQUIRED__VALUE_EXPR.fill(value, context), ex);
			}
		} else {
			throw new TopLogicException(I18NConstants.ERROR_NUMBER_REQUIRED__VALUE_EXPR.fill(value, context));
		}
	}

	/**
	 * Converts the given value to an <code>int</code> value.
	 */
	public int asInt(Object value) {
		return asInt(this, value);
	}

	/**
	 * Converts the given value to an <code>double</code> value.
	 */
	public static double asDouble(SearchExpression context, Object value) {
		value = asSingleElement(context, value);
		if (value == null) {
			return 0.0;
		} else if (value instanceof Number) {
			return ((Number) value).doubleValue();
		} else if (value instanceof String) {
			try {
				return Double.parseDouble((String) value);
			} catch (NumberFormatException ex) {
				throw new TopLogicException(I18NConstants.ERROR_NUMBER_REQUIRED__VALUE_EXPR.fill(value, context), ex);
			}
		} else {
			throw new TopLogicException(I18NConstants.ERROR_NUMBER_REQUIRED__VALUE_EXPR.fill(value, context));
		}
	}

	/**
	 * Converts the given number to an object according to TL-Script number semantics.
	 */
	public static Double toNumber(float value) {
		return Double.valueOf(value);
	}

	/**
	 * Converts the given number to an object according to TL-Script number semantics.
	 * 
	 * <p>
	 * Note: Use this method before returning a potentially numeric value from a TL-Script function.
	 * </p>
	 */
	public static Double toNumber(double value) {
		return Double.valueOf(value);
	}

	/**
	 * Converts the given number to an object according to TL-Script number semantics.
	 * 
	 * <p>
	 * Note: Use this method before returning a numeric value from a TL-Script function.
	 * </p>
	 */
	public static Double toNumber(int value) {
		if (value >= MIN && value <= MAX) {
			return NUMBER_CACHE[value - MIN];
		}
		return Double.valueOf(value);
	}

	/**
	 * Converts the given number to an object according to TL-Script number semantics.
	 * 
	 * <p>
	 * Note: Use this method before returning a numeric value from a TL-Script function.
	 * </p>
	 */
	public static Double toNumber(long value) {
		if (value >= MIN && value <= MAX) {
			return NUMBER_CACHE[((int) value) - MIN];
		}
		return Double.valueOf(value);
	}

	/**
	 * Converts the given value to an <code>double</code> value.
	 */
	public double asDouble(Object value) {
		return asDouble(this, value);
	}

	/**
	 * Converts the given value to an <code>Boolean</code> value in the most fuzzy sense.
	 */
	public static Boolean asBoolean(Object value) {
		return Boolean.valueOf(isTrue(value));
	}

	/**
	 * Whether the given value is considered to be equivalent to <code>true</code> in a boolean
	 * context.
	 */
	public static boolean isTrue(Object value) {
		if (value == null) {
			return false;
		}
		if (value instanceof Boolean) {
			return ((Boolean) value).booleanValue();
		}
		if (value instanceof Collection<?>) {
			return !((Collection<?>) value).isEmpty();
		}
		if (value instanceof CharSequence) {
			return ((CharSequence) value).length() > 0;
		}
		return true;
	}

	/**
	 * Converts the given value to a {@link String} value.
	 */
	public final String asString(Object value) {
		return asString(value, "");
	}

	/**
	 * Converts the given value to a {@link String} value with a given default value to use for
	 * <code>null</code>.
	 */
	public final String asString(Object value, String defaultValue) {
		value = asSingleElement(value);
		if (value == null) {
			return defaultValue;
		}
		if (value instanceof Double) {
			double x = ((Double) value).doubleValue();
			if (Math.floor(x) == x) {
				// Do not insist in marking numbers as floating point values. In TL-Script, all
				// numbers are treated as Double internally.
				return Long.toString((long) x);
			}
		}
		return value.toString();
	}
	
	/**
	 * Converts the given value to a {@link Date}.
	 * 
	 * <p>
	 * Supported argument types are {@link Date}, {@link Calendar}, {@link Number}.
	 * </p>
	 */
	public final Date asDate(Object self) {
		if (self instanceof Date) {
			return (Date) self;
		} else if (self instanceof Calendar) {
			return ((Calendar) self).getTime();
		} else if (self instanceof Number) {
			return new Date(((Number) self).longValue());
		} else if (self instanceof CharSequence) {
			try {
				return (Date) XmlDateTimeFormat.INSTANCE.parseObject(self.toString());
			} catch (ParseException ex) {
				throw new TopLogicException(
					I18NConstants.ERROR_INVALID_DATE_FORMAT__VAL_EXPR_MSG.fill(self, this, ex.getMessage()));
			}
		} else {
			throw new TopLogicException(I18NConstants.ERROR_NOT_A_DATE__VAL_EXPR.fill(self, this));
		}
	}

	/**
	 * Converts the given value to a {@link Calendar} or reports an error, if this is not possible.
	 */
	public final Calendar asCalendar(Object self) {
		if (self instanceof Calendar) {
			return (Calendar) self;
		}

		throw new TopLogicException(I18NConstants.ERROR_NOT_A_CALENDAR__VAL_EXPR.fill(self, this));
	}

	/**
	 * Converts the given value to a {@link ResKey}.
	 * 
	 * @param value
	 *        The value to convert to {@link ResKey} instance; either a {@link ResKey},
	 *        <code>null</code>, or a {@link String}.
	 * @param expression
	 *        The expression representing the given value.
	 * @return A {@link ResKey} for the given value.
	 */
	public static ResKey asResKeyNotNull(Object value, SearchExpression expression) {
		if (value instanceof ResKey) {
			return (ResKey) value;
		}
		if (value instanceof String || value == null) {
			return ResKey.text((String) value);
		}
		throw new TopLogicException(I18NConstants.ERROR_NOT_A_RES_KEY__VALUE__EXPR.fill(value, expression));
	}

	/**
	 * Converts the given value to a {@link ThemeImage}.
	 * 
	 * @param value
	 *        The value to convert to {@link ThemeImage} instance; either a {@link ThemeImage},
	 *        <code>null</code>, or a {@link String}.
	 * @return A {@link ThemeImage} for the given value.
	 */
	public ThemeImage asImage(Object value) {
		if (value == null) {
			return ThemeImage.none();
		}
		return asImageOptional(value);
	}

	/**
	 * Converts the given value to a {@link ThemeImage}.
	 * 
	 * @param value
	 *        The value to convert to {@link ThemeImage} instance; either a {@link ThemeImage},
	 *        <code>null</code>, or a {@link String}.
	 * @return A {@link ThemeImage} for the given value, <code>null</code> for <code>null</code>.
	 */
	public ThemeImage asImageOptional(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof ThemeImage) {
			return (ThemeImage) value;
		}
		if (value instanceof String) {
			return ThemeImage.internalDecode((String) value);
		}
		throw new TopLogicException(I18NConstants.ERROR_NOT_A_THEME_IMAGE__VALUE__EXPR.fill(value, this));
	}

	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		try {
			visit(ToString.INSTANCE, out);
		} catch (Exception ex) {
			// Without thread context, printing model part names fails.
			out.append("ERROR: ");
			out.append(ex.getClass().getName());
			out.append(": ");
			out.append(ex.getMessage());
		}
		return out.toString();
	}

	/**
	 * The identifier corresponding to {@link MethodBuilder#getId()}.
	 */
	public Object getId() {
		return getClass();
	}

	/**
	 * Converts the given value to a {@link Locale}.
	 * 
	 * @param lang
	 *        The value to convert to a locale: Either a {@link Locale} (then only its
	 *        {@link Locale#getLanguage() language} is used) or a {@link #asString(Object) string}.
	 */
	protected Locale asLocale(Object lang) {
		String language;
		if (lang instanceof Locale) {
			language = ((Locale) lang).getLanguage();
		} else {
			language = asString(lang);
		}
		return ResourcesModule.localeFromString(language);
	}

	private static final int MIN = -128;

	private static final int MAX = 127;

	private static final Double[] NUMBER_CACHE;

	static {
		int cnt = MAX - MIN + 1;
		Double[] numbers = new Double[cnt];
		for (int num = MIN; num <= MAX; num++) {
			numbers[num - MIN] = Double.valueOf(num);
		}
		NUMBER_CACHE = numbers;
	}
}
