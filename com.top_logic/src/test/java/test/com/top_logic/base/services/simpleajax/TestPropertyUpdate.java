/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.services.simpleajax;

import junit.framework.Test;

import com.top_logic.base.services.simpleajax.PropertyUpdate;
import com.top_logic.layout.basic.ConstantDisplayValue;

/**
 * Create coverage for {@link PropertyUpdate}.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestPropertyUpdate extends ActionTestcase {

    public TestPropertyUpdate(String name) {
        super(name);
    }

    /**
     * Test simple uage of {@link PropertyUpdate}.
     */
    public void testPropertyUpdate() throws Exception {
        PropertyUpdate pu = new PropertyUpdate("zzz", "someProp"
                ,  new ConstantDisplayValue("I am variable"));
        assertXMLOutput(pu, null);
    }

	public static Test suite() {
		return suite(TestPropertyUpdate.class);
	}

}

