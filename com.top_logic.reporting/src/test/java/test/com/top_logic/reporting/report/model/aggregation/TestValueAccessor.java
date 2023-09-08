/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.report.model.aggregation;

import com.top_logic.layout.Accessor;

/**
 * Accessor for testing an aggregation function. The Objects must be {@link TestValueObject}s.
 * 
 * @author     <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class TestValueAccessor implements Accessor {

	@Override
	public Object getValue(Object object, String property) {
		return ((TestValueObject)object).getValue(property);
	}

	@Override
	public void setValue(Object object, String property, Object value) {
	}

}
