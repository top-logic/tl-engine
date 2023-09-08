/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.StringServices;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.data.DefaultDataObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOCollection;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.template.tree.parameter.ListParameterValue;
import com.top_logic.template.tree.parameter.ParameterValue;
import com.top_logic.template.tree.parameter.PrimitiveParameterValue;
import com.top_logic.template.tree.parameter.StructuredParameterValue;
import com.top_logic.template.xml.ConfigurableExpansionModel;

/**
 * An implementation of {@link DefaultDataObject} with special behavior for the template engine.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class TemplateDataObject extends DefaultDataObject {

	private StructuredParameterValue<Object> _defaultValue;

	private final Set<String> _initialisedAttributes = new HashSet<>();

	/**
	 * Creates a new {@link TemplateDataObject}.
	 */
	public TemplateDataObject(MetaObject anObject) {
		super(anObject);
	}

	/**
	 * Not supported by {@link TemplateDataObject}.
	 * 
	 * @deprecated Use {@link #getValue(String, ConfigurableExpansionModel)} instead.
	 * @throws UnsupportedOperationException
	 *         Always & Unconditionally
	 */
	@Deprecated
	@Override
	public Object getAttributeValue(String attrName) throws NoSuchAttributeException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported by {@link TemplateDataObject}.
	 * 
	 * @deprecated Use {@link #getValue(MOAttribute, ConfigurableExpansionModel)} instead.
	 * @throws UnsupportedOperationException
	 *         Always & Unconditionally
	 */
	@Deprecated
	@Override
	public Object getValue(MOAttribute attribute) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object setAttributeValue(String attrName, Object value) throws DataObjectException {
		Object result = super.setAttributeValue(attrName, value);
		_initialisedAttributes.add(attrName);
		return result;
	}

	@Override
	public Object setValue(MOAttribute attribute, Object newValue) throws DataObjectException {
		Object result = super.setValue(attribute, newValue);
		_initialisedAttributes.add(attribute.getName());
		return result;
	}

	/**
	 * Replacement for {@link #getAttributeValue(String)}, as the {@link ConfigurableExpansionModel}
	 * is needed to expand default values.
	 */
	public Object getValue(String attributeName, ConfigurableExpansionModel expansionModel)
			throws NoSuchAttributeException {
		MOAttribute attribute = MetaObjectUtils.getAttribute(tTable(), attributeName);
		return getValue(attribute, expansionModel);
	}

	/**
	 * Replacement for {@link #getValue(MOAttribute)}, as the {@link ConfigurableExpansionModel} is
	 * needed to expand default values.
	 */
	public Object getValue(MOAttribute attribute, ConfigurableExpansionModel expansionModel) {
		if (!_initialisedAttributes.contains(attribute.getName())) {
			initAttributeValue(attribute, expansionModel);
			_initialisedAttributes.add(attribute.getName());
		}
		return attribute.getStorage().getApplicationValue(attribute, this, this, storage);
	}

	private void initAttributeValue(MOAttribute attribute, ConfigurableExpansionModel expansionModel) {
		TemplateMOAttribute templateAttribute = (TemplateMOAttribute) attribute;
		if (hasLocalDefaultValue(attribute)) {
			initAttributeValue(attribute, _defaultValue.getStructuredValue().get(attribute.getName()));
		} else if (templateAttribute.hasDefaultValue()) {
			ParameterValue<Object> value = templateAttribute.getDefaultValue(expansionModel);
			initAttributeValue(attribute, value);
		} else if (attribute.getMetaObject() instanceof MOClass) {
			TemplateDataObject innerDataObject = new TemplateDataObject(attribute.getMetaObject());
			attribute.getStorage().initApplicationValue(attribute, this, this, storage, innerDataObject);
		}
	}

	private boolean hasLocalDefaultValue(MOAttribute attribute) {
		return (_defaultValue != null) && _defaultValue.getStructuredValue().containsKey(attribute.getName());
	}

	private void initAttributeValue(MOAttribute attribute, ParameterValue<Object> value) {
		MetaObject attributeType = attribute.getMetaObject();
		if (value.isPrimitiveValue()) {
			if (StringServices.isEmpty(value.asPrimitiveValue().getPrimitiveValue())) {
				attribute.getStorage().initApplicationValue(attribute, this, this, storage, null);
				return;
			}
			if (!(attributeType instanceof MOPrimitive)) {
				throw new RuntimeException("Got a primitive value for an attribute of type: "
					+ StringServices.getObjectDescription(attributeType));
			}
			PrimitiveParameterValue<Object> primitiveValue = value.asPrimitiveValue();
			attribute.getStorage().initApplicationValue(attribute, this, this, storage,
				primitiveValue.getPrimitiveValue());
			return;
		}
		if (value.isListValue()) {
			if (!(attributeType instanceof MOCollection)) {
				throw new RuntimeException("Got a list value for an attribute of type: "
					+ StringServices.getObjectDescription(attributeType));
			}
			MetaObject elementType = ((MOCollection) attributeType).getElementType();
			ListParameterValue<Object> listValue = value.asListValue();
			List<Object> finalValues = new ArrayList<>();
			for (ParameterValue<Object> entry : listValue.getListValue()) {
				if (entry.isListValue()) {
					throw new RuntimeException("Lists cannot be nested directly.");
				}
				if (entry.isPrimitiveValue()) {
					finalValues.add(entry.asPrimitiveValue().getPrimitiveValue());
				} else if (entry.isStructuredValue()) {
					TemplateDataObject entryDataObject = new TemplateDataObject(elementType);
					entryDataObject.setDefaultValue(entry.asStructuredValue());
					finalValues.add(entryDataObject);
				} else {
					throw new UnsupportedOperationException("Unknown " + ParameterValue.class.getSimpleName()
						+ " instance: " + StringServices.getObjectDescription(value));
				}
			}
			attribute.getStorage().initApplicationValue(attribute, this, this, storage, finalValues);
			return;
		}
		if (value.isStructuredValue()) {
			if (!(attributeType instanceof MOClass)) {
				throw new RuntimeException("Got a structured value for an attribute of type: "
					+ StringServices.getObjectDescription(attributeType));
			}
			TemplateDataObject innerDataObject = new TemplateDataObject(attributeType);
			innerDataObject.setDefaultValue(value.asStructuredValue());
			attribute.getStorage().initApplicationValue(attribute, this, this, storage, innerDataObject);
			return;
		}
		throw new UnsupportedOperationException("Unknown " + ParameterValue.class.getSimpleName()
			+ " instance: " + StringServices.getObjectDescription(value));
	}

	/**
	 * Used to set the default values on "inner" objects.
	 * <p>
	 * (If this object represents a list or structure.)
	 * </p>
	 */
	private void setDefaultValue(StructuredParameterValue<Object> defaultValue) {
		assert _defaultValue == null : "Default value has already been set.";
		_defaultValue = defaultValue;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + super.toString() + ")";
	}

}
