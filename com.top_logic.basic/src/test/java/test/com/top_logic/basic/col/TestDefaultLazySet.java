/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.col.LazySetModifyable;

/**
 * Test case for {@link LazySetModifyable}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestDefaultLazySet extends TestCase {

	public void testLazySet() {
		Set lazySet = new LazySetModifyable<Integer>() {
			int callCount = 0;
			
			@Override
			protected Set<Integer> initInstance() {
				callCount++;
				
				HashSet deferred = new HashSet();
				for (int n = 0; n < callCount; n++) {
					deferred.add(Integer.valueOf(13));
				}
				
				return deferred;
			}};
		
		assertEquals(lazySet.size(), 1);
		
		// Again, stays 1
		assertEquals(lazySet.size(), 1);
		
		assertTrue(lazySet.contains(Integer.valueOf(13)));
	}

    /** 
     * Returns a suite of tests.
     */
    public static Test suite() {
        return new TestSuite(TestDefaultLazySet.class);
    }
    
}
