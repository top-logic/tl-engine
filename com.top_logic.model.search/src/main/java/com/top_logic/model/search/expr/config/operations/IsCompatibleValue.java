/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.func.misc.IsEmpty;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link SearchExpression} testing whether a value can be assigned to a variable of the given
 * {@link TLType}. This works for both {@link TLObject}s and primitive types (String, Number,
 * Boolean, etc).
 *
 * <p>
 * Supports checking single values or collections of values against {@link TLClass}es,
 * {@link TLStructuredTypePart} or {@link TLPrimitive}s. For collections, all items must be
 * compatible with the target {@link TLType}.
 * </p>
 *
 * @author <a href="mailto:jhu@top-logic.com">Jonathan Hüsing</a>
 */
public class IsCompatibleValue extends GenericMethod {

	/** Creates an {@link IsCompatibleValue}. */
	protected IsCompatibleValue(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new IsCompatibleValue(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.BOOLEAN_TYPE);
	}

	/**
	 * Evaluates compatibility between a value and target {@link TLType}.
	 * 
	 * <p>
	 * First argument is the value to check, second argument is the target type specification
	 * ({@link TLPrimitive}, {@link TLClass} or {@link TLStructuredTypePart}).
	 * </p>
	 * 
	 * @param arguments
	 *        evaluation arguments: value and target type specification
	 * @param definitions
	 *        current evaluation context
	 * @return true if the value is compatible with the target type, false otherwise
	 */
	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		Object valueToCheck = arguments[0];
		Object target = arguments[1];

		if (valueToCheck instanceof Collection) {
			return checkCollection((Collection<?>) valueToCheck, target);
		} else {
			return isCompatible(valueToCheck, target);
		}
	}

	/**
	 * Checks if all items in a collection are compatible with the target {@link TLType}.
	 * 
	 * @param collection
	 *        collection of values to check
	 * @param target
	 *        type specification to check against
	 * @return true if all items are compatible, false otherwise
	 */
	private boolean checkCollection(Collection<?> collection, Object target) {
		// First check if target supports collections
		if (target instanceof TLStructuredTypePart) {
			TLStructuredTypePart part = (TLStructuredTypePart) target;

			// Check if the empty collection is allowed for mandatory fields
			if (collection.isEmpty()) {
				return !part.isMandatory();
			}
			
			// For multiple references, check for duplicates if not a bag
			if (part.isMultiple() && !part.isBag() && (part instanceof TLReference) && hasDuplicates(collection)) {
				return false;
			}

			if (!part.isMultiple() && collection.size() != 1) {
				// Non-multiple parts only accept collections with exactly one element
					return false;
			}

			// For mandatory multiple parts, we need at least one valid entry and no invalid
			// entries
			if (part.isMandatory() && part.isMultiple()) {
				boolean hasValidEntry = false;

				for (Object item : collection) {
					if (item == null) {
						continue;
					}

					if (!isCompatible(item, part)) {
						return false; // Found an invalid entry
					}

					hasValidEntry = true; // Found at least one valid entry
				}

				return hasValidEntry;
			}
		}

		// Then check each item
		for (Object item : collection) {
			if (!isCompatible(item, target)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks compatibility between a single value and a target {@link TLType}.
	 * 
	 * @param valueToCheck
	 *        single value to check compatibility
	 * @param target
	 *        type definition to check against
	 * @return true if compatible, false otherwise
	 * @throws TopLogicException
	 *         if target is of unexpected type
	 */
	private boolean isCompatible(Object valueToCheck, Object target) {
		// Handle the TLStructuredTypePart case first
		if (target instanceof TLStructuredTypePart) {
			return handleStructuredTypePart(valueToCheck, (TLStructuredTypePart) target);
		}

		// Then handle the type-based cases
		if (target instanceof TLClass) {
			return checkAgainstClass(valueToCheck, (TLClass) target);
		} else if (target instanceof TLPrimitive) {
			return checkAgainstPrimitive(valueToCheck, (TLPrimitive) target);
		} else {
			throw new TopLogicException(
				I18NConstants.ERROR_WRONG_TARGET__ISCOMPATIBLEVALUE.fill(asString(valueToCheck), asString(target)));
		}
	}

	/**
	 * Checks compatibility with a {@link TLStructuredTypePart}.
	 * 
	 * @param valueToCheck
	 *        value to check
	 * @param part
	 *        structured type part to check against
	 * @return true if compatible, false otherwise
	 */
	private boolean handleStructuredTypePart(Object valueToCheck, TLStructuredTypePart part) {
		// Check mandatory constraint
		if (IsEmpty.isEmpty(valueToCheck)) {
			return !part.isMandatory();
		}

		// Check against the actual type
		TLType type = part.getType();
		if (type instanceof TLClass) {
			return checkAgainstClass(valueToCheck, (TLClass) type);
		} else if (type instanceof TLPrimitive) {
			return checkAgainstPrimitive(valueToCheck, (TLPrimitive) type);
		}

		return false;
	}

	/**
	 * Checks compatibility with a {@link TLClass}.
	 * 
	 * @param valueToCheck
	 *        value to check
	 * @param typeClass
	 *        class to check against
	 * @return true if compatible, false otherwise
	 */
	private boolean checkAgainstClass(Object valueToCheck, TLClass typeClass) {
		// Only TLObjects can be compatible with classes
		if (valueToCheck instanceof TLObject) {
			TLObject tlObject = (TLObject) valueToCheck;
			return TLModelUtil.isCompatibleType(asType(typeClass), tlObject.tType());
		}
		return false;
	}

	/**
	 * Checks compatibility with a {@link TLPrimitive}.
	 * 
	 * @param valueToCheck
	 *        value to check
	 * @param primitive
	 *        primitive type to check against
	 * @return true if compatible, false otherwise
	 */
	private boolean checkAgainstPrimitive(Object valueToCheck, TLPrimitive primitive) {
		// TLObjects are never compatible with primitive types
		if (valueToCheck instanceof TLObject) {
			return false;
		}
		return primitive.getStorageMapping().isCompatible(valueToCheck);
	}

	/**
	 * Checks if a collection contains duplicate values.
	 * 
	 * @param collection
	 *        the collection to check
	 * @return true if the collection contains duplicates, false otherwise
	 */
	private boolean hasDuplicates(Collection<?> collection) {
		Set<Object> uniqueItems = new HashSet<>();

		// Count null values separately
		int nullCount = 0;

		for (Object item : collection) {
			if (item == null) {
				nullCount++;
				if (nullCount > 1) {
					return true; // Found duplicate null
				}
			} else if (!uniqueItems.add(item)) {
				return true; // Found duplicate non-null
			}
		}

		return false;
	}

	/** {@link MethodBuilder} creating {@link IsCompatibleValue}. */
	public static final class Builder extends AbstractSimpleMethodBuilder<IsCompatibleValue> {

		/** Description of parameters for a {@link IsCompatibleValue}. */
		public static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("value")
			.mandatory("target")
			.build();

		/** Creates a {@link Builder}. */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}

		@Override
		public IsCompatibleValue build(Expr expr, SearchExpression[] args) throws ConfigurationException {
			checkTwoArgs(expr, args);
			return new IsCompatibleValue(getConfig().getName(), args);
		}
	}

}