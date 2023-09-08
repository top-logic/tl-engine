/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.singleton;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.element.util.ElementWebTestSetup;

import com.top_logic.basic.Logger;
import com.top_logic.element.singleton.ElementSingletonManager;
import com.top_logic.element.structured.StructuredElement;

/**
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class TestElementSingletonManager extends BasicTestCase {

    public TestElementSingletonManager(String name) {
        super(name);
    }
    
    public void testElementSingletonManager() throws Exception {
		StructuredElement theProdRoot = (StructuredElement) ElementSingletonManager
			.getSingleton(ElementSingletonManager.SINGLETON_PREFIX_STRUCTURE_ROOT + "prodElement");
        assertNotNull(theProdRoot);
        assertEquals("RootElement", theProdRoot.getElementType());
    }
    

    /**
     * Return the suite of Tests to perform.
     *
     * @return the test for this class
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(TestElementSingletonManager.class);
        return ElementWebTestSetup.createElementWebTestSetup(suite);
    }

    /**
     * Main function for direct testing.
     *
     * @param args command line arguments are ignored
     */
    public static void main(String[] args) {
        Logger.configureStdout();
        // KBSetup.CREATE_TABLES = false; // Speed up debugging
        TestRunner.run(suite());
    }


}

