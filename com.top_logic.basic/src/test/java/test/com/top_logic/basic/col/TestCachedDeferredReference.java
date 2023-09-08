/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.col.CachedDeferredReference;
import com.top_logic.basic.col.DeferredReference;

/**
 * Test case for {@link CachedDeferredReference}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestCachedDeferredReference extends TestCase {
	private int time;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		
		time = 0;
	}

	public void testDeferedReference() {
		int timeBeforeCreation = getTime();
		
		DeferredReference ref = createDeferedReference();

		int timeAfterCreation = getTime();

		// Did not yet attemt to create the object.
		assertEquals(timeAfterCreation, timeBeforeCreation + 1);
		
		Integer dereferenced = (Integer) ref.get();

		// Did initialize object.
		assertEquals(dereferenced.intValue(), timeAfterCreation + 1);
	}
	
	public void testCachedDeferedReference() {
		int timeBeforeCreation = getTime();

		DeferredReference ref = createDeferedReference();

		Integer dereferenced = (Integer) ref.get();
		
		int timeAfterAccess = getTime();

		// Did access the init method exactly once.
		assertEquals(dereferenced.intValue(), timeBeforeCreation + 1);
		assertEquals(timeAfterAccess, dereferenced.intValue() + 1);
		
		Integer dereferencedAgain = (Integer) ref.get();
		
		// Does return the very same object.
		assertTrue(dereferenced == dereferencedAgain);
		
		int timeAfterAccessingAgain = getTime();
		
		// Did not touch the init method again.
		assertEquals(timeAfterAccessingAgain, timeAfterAccess + 1);
	}
	
	protected CachedDeferredReference createDeferedReference() {
		return new CachedDeferredReference() {
			@Override
			protected Object initInstance() {
				return Integer.valueOf(getTime());
			}
		};
	}
	
	protected int getTime() {
		return time++;
	}

    /** 
     * The suite of tests.
     */
    public static Test suite() {
        return new TestSuite(TestCachedDeferredReference.class);
    }
}
