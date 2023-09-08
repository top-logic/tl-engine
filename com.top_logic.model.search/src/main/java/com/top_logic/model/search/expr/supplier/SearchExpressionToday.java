/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.supplier;

import java.util.Calendar;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.time.TimeZones;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link SupplierSearchExpressionBuilder} for the current day.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class SearchExpressionToday extends SupplierSearchExpressionBuilder {

	/** {@link TypedConfiguration} constructor for {@link SearchExpressionToday}. */
	public SearchExpressionToday(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Object getValue() {
		Calendar now = CalendarUtil.createCalendarInUserTimeZone();
		Calendar target = CalendarUtil.clone(now);
		target.setTimeZone(TimeZones.systemTimeZone());
		target.clear();
		CalendarUtil.copyFieldValue(now, target, Calendar.YEAR);
		CalendarUtil.copyFieldValue(now, target, Calendar.MONTH);
		CalendarUtil.copyFieldValue(now, target, Calendar.DAY_OF_MONTH);
		target.set(Calendar.HOUR_OF_DAY, 0);
		target.set(Calendar.MINUTE, 0);
		target.set(Calendar.SECOND, 0);
		target.set(Calendar.MILLISECOND, 0);
		return target.getTime();
	}

	@Override
	public TLType getType() {
		return TLModelUtil.findType(TypeSpec.DATE_TYPE);
	}

	@Override
	public boolean isMultiple() {
		return false;
	}

}
