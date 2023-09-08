/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.gantt.component.form;

import java.util.List;

import com.top_logic.basic.StringServices;
import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.mig.html.I18NResourceProvider;
import com.top_logic.reporting.chart.gantt.component.GanttChartFilterComponent;
import com.top_logic.reporting.view.component.AbstractProgressFilterComponent;
import com.top_logic.reporting.view.component.property.FilterProperty;
import com.top_logic.tool.boundsec.BoundComponent;

/**
 * Property for column selection when additional columns option is set.
 * 
 * @author <a href="mailto:mga@top-logic.com">mga</a>
 */
public class ColumnSelectorProperty extends FilterProperty {

	private final I18NResourceProvider _resProvider;
	
	private final List<String> _options;

	/**
	 * Creates a new {@link ColumnSelectorProperty}.
	 */
	public ColumnSelectorProperty(String name, List<String> options, List<String> initialValue, BoundComponent component) {
		super(name, initialValue, component);
		_resProvider = new I18NResourceProvider(GanttChartFilterComponent.RES_PREFIX_FOR_COLUMNS);
		_options = options;
	}

	@Override
	protected AbstractFormField createNewFormMember() {
		SelectField field = FormFactory.newSelectField(getName(), _options,
			AbstractProgressFilterComponent.MULTI_SELECT, !AbstractProgressFilterComponent.IMMUTABLE);
		field.setAsSelection((List) getInitialValue());
		field.setCustomOrder(true);
		field.setOptionLabelProvider(_resProvider);
		return field;
	}

	@Override
	protected Object getValueForPersonalConfiguration(Object fieldValue) {
		return StringServices.toString((List) fieldValue, ",");
	}

	@Override
	protected Object getValueFromPersonalConfiguration(Object confValue) {
		return confValue == null ? null : StringServices.toNonNullList((CharSequence) confValue, ',');
	}

}
