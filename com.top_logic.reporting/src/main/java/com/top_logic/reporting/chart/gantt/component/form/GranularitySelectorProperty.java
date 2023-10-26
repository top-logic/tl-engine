/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.gantt.component.form;

import java.util.Collections;
import java.util.List;

import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.provider.EnumResourceProvider;
import com.top_logic.reporting.chart.gantt.model.TimeGranularity;
import com.top_logic.reporting.view.component.property.FilterProperty;
import com.top_logic.tool.boundsec.BoundComponent;
import com.top_logic.util.Utils;

/**
 * Property for granularity option when scaling manual is selected.
 * 
 * @author <a href="mailto:mga@top-logic.com">mga</a>
 */
public class GranularitySelectorProperty extends FilterProperty {

	List<TimeGranularity> _validIntervals;

	/**
	 * Creates a new {@link GranularitySelectorProperty}.
	 */
	public GranularitySelectorProperty(String name, TimeGranularity initialValue, List<TimeGranularity> validIntervals, BoundComponent component) {
		super(name, toInitialValue(initialValue), component);
		_validIntervals = validIntervals;
	}

	private static Object toInitialValue(TimeGranularity initialValue) {
		if (initialValue == null) {
			initialValue = TimeGranularity.QUARTER;
		}
		return Collections.singletonList(initialValue);
	}

	@Override
	protected AbstractFormField createNewFormMember() {
		SelectField granularityOptionsField = FormFactory.newSelectField(getName(), _validIntervals);
		granularityOptionsField.setAsSelection((List) getInitialValue());
		granularityOptionsField.setOptionLabelProvider(EnumResourceProvider.INSTANCE);
		return granularityOptionsField;
	}

	@Override
	protected Object getValueForPersonalConfiguration(Object fieldValue) {
		return ((TimeGranularity) Utils.resolveCollection(fieldValue)).toString();
	}

	@Override
	protected Object getValueFromPersonalConfiguration(Object confValue) {
		return confValue == null ? null : Collections.singletonList(TimeGranularity.valueOf((String) confValue));
	}

}
