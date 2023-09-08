/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.tree.model;

import junit.framework.Test;
import junit.framework.TestCase;

import com.top_logic.layout.tree.model.TreeModelEvent;

/**
 * Test the {@link TreeModelEvent}
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestTreeModelEvent extends TestCase {

    /** 
     * Creates a TestTreeModelEvent for functionName.
     */
    public TestTreeModelEvent(String functionName) {
        super(functionName);
    }

    /**
     * Not much to test here ...
     */
    public void testTreeModelEvent() {
        TreeModelEvent tme = new TreeModelEvent(null, TreeModelEvent.AFTER_NODE_ADD, null);
        assertNull(tme.getModel());
        assertNull(tme.getNode());

        assertEquals(TreeModelEvent.AFTER_NODE_ADD, tme.getType());
    }

    static public Test suite () {   
        return new TestTreeModelEvent("testTreeModelEvent");
    }

}

