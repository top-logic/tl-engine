/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.services.simpleajax;

import junit.framework.Test;

import com.top_logic.base.services.simpleajax.FragmentInsertion;
import com.top_logic.layout.basic.fragments.Fragments;

/**
 * Create coverage for {@link FragmentInsertion}.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestFragmentInsertion extends ActionTestcase {

    public TestFragmentInsertion(String name) {
        super(name);
    }

    /**
     * Test simple uage of {@link FragmentInsertion}.
     */
    public void testFragmentInsertiont() throws Exception {
        FragmentInsertion fr = new FragmentInsertion("zzz", "do.it.there"
                ,  Fragments.empty());
        assertXMLOutput(fr, null);
    }

	public static Test suite() {
		return suite(TestFragmentInsertion.class);
	}

}

