/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.gantt.component.form;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.json.JSON;
import com.top_logic.basic.json.JSON.ParseException;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.mig.html.I18NResourceProvider;
import com.top_logic.reporting.view.component.property.FilterProperty;
import com.top_logic.tool.boundsec.BoundComponent;
import com.top_logic.util.Utils;

/**
 * {@link FilterProperty} for {@link List}s of {@link String}s.
 * 
 * @author <a href="mailto:mga@top-logic.com">mga</a>
 */
public class StringListSelectorProperty extends FilterProperty {

	private final ResPrefix _resPrefix;

	private List<String> _options;

	private boolean _multiple;

	/**
	 * Creates a new {@link StringListSelectorProperty}.
	 */
	public StringListSelectorProperty(String name, String initialValue, List<String> options, BoundComponent component) {
		this(name, initialValue, options, component, false);
	}
	
	/**
	 * Creates a new {@link StringListSelectorProperty}.
	 */
	public StringListSelectorProperty(String name, String initialValue, List<String> options, BoundComponent component, boolean multiple) {
		super(name, toInitialValue(initialValue), component);
		_resPrefix = component.getResPrefix();
		_options = options;
		_multiple = multiple;
		if (_multiple) {
			/* Workaround: The initial value is loaded in the constructor of the super class. At
			 * this time the property "_multiple" is not set! Therefore the JSON value is not
			 * deserialized. */
			loadFromPersonalConfiguration(getConfigBaseName(), this::setNonNullInitialValue);
		}
	}

	private static List<String> toInitialValue(String initialValue) {
		if (initialValue == null) {
			return null;
		}
		return Collections.singletonList(initialValue);
	}

	@Override
	protected AbstractFormField createNewFormMember() {
		SelectField selectField = FormFactory.newSelectField(getName(), _options, _multiple, false);
		selectField.setAsSelection((List) getInitialValue());
		selectField.setOptionLabelProvider(new I18NResourceProvider(_resPrefix));
		selectField.setCustomOrder(true);
		return selectField;
	}

	@Override
	protected Object getValueForPersonalConfiguration(Object fieldValue) {
		if (fieldValue == null) {
			return null;
		}
		if (_multiple) {
			return JSON.toString(fieldValue);
		} else {
			return Utils.resolveCollection(fieldValue);
		}
	}

	@Override
	protected Object getValueFromPersonalConfiguration(Object confValue) {
		if (confValue == null) {
			return null;
		}
		if (_multiple) {
			CharSequence jsonString = (CharSequence) confValue;
			try {
				return JSON.fromString(jsonString);
			} catch (ParseException ex) {
				/* Invalid personal configuration value. Reset it and inform the user. */
				InfoService.showWarning(
					I18NConstants.ERROR_INVALID_FORMAT_STRING_LIST__PROPERTY__VALUE.fill(getName(), jsonString));
				saveToPersonalConfig(getConfigBaseName(), null);
				return Collections.emptyList();
			}
		} else {
			return Collections.singletonList(confValue);
		}
	}

}
