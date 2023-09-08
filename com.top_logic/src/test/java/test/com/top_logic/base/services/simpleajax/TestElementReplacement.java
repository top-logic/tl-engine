/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.services.simpleajax;

import junit.framework.Test;

import com.top_logic.base.services.simpleajax.ElementReplacement;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.basic.fragments.TextFragment;

/**
 * Create coverage for {@link ElementReplacement} and {@link TextFragment}.
 * 
 * @author <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestElementReplacement extends ActionTestcase {

    public TestElementReplacement(String name) {
        super(name);
    }

    /**
     * Test simple uage of {@link ElementReplacement}.
     */
    public void testElementReplacement() throws Exception {
        ElementReplacement er = new ElementReplacement("zzz"
			, Fragments.text("Tandaradey"));
        assertXMLOutput(er, null);
    }

	public static Test suite() {
		return suite(TestElementReplacement.class);
	}

}

