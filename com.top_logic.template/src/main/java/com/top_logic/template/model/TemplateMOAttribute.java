/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.model;

import java.text.Format;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.StringServices;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOCollection;
import com.top_logic.template.expander.TemplateExpander;
import com.top_logic.template.tree.Template;
import com.top_logic.template.tree.TemplateNode;
import com.top_logic.template.tree.parameter.ListParameterValue;
import com.top_logic.template.tree.parameter.ParameterValue;
import com.top_logic.template.tree.parameter.PrimitiveParameterValue;
import com.top_logic.template.tree.parameter.StructuredParameterValue;
import com.top_logic.template.xml.ConfigurableExpansionModel;

/**
 * An implementation of {@link MOAttributeImpl} with special behavior for the template engine.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class TemplateMOAttribute extends MOAttributeImpl {

	private ParameterValue<TemplateNode> _defaultValueTemplate;

	private ParameterValue<Object> _defaultValue;

	private boolean _defaultValueExpanded = false;

	/**
	 * Creates a new {@link TemplateMOAttribute} like
	 * {@link MOAttributeImpl#MOAttributeImpl(String, MetaObject, boolean)}, but additionally stores
	 * a default value {@link Template} {@link List}.
	 * <p>
	 * That default value template is expanded when it's first requested via
	 * {@link #getDefaultValue(ConfigurableExpansionModel)}.
	 * </p>
	 * 
	 * @param defaultValueTemplates
	 *        A {@link List} of default value templates or <code>null</code> if there is no default
	 *        value.
	 */
	public TemplateMOAttribute(
			String aName, MetaObject aType, ParameterValue<TemplateNode> defaultValueTemplates, boolean mandatory) {
		super(aName, aType, mandatory);
		_defaultValueTemplate = defaultValueTemplates;
	}

	/**
	 * Getter for the default value for this attribute.
	 * <p>
	 * Returns <code>null</code> if there is no default value. <br/>
	 * Returns an empty {@link List} if the default value is the empty value.
	 * </p>
	 */
	ParameterValue<Object> getDefaultValue(ConfigurableExpansionModel expansionModel) {
		if (!_defaultValueExpanded) {
			_defaultValue = expand(_defaultValueTemplate, expansionModel);
			_defaultValueTemplate = null; // Free up unneeded memory
			_defaultValueExpanded = true;
		}
		return _defaultValue;
	}

	private ParameterValue<Object> expand(
			ParameterValue<TemplateNode> template, ConfigurableExpansionModel expansionModel) {
		MetaObject metaObject = getMetaObject();
		return expand(template, metaObject, expansionModel);
	}

	private ParameterValue<Object> expand(
			ParameterValue<TemplateNode> template, MetaObject type, ConfigurableExpansionModel expansionModel) {
		if (template == null) {
			return null;
		}
		if (template.isPrimitiveValue()) {
			return expandPrimitive(template.asPrimitiveValue(), type, expansionModel);
		} else if (template.isListValue()) {
			return expandList(template.asListValue(), (MOCollection) type, expansionModel);
		} else if (template.isStructuredValue()) {
			return expandStructuredValue(template.asStructuredValue(), (MOClass) type, expansionModel);
		} else {
			throw new UnsupportedOperationException("Unknown " + ParameterValue.class.getSimpleName() + " instance: "
				+ StringServices.getObjectDescription(template));
		}
	}

	private ParameterValue<Object> expandPrimitive(
			PrimitiveParameterValue<TemplateNode> templateValue, MetaObject type,
			ConfigurableExpansionModel expansionModel) {

		Format format = expansionModel.getFormatMetaObjectMap().get(type);
		TemplateNode templateSnippet = templateValue.getPrimitiveValue();
		if (templateSnippet == null) {
			return new PrimitiveParameterValue<>(null);
		} else {
			String valueAsString = TemplateExpander.expand(templateSnippet, expansionModel);
			return new PrimitiveParameterValue<>(parseValue(format, valueAsString));
		}
	}

	private ParameterValue<Object> expandList(
			ListParameterValue<TemplateNode> template, MOCollection type, ConfigurableExpansionModel expansionModel) {

		List<ParameterValue<TemplateNode>> valueList = template.getListValue();
		List<ParameterValue<Object>> expandedValues = new ArrayList<>();

		for (ParameterValue<TemplateNode> valueEntry : valueList) {
			expandedValues.add(expand(valueEntry, type.getElementType(), expansionModel));
		}
		return new ListParameterValue<>(expandedValues);
	}

	private ParameterValue<Object> expandStructuredValue(
			StructuredParameterValue<TemplateNode> template, MOClass type, ConfigurableExpansionModel expansionModel) {

		Map<String, ParameterValue<TemplateNode>> valueObject = template.getStructuredValue();
		Map<String, ParameterValue<Object>> expandedObject = new HashMap<>();

		for (Map.Entry<String, ParameterValue<TemplateNode>> valueAttribute : valueObject.entrySet()) {
			String attributeName = valueAttribute.getKey();
			MetaObject attributeType = getAttribute(type, attributeName).getMetaObject();
			ParameterValue<TemplateNode> attributeTemplate = valueAttribute.getValue();
			ParameterValue<Object> expandedAttribute = expand(attributeTemplate, attributeType, expansionModel);
			expandedObject.put(attributeName, expandedAttribute);
		}
		return new StructuredParameterValue<>(expandedObject);
	}

	private MOAttribute getAttribute(MOClass type, String attributeName) {
		try {
			return type.getAttribute(attributeName);
		} catch (NoSuchAttributeException ex) {
			throw new RuntimeException(ex);
		}
	}

	private Object parseValue(Format format, String valueAsString) {
		try {
			if (StringServices.isEmpty(valueAsString)) {
				// As "" and null are equivalent for String types (String & XML),
				// this is even correct for "".
				return null;
			}
			return format.parseObject(valueAsString);
		} catch (ParseException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Is a default value available for this attribute?
	 */
	public boolean hasDefaultValue() {
		if (_defaultValueExpanded) {
			return _defaultValue != null;
		}
		return _defaultValueTemplate != null;
	}

}
