/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util.resource;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.col.MappingSorter;
import com.top_logic.basic.util.ResKey;
import com.top_logic.util.resource.ResourceMapMapping;

/**
 * Test the {@link ResourceMapMapping}
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestResourceMapMapping extends BasicTestCase {

    public TestResourceMapMapping(String name) {
        super(name);
    }

    /**
     * Test method for {@link com.top_logic.util.resource.ResourceMapMapping#map(java.lang.Object)}.
     */
    public void testMap() {
		Map<String, ResKey> keys = new LinkedHashMap<>(); // Conserve insert Order for
																		// reliable result
		keys.put("b", ResKey.forTest("b.b.b"));
		keys.put("a", ResKey.forTest("a.a.a"));
		keys.put("c", ResKey.forTest("c.c.c"));
        List forSort = new ArrayList(keys.keySet());
        
		ResourceMapMapping<String> rmm = new ResourceMapMapping<>(keys);
        Comparator         comp = Collator.getInstance();
        
		MappingSorter.sortByMappingInline(forSort, rmm, comp);
        
        assertSorted(forSort, comp);
    }
    
    /**
     * Get a suite of all test in this test file
     */
    public static Test suite () {
        return TLTestSetup.createTLTestSetup(new TestSuite (TestResourceMapMapping.class));
    }


    public static void main (String[] args) {
        TestRunner.run (suite ());
    }

}

