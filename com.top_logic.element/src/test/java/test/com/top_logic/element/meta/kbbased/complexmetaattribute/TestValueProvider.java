/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.kbbased.complexmetaattribute;

import java.util.Iterator;
import java.util.List;

import com.top_logic.element.meta.ComplexValueProvider;
import com.top_logic.element.meta.OptionProvider;

/**
 * @author    <a href="mailto:jco@top-logic.com">jco</a>
 */
public class TestValueProvider implements ComplexValueProvider<TestBusinessObject> {

    private TestOptionProvider options;
    /**
     * @see com.top_logic.element.meta.ComplexValueProvider#getBusinessObject(java.lang.Object)
     */
    @Override
	public TestBusinessObject getBusinessObject(Object aStorageObject) {
        if(aStorageObject instanceof String) {
            return lookupBusinessObject ((String)aStorageObject);
        }
        return null;
    }

	@Override
	public Class<TestBusinessObject> getApplicationType() {
		return TestBusinessObject.class;
	}

	private TestBusinessObject lookupBusinessObject(String anID) {
		List theOptions = testOptions().getOptionsList(null);
        Iterator iter = theOptions.iterator();
        while (iter.hasNext()) {
            TestBusinessObject elem = (TestBusinessObject) iter.next();
			if (elem.name().equals(anID)) {
                return elem;
            }
        }
        return null;
    }

    /**
     * @see com.top_logic.element.meta.ComplexValueProvider#getOptionProvider()
     */
    @Override
	public OptionProvider getOptionProvider() {
        return testOptions();
    }

	private TestOptionProvider testOptions() {
		if (options == null) {
            options = new TestOptionProvider();   
        }
        return options;
	}

    /**
     * @see com.top_logic.element.meta.ComplexValueProvider#getStorageObject(java.lang.Object)
     */
    @Override
	public Object getStorageObject(Object aBusinessObject) {
        if (aBusinessObject instanceof TestBusinessObject)
			return ((TestBusinessObject) aBusinessObject).name();
        return null;
    }

	@Override
	public boolean isCompatible(Object businessObject) {
		return businessObject == null || businessObject instanceof TestBusinessObject;
	}

}

