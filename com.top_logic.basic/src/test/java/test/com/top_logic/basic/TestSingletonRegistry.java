/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.top_logic.basic.DefaultTypeKeyProvider;
import com.top_logic.basic.KeyStorage;
import com.top_logic.basic.Logger;
import com.top_logic.basic.ReloadableManager;
import com.top_logic.basic.SingletonRegistry;
import com.top_logic.basic.TypeKeyProvider.Key;

/**
 * testcase for the {@link SingletonRegistry}.
 * 
 * @author     <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestSingletonRegistry extends BasicTestCase {


    public void testSingletonRegistry() {
    	TestRegistry singleton = TestRegistry.INSTANCE;
    	
		assertTrue(ReloadableManager.getInstance().isReloadableRegistered(singleton));
    	
        // Test fallback to global default.
        assertEquals("theGlobalValue", TestRegistry.getEntryFor(String.class));

        // Test direct match.
        assertEquals("theValueForTestSingletonRegistry", TestRegistry.getEntryFor(TestSingletonRegistry.class));
        
        // Test match for inner class.
        assertEquals("theValueForTheTestRegistry", TestRegistry.getEntryFor(TestRegistry.class));
        
        // Test multi-level fallback.
        assertEquals("theValueForAssert", TestRegistry.getEntryFor(BasicTestCase.class));
        
		String untouchedValue = TestRegistry.getEntryFor("untouched");
		
		assertEquals("theUntouchedValue", untouchedValue);
		assertEquals("theTouchedValue", TestRegistry.getEntryFor("touched"));
		
		// Without a reload, the registry returns exactly the cached value.
		assertSame(untouchedValue, TestRegistry.getEntryFor("untouched"));
		
		// A reload clears the internal key-value-pair cache of the registry.
		ReloadableManager.getInstance().reload();

		String untouchedValueAfterReload = TestRegistry.getEntryFor("untouched");

		// Now, a newly created value is returned.
		assertNotSame(untouchedValue, untouchedValueAfterReload);
		assertEquals(untouchedValue, untouchedValueAfterReload);
		
		// The instance of the registry is still the same (cannot change here,
		// but also must not change in all other implementations of
		// SingletonRegistry).
		assertSame(singleton, TestRegistry.INSTANCE);
		
		singleton.unregisterFromReload();
		
		// Again, a reload, with an unregistered reloadable.
		ReloadableManager.getInstance().reload();
		
		// Since no reload was triggered on the registry, it must still return
		// the same cached value.
		assertSame(untouchedValueAfterReload, TestRegistry.getEntryFor("untouched"));
    }
    
    public void testBrokenRegistry() throws Exception {
        
        BrokenRegistry br = new BrokenRegistry();
        
        Logger.configureStdout("FATAL"); // That ERROR is just what we expected
        assertNull(br.getSingletonFor(Boolean.class));
        Logger.configureStdout(); 

        assertSame(Object.class, br.getSingletonFor(String.class).getClass());

        assertNull(br.getSingletonFor(this.getClass()));
        
        Logger.configureStdout("FATAL"); // Those ERRORs are just what we expected
        assertNull(br.getSingletonFor(Integer.class));
        Logger.configureStdout(); 

        Logger.configureStdout("FATAL"); // Those ERRORs are just what we expected
        assertNull(br.getSingletonFor(Double.class));
        Logger.configureStdout(); 
    }

    /**
     * Subclass SingletonRegistry with an example implementation.
     */
	public static class TestRegistry extends KeyStorage<String> {
        
        static TestRegistry INSTANCE = newInstance();
        
        public static String getEntryFor(Class<?> aClass) {
            return INSTANCE.lookupEntry(new DefaultTypeKeyProvider.ClassKey(aClass));
        }

		public static String getEntryFor(String aPlainKey) {
            return INSTANCE.lookupEntry(new PlainKey(aPlainKey));
        }
        
        @Override
		protected String getGlobalDefault() {
            return "theGlobalValue";
        }
        
        /**  
         * Create new entry by prepending "the" and twiddeling with case of configurationValue.
         * 
         * You normally will NOT override this function, this is done for testing only.
         */
        @Override
		protected String newEntry(String propertyName, String configurationValue) throws Exception {
            return "the" + Character.toUpperCase(configurationValue.charAt(0)) + configurationValue.substring(1);
        }

        /**
		 * Create a new instance of this class and register it with the
		 * {@link ReloadableManager}.
		 */
        private static TestRegistry newInstance() {
			TestRegistry newInstance = new TestRegistry();
			
			// Since this is reloadable, it must register with the
			// ReloadableManager.
			newInstance.registerForReload();
			
			return newInstance;
		}

		@Override
		public String getDescription() {
			return null;
		}

		@Override
		public String getName() {
			return null;
		}
    }

    /**
     * This SingletonRegistry is broken via its config, see tl_basic.xml
     */
    public static class BrokenRegistry extends SingletonRegistry<Object> {
        
        @Override
		protected boolean checkConfiguration() {
        	// Do nothing, because this test case is a non-standard usage of the
			// Registry, where configuration values are no legal class names.
        	return true;
        }

        /** A Default implementation that uses {@link SingletonRegistry#lookupEntry(Key)}. */
        public Object getSingletonFor(Class<?> aClass) {
            return lookupEntry(new DefaultTypeKeyProvider.ClassKey(aClass));
        }

        /** Return null to indicate an unsupported/unconfigured class */
        @Override
		protected Object getGlobalDefault() {
            return null;    
        }

		@Override
		public String getDescription() {
			return null;
		}

		@Override
		public String getName() {
			return null;
		}
		
		@Override
		protected Object wrap(Object newEntry) {
			return newEntry;
		}
    }
    
    public static class BrokenSingleton {
		public static final Object INSTANCE = null;
    }

    public static class BrokenSingleton2 {
        public static BrokenSingleton2 getInstance() {
            throw new IllegalArgumentException("Dont try to call me !");
        }
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(TestSingletonRegistry.class);
        // Test      suite =  new TestSingletonRegistry("textXXX"));
        return BasicTestSetup.createBasicTestSetup(suite);
    }

    /**
     * main fucntion for direct testing.
     */
    public static void main(String[] args) {
        // switsch of info messages
        Logger.configureStdout();
        junit.textui.TestRunner.run (suite ());
    }    

}
