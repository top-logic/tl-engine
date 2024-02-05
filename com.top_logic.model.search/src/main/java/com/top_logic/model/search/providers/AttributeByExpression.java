/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import static com.top_logic.model.search.expr.query.QueryExecutor.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocator;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.search.persistency.attribute.AbstractExpressionAttribute;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link AttributeValueLocator} that can be configured using search expressions.
 * 
 * @see Config#getExpr()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
public class AttributeByExpression<C extends AttributeByExpression.Config<?>> extends AbstractExpressionAttribute<C> {

	/**
	 * Configuration options for {@link AttributeByExpression}.
	 */
	@TagName("query")
	public interface Config<I extends AttributeByExpression<?>> extends AbstractExpressionAttribute.Config<I> {
		// Pure marker interface.
	}

	/**
	 * Creates a {@link AttributeByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AttributeByExpression(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public Object getAttributeValue(TLObject object, TLStructuredTypePart attribute) throws AttributeException {
		Object result = executor(attribute.tKnowledgeBase(), attribute.getModel(), getExpr()).execute(object);
		return convertAndCheck(object, attribute, result);
	}

	private Object convertAndCheck(TLObject object, TLStructuredTypePart attribute, Object result) {
		TLType type = attribute.getType();
		if (attribute.isMultiple()) {
			return checkCollection(object, attribute, type, attribute.isMandatory(), toCollection(attribute, result));
		} else {
			return normalizeValue(object, attribute, type, attribute.isMandatory(), toSingleElement(attribute, result));
		}
	}

	private Collection<?> checkCollection(TLObject object, TLStructuredTypePart attribute, TLType type,
			boolean mandatory, Collection<?> collection) {
		for (Object element : collection) {
			checkValue(object, attribute, type, true, element);
		}
		if (mandatory && collection.isEmpty()) {
			throw new TopLogicException(
				I18NConstants.ERROR_SCRIPT_DELIVERED_NO_RESULT_FOR_MANDATORY_ARRTIBUTE__ATTR_OBJ.fill(attribute,
					object));
		}
		return collection;
	}

	private Object normalizeValue(TLObject object, TLStructuredTypePart attribute, TLType type, boolean mandatory,
			Object element) {
		if (type.getModelKind() == ModelKind.DATATYPE) {
			TLPrimitive primitiveType = (TLPrimitive) type;
			StorageMapping<?> mapping = primitiveType.getStorageMapping();

			// Normalize value.
			Object storage = mapping.getStorageObject(element);
			return mapping.getBusinessObject(storage);
		} else {
			checkValue(object, attribute, type, mandatory, element);
			return element;
		}
	}

	private void checkValue(TLObject object, TLStructuredTypePart attribute, TLType type, boolean mandatory, Object element) {
		if (element == null) {
			if (mandatory) {
				throw new TopLogicException(
					I18NConstants.ERROR_SCRIPT_DELIVERED_NO_RESULT_FOR_MANDATORY_ARRTIBUTE__ATTR_OBJ
						.fill(TLModelUtil.qualifiedName(attribute), object));
			}
		} else {
			if (!TLModelUtil.isCompatibleInstance(type, element)) {
				throw new TopLogicException(
					I18NConstants.ERROR_SCRIPT_RESULT_OF_INCOMPATIBLE_TYPE__ATTR_EXPECTED_ACTUAL.fill(
						TLModelUtil.qualifiedName(attribute),
						type, element instanceof TLObject ? ((TLObject) element).tType() : element));
			}
		}
	}

	private Collection<?> toCollection(TLStructuredTypePart attribute, Object result) {
		if (attribute.isOrdered() || attribute.isBag()) {
			if (result instanceof List<?>) {
				return (List<?>) result;
			} else if (result instanceof Collection<?>) {
				return new ArrayList<Object>((Collection<?>) result);
			} else {
				return CollectionUtilShared.singletonOrEmptyList(result);
			}
		} else {
			if (result instanceof Set<?>) {
				return (Set<?>) result;
			} else if (result instanceof Collection<?>) {
				return new HashSet<Object>((Collection<?>) result);
			} else {
				return CollectionUtilShared.singletonOrEmptySet(result);
			}
		}
	}

	private Object toSingleElement(TLStructuredTypePart attribute, Object result) {
		if (result instanceof Collection<?>) {
			Collection<?> c = (Collection<?>) result;
			switch (c.size()) {
				case 0:
					return null;
				case 1:
					return c.iterator().next();
				default:
					throw new TopLogicException(
						I18NConstants.ERROR_SCRIPT_RESULT_IS_COLLECTION__ATTR_VALUE.fill(attribute, result));
			}
		} else {
			return result;
		}
	}

}
