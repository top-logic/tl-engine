/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.report.model.aggregation;

import java.util.HashMap;
import java.util.Map;

/**
 * @author     <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class TestValueObject implements Comparable {
	private Map values;
	
	public TestValueObject(String aProperty, Object aValue) {
		this.values = new HashMap();
		this.setValue(aProperty, aValue);
    }
	
	public Object getValue(String aProperty) {
		return values.get(aProperty);
	}
	
	public void setValue(String aProperty, Object aValue) {
		values.put(aProperty, aValue);
	}

	@Override
	public int compareTo(Object o) {
		if(o instanceof TestValueObject) {
			Number n1 = (Number) this.getValue("value");
			Number n2 = (Number) ((TestValueObject)o).getValue("value");
			return (n1.doubleValue() > n2.doubleValue()) ?  1 : (n1.doubleValue() == n2.doubleValue()) ? 0 : -1;
		}
	    return 0;
    }
	
	
}
