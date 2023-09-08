/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import junit.framework.Test;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.DefaultTypeKeyProvider;
import com.top_logic.basic.TypeKeyProvider.Key;
import com.top_logic.basic.TypeKeyRegistry;

/**
 * Tests {@link TypeKeyRegistry} and {@link DefaultTypeKeyProvider}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestTypeKeyRegistry extends BasicTestCase {

    /** 
     * Create a new TestTypeKeyRegistry by name.
     */
    public TestTypeKeyRegistry(String aName) {
        super(aName);
    }

    public void testOK() {
		Key stringKey = TypeKeyRegistry.lookupTypeKey("test");
		assertNotNull(stringKey);
		assertTrue(stringKey == TypeKeyRegistry.lookupTypeKey("more test"));
		
		Key aKey         = TypeKeyRegistry.lookupTypeKey(new A());
		assertEquals(aKey, TypeKeyRegistry.lookupTypeKey(new B()).getDefaultKey());
		assertEquals(aKey, TypeKeyRegistry.lookupTypeKey(new C()).getDefaultKey());
	}
    
	static class A { /* nothing to do here */ }
	
	static class B extends A {/* nothing to do here */}
	
	static class C extends A {/* nothing to do here */}

	
    public static Test suite() {
		Test innerTest = ServiceTestSetup.createSetup(TestTypeKeyRegistry.class, TypeKeyRegistry.Module.INSTANCE);
		return BasicTestSetup.createBasicTestSetup(innerTest);
    }
	
}
