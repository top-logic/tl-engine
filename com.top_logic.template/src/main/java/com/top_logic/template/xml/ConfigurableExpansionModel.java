/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.xml;

import static com.top_logic.basic.shared.collection.CollectionUtilShared.*;

import java.text.Format;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.col.Maybe;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.data.Function;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.DefaultTypeSystem;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOCollection;
import com.top_logic.dob.meta.MOFunction;
import com.top_logic.dob.meta.TypeSystem;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.template.FormatMap;
import com.top_logic.template.TemplateParseResult;
import com.top_logic.template.TemplateTypes;
import com.top_logic.template.model.ExpansionModel;
import com.top_logic.template.model.TemplateDataObject;
import com.top_logic.template.model.TemplateMOAttribute;
import com.top_logic.template.model.function.EqualsFunction;
import com.top_logic.template.model.function.ExistsFunction;
import com.top_logic.template.model.function.IsEmptyFunction;
import com.top_logic.util.Utils;

/**
 * An {@link ExpansionModel} that gets the types for its {@link TypeSystem} given in the
 * {@link #ConfigurableExpansionModel(TemplateParseResult, Map) constructor}, together with the
 * variable values.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class ConfigurableExpansionModel implements ExpansionModel {

	private static final String VARARGS_VALUE = "value";

	private static final String VARARGS_KEY = "key";

	private static final String VARARGS_PARAMETER = "arguments";

	private final TemplateParseResult _parseResult;

	private final TypeSystem _typeSystem;

	private final TemplateDataObject _thisParameter;

	private final Map<String, Function> _functions = new MapBuilder<String, Function>()
		.put("exists", new ExistsFunction())
		.put("isEmpty", new IsEmptyFunction(this))
		.put("equals", new EqualsFunction())
		.toMap();

	/**
	 * Creates a new {@link ConfigurableExpansionModel}.
	 * 
	 * @param parseResult
	 *        Must not be <code>null</code>.
	 * @param parameterValues
	 *        Is allowed to be <code>null</code>.
	 */
	public ConfigurableExpansionModel(TemplateParseResult parseResult, Map<String, ?> parameterValues) {
		if (parseResult == null) {
			throw new NullPointerException("The parse result must not be null.");
		}
		_parseResult = parseResult;
		_typeSystem = new DefaultTypeSystem(parseResult.getTypes());
		_thisParameter = createParameterStructure(_typeSystem, CollectionUtil.nonNull(parameterValues));
	}

	private TemplateDataObject createParameterStructure(TypeSystem typeSystem, Map<String, ?> variableValues) {
		MetaObject thisType = getType(typeSystem, TemplateTypes.IMPLICIT_THIS_TYPE);
		return createDataObject(thisType, variableValues, typeSystem);
	}

	private TemplateDataObject createDataObject(MetaObject type, Map<String, ?> attributeValues, TypeSystem typeSystem) {
		TemplateDataObject object = new TemplateDataObject(type);
		Set<String> attributesNotSet = new HashSet<>(Arrays.asList(object.getAttributeNames()));
		List<DataObject> varargValues = new ArrayList<>();
		for (Map.Entry<String, ?> attributeValue : attributeValues.entrySet()) {
			String attributeName = attributeValue.getKey();
			if (isVarargsValue(attributeName, type)) {
				varargValues.add(createVarargsEntry(attributeValue, type));
			} else {
				setAttributeValue(object, attributeName, attributeValue.getValue(), typeSystem);
			}

			attributesNotSet.remove(attributeName);
		}
		if (MetaObjectUtils.hasAttribute(type, VARARGS_PARAMETER)) {
			setAttributeValue(object, VARARGS_PARAMETER, varargValues);
		}
		removeAttributesWithDefaultValue(object, attributesNotSet);
		checkNoUnsetParameters(attributesNotSet);
		return object;
	}

	private boolean isVarargsValue(String attributeName, MetaObject type) {
		return MetaObjectUtils.hasAttribute(type, VARARGS_PARAMETER)
			&& !MetaObjectUtils.hasAttribute(type, attributeName);
	}

	private DataObject createVarargsEntry(Map.Entry<String, ?> keyValuePair, MetaObject varargsHolderType) {
		MOAttribute varargsAttribute = getAttribute(varargsHolderType, VARARGS_PARAMETER);
		TemplateDataObject entryObject = new TemplateDataObject(getElementType(varargsAttribute));
		setAttributeValue(entryObject, VARARGS_KEY, keyValuePair.getKey());
		setAttributeValue(entryObject, VARARGS_VALUE, keyValuePair.getValue());
		return entryObject;
	}

	private MetaObject getElementType(MOAttribute collectionAttribute) {
		MOCollection collectionType = (MOCollection) collectionAttribute.getMetaObject();
		return collectionType.getElementType();
	}

	private void removeAttributesWithDefaultValue(TemplateDataObject object, Set<String> attributesNotSet) {
		Iterator<String> iterator = attributesNotSet.iterator();
		while (iterator.hasNext()) {
			String attributeName = iterator.next();
			TemplateMOAttribute attribute = (TemplateMOAttribute) getAttribute(object, attributeName);
			if (attribute.hasDefaultValue()) {
				iterator.remove();
				continue;
			}
			// Structured parameters don't have a default value themselves.
			// Only their attributes have.
			if (isStructuredAttribute(attribute)) {
				iterator.remove();
				continue;
			}
		}
	}

	private void checkNoUnsetParameters(Set<String> attributesNotSet) {
		if (!attributesNotSet.isEmpty()) {
			throw new IllegalArgumentException("The following parameters are not set and have no default value: "
				+ attributesNotSet);
		}
	}

	private boolean isStructuredAttribute(MOAttribute attribute) {
		MetaObject attributeType = attribute.getMetaObject();
		if (attributeType instanceof MOClass) {
			return true;
		}
		if (!(attributeType instanceof MOCollection)) {
			return false;
		}
		MOCollection listAttributeType = (MOCollection) attributeType;
		return listAttributeType.getElementType() instanceof MOClass;
	}

	private void setAttributeValue(
			DataObject object, String variableName, Object rawVariableValue, TypeSystem typeSystem) {
		Object variableValue = StringServices.isEmpty(rawVariableValue) ? null : rawVariableValue;
		MOAttribute attribute = getAttribute(object, variableName);
		MetaObject attributeType = attribute.getMetaObject();
		checkMandatoryConstraint(attribute, variableValue);
		boolean isListAttribute = isListAttribute(attributeType);
		boolean isListValue = (variableValue == null) ? isListAttribute : isListValue(variableValue);
		if (isListAttribute != isListValue) {
			if (isListAttribute) {
				throw new IllegalArgumentException("The attribute '" + variableName
					+ "' is a list, but the value (Class: '" + StringServices.getClassNameNullsafe(variableValue)
					+ "') is not!");
			} else {
				throw new IllegalArgumentException("The value (Class: '"
					+ StringServices.getClassNameNullsafe(variableValue)
					+ "') is a list, but the attribute '" + variableName + "' is not!");
			}
		}
		boolean isStructuredAttribute = isStructuredAttribute(attributeType);
		if (isListAttribute && isListValue) {
			if (isStructuredAttribute) {
				MetaObject elementType = ((MOCollection) attributeType).getElementType();
				List<DataObject> attributeList = new ArrayList<>();
				for (Object entry : nonNull((List<?>) variableValue)) {
					if ((entry != null) && !isStructuredValue(entry)) {
						throw new IllegalArgumentException("The given value for attribute '" + variableName
							+ "' has an incompatible type: " + StringServices.getClassNameNullsafe(variableValue));
					}
					attributeList.add(createDataObject(elementType, entry, typeSystem));
				}
				setAttributeValue(object, variableName, attributeList);
			} else {
				setAttributeValue(object, variableName, new ArrayList<>(nonNull((List<?>) variableValue)));
			}
		} else {
			boolean isStructuredValue =
				(variableValue == null) ? isStructuredAttribute : isStructuredValue(variableValue);
			if (isStructuredAttribute != isStructuredValue) {
				throw new IllegalArgumentException("The given value for attribute '" + variableName
					+ "' has an incompatible type: " + StringServices.getClassNameNullsafe(variableValue));
			}
			if (isStructuredAttribute && isStructuredValue) {
				DataObject attributeObject;
				if (variableValue instanceof TemplateDataObject) {
					Map<String, Object> valueMap = dataObjectToMap((TemplateDataObject) variableValue);
					attributeObject = createDataObject(attributeType, valueMap, typeSystem);
				} else {
					attributeObject = createDataObject(attributeType, variableValue, typeSystem);
				}
				setAttributeValue(object, variableName, attributeObject);
			} else {
				setAttributeValue(object, variableName, variableValue);
			}
		}
	}

	private DataObject createDataObject(MetaObject attributeType, Object attributeValues, TypeSystem typeSystem) {
		if (attributeValues == null) {
			return null;
		}
		// Fail early if one of the keys is not a string.
		CollectionUtil.dynamicCastView(String.class, ((Map<?, ?>) attributeValues).keySet());
		@SuppressWarnings("unchecked")
		Map<String, Object> castedAttributeValues = (Map<String, Object>) attributeValues;
		return createDataObject(attributeType, castedAttributeValues, typeSystem);
	}

	private Map<String, Object> dataObjectToMap(TemplateDataObject dataObject) {
		try {
			Map<String, Object> resultMap = new HashMap<>();
			for (String attributeName : dataObject.getAttributeNames()) {
				resultMap.put(attributeName, dataObject.getValue(attributeName, this));
			}
			return resultMap;
		} catch (NoSuchAttributeException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void checkMandatoryConstraint(MOAttribute attribute, Object variableValue) {
		if (attribute.isMandatory() && isEmptyValue(variableValue)) {
			throw new IllegalArgumentException("Attribute " + attribute.getName()
				+ " is mandatory but has the empty value!");
		}
	}

	private boolean isEmptyValue(Object variableValue) {
		return (variableValue == null) || (Utils.equals(variableValue, ""));
	}

	private boolean isListValue(Object variableValue) {
		return variableValue instanceof List;
	}

	private boolean isListAttribute(MetaObject attributeType) {
		return attributeType instanceof MOCollection;
	}

	private boolean isStructuredValue(Object variableValue) {
		return (variableValue instanceof Map) || (variableValue instanceof DataObject);
	}

	private boolean isStructuredAttribute(MetaObject attributeType) {
		return attributeType instanceof MOClass;
	}

	private MOAttribute getAttribute(DataObject object, String name) {
		MetaObject metaObject = object.tTable();
		return getAttribute(metaObject, name);
	}

	private MOAttribute getAttribute(MetaObject metaObject, String name) {
		try {
			return ((MOClass) metaObject).getAttribute(name);
		} catch (NoSuchAttributeException exception) {
			throw new RuntimeException(exception);
		}
	}

	private void setAttributeValue(DataObject object, String name, Object value) {
		MetaObject attributeType = getAttribute(object, name).getMetaObject();
		try {
			object.setAttributeValue(name, convert(value, attributeType));
		} catch (DataObjectException exception) {
			throw new RuntimeException(exception);
		}
	}

	private Object convert(Object value, MetaObject type) {
		if (type instanceof MOPrimitive) {
			if (type == MOPrimitive.STRING) {
				return value;
			}
			if (type == MOPrimitive.CLOB) {
				return value;
			}
			if (type == MOPrimitive.BOOLEAN) {
				return convertToBoolean(value);
			}
			if (type == MOPrimitive.LONG) {
				return convertToLong(value);
			}
			if (type == MOPrimitive.DOUBLE) {
				return convertToDouble(value);
			}
			if (type == MOPrimitive.DATE) {
				return convertToDate(value);
			}
		}
		if (type instanceof MOCollection) {
			MOCollection collectionType = (MOCollection) type;
			if (value == null) {
				return Collections.emptyList();
			}
			if (value instanceof DataObject) {
				return value;
			}
			if (value instanceof Iterable) {
				List<Object> convertedValues = new ArrayList<>();
				for (Object entry : (Iterable<?>) value) {
					convertedValues.add(convert(entry, collectionType.getElementType()));
				}
				return convertedValues;
			}
		}
		if (type instanceof MOClass) {
			if (value == null) {
				return null;
			}
			if (value instanceof DataObject) {
				return value;
			}
			if (value instanceof Map) {
				return createDataObject(type, value, _typeSystem);
			}
		}
		throw new IllegalArgumentException(
			"Don't know how to convert value '" + value + "' to type '" + type + "'.");
	}

	private Object convertToBoolean(Object value) {
		if (value instanceof Boolean) {
			return value;
		} else {
			return parseValue(value, MOPrimitive.BOOLEAN);
		}
	}

	private Object convertToLong(Object value) {
		if (value instanceof Long) {
			return value;
		} else if (value instanceof Number) {
			return ((Number) value).longValue();
		} else {
			return parseValue(value, MOPrimitive.LONG);
		}
	}

	private Object convertToDouble(Object value) {
		if (value instanceof Double) {
			return value;
		} else if (value instanceof Number) {
			return ((Number) value).doubleValue();
		} else {
			return parseValue(value, MOPrimitive.DOUBLE);
		}
	}

	private Object convertToDate(Object value) {
		if (value instanceof Date) {
			return value;
		} else if (value instanceof Calendar) {
			return ((Calendar) value).getTime();
		} else {
			return parseValue(value, MOPrimitive.DATE);
		}
	}

	private Object parseValue(Object value, MetaObject type) {
		if (StringServices.isEmpty(value)) {
			return null;
		} else if (value instanceof CharSequence) {
			return parse(value.toString(), type);
		} else {
			throw new IllegalArgumentException("Expected a value of type " + type + " but got: "
				+ StringServices.getObjectDescription(value));
		}
	}

	private Object parse(String value, MetaObject targetType) {
		try {
			Format format = getFormatMap().get(targetType);
			if (format == null) {
				throw new RuntimeException("No format found for type: " + targetType.getName());
			} else {
				return format.parseObject(value);
			}
		} catch (ParseException exception) {
			throw new RuntimeException(exception);
		}
	}

	private MetaObject getType(TypeSystem typeSystem, String typeName) {
		try {
			return typeSystem.getType(typeName);
		} catch (UnknownTypeException exception) {
			throw new RuntimeException(exception);
		}
	}

	@Override
	public MetaObject getTypeForVariable(String namespace, String name) {
		if (namespace != null) {
			throw new UnsupportedOperationException("Namespaces are not supported by this class.");
		}
		return getAttribute(_thisParameter, name).getMetaObject();
	}

	@Override
	public Object getValueForVariable(String namespace, String name) {
		if (namespace != null) {
			throw new UnsupportedOperationException("Namespaces are not supported by this class.");
		}
		try {
			return _thisParameter.getValue(name, this);
		} catch (NoSuchAttributeException exception) {
			throw new RuntimeException(exception);
		}
	}

	@Override
	public Object getValueForAttribute(Object object, String attribute) {
		try {
			return ((TemplateDataObject) object).getValue(attribute, this);
		} catch (NoSuchAttributeException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public TypeSystem getTypeSystem() {
		return _typeSystem;
	}

	@Override
	public MOFunction getTypeForFunction(String name) {
		return _functions.get(name).getType();
	}

	@Override
	public Function getImplementationForFunction(String name) {
		return _functions.get(name);
	}

	@Override
	public Iterator<?> getIteratorForObject(Object object) {
		if (object instanceof Iterator) {
			return (Iterator<?>) object;
		}
		if (object instanceof Iterable) {
			return ((Iterable<?>) object).iterator();
		}
		throw new IllegalArgumentException("Cannot create an iterator for an object of type: "
				+ StringServices.getClassNameNullsafe(object));
	}

	/** Getter for the {@link TemplateParseResult}. */
	public TemplateParseResult getParseResult() {
		return _parseResult;
	}

	private FormatMap getFormatMap() {
		return _parseResult.getFormatMap();
	}

	/**
	 * An unmodifiable view of the internal used {@link Map} from {@link Class} to
	 *         {@link Format}.
	 */
	public Map<Class<?>, Format> getFormatClassMap() {
		return getFormatMap().getClassMap();
	}

	/**
	 * An unmodifiable view of the internal used {@link Map} from {@link MetaObject} to
	 *         {@link Format}.
	 */
	public Map<MetaObject, Format> getFormatMetaObjectMap() {
		return getFormatMap().getMetaObjectMap();
	}

	@Override
	public Maybe<String> format(Object value) {
		Class<?> valueClass = (value == null ? null : value.getClass());
		if (getFormatMap().get(valueClass) == null) {
			return Maybe.none();
		}
		return Maybe.toMaybe(getFormatMap().get(valueClass).format(value));
	}
}
