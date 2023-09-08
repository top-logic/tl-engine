/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.services.simpleajax;

import junit.framework.Test;

import com.top_logic.base.services.simpleajax.ContentReplacement;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.layout.basic.fragments.Fragments;

/**
 * Create coverage for {@link ContentReplacement} and {@link HTMLFragment}.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestContentReplacement extends ActionTestcase {

    public TestContentReplacement(String name) {
        super(name);
    }

    /**
     * Test simple uage of {@link ContentReplacement}.
     */
    public void testContentReplacement() throws Exception {
        ContentReplacement cr = new ContentReplacement("xxx", Fragments.empty());
        assertXMLOutput(cr, null);
    }

	public static Test suite() {
		return suite(TestContentReplacement.class);
	}

}

