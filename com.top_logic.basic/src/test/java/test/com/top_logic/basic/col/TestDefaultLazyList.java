/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.col.LazyListModifyable;

/**
 * Test case for {@link LazyListModifyable}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestDefaultLazyList extends TestCase {

	public void testLazyList() {
		List lazyList = new LazyListModifyable<Integer>() {
			int callCount = 0;
			
			@Override
			protected List<Integer> initInstance() {
				callCount++;
				
				ArrayList deferred = new ArrayList();
				for (int n = 0; n < callCount; n++) {
					deferred.add(Integer.valueOf(13));
				}
				
				return deferred;
			}};
		
		assertEquals(lazyList.size(), 1);
		
		// Again, stays 1
		assertEquals(lazyList.size(), 1);
		
		assertEquals(lazyList.get(0), Integer.valueOf(13));
	}

    /** 
     * The suite of tests.
     */
    public static Test suite() {
        return new TestSuite(TestDefaultLazyList.class);
    }
}
