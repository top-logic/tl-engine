/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.services.simpleajax;

import junit.framework.Test;

import com.top_logic.base.services.simpleajax.RangeReplacement;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.basic.fragments.TextFragment;

/**
 * Create coverage for {@link RangeReplacement} and {@link TextFragment}.
 * 
 * @author <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestRangeReplacement extends ActionTestcase {

    public TestRangeReplacement(String name) {
        super(name);
    }

    /**
     * Test simple uage of {@link RangeReplacement}.
     */
    public void testRangeReplacement() throws Exception {
        RangeReplacement rr = new RangeReplacement("fromId", "toId"
			, Fragments.text("New Content"));
        assertXMLOutput(rr, null);
    }

	public static Test suite() {
		return suite(TestRangeReplacement.class);
	}

}

