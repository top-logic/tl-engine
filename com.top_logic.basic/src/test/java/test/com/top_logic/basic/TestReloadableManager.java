/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import com.top_logic.basic.Reloadable;
import com.top_logic.basic.ReloadableManager;
import com.top_logic.basic.ReloadableManager.ReloadFailure;
import com.top_logic.basic.StringServices;


/**
 * Tescase for the {@link com.top_logic.basic.ReloadableManager}.
 *
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestReloadableManager extends TestCase implements Reloadable {

    /** faked result for reload() */
    static boolean reloadOK;
    
    /** True when reload() was called */
    static boolean reloadCalled;

    /** faked result for useXMLProperties() */
    static boolean xmlOK;
    
    /** True when useXMLProperties() was called */
    static boolean xmlCalled;


    /**
     * Constructor for TestReloadableManager.
     * 
     * @param name name of function to execute
     */
    public TestReloadableManager(String name) {
        super(name);
    }
        
    /** Main and singlke testcase for now */
    public void testReloading() {
        ReloadableManager rm = ReloadableManager.getInstance();
        
        rm.addReloadable(this);
        assertEquals(this, rm.get("testReloading"));

        reloadCalled = false;
        reloadOK     = true;
        xmlCalled    = false;
        xmlOK        = true;
        assertEquals(0, rm.reload().length);
        assertTrue(reloadCalled);
        // Not Necessarily so, soe other relodable may have been called before
        // assertTrue(xmlCalled); 
        assertTrue(rm.reload("testReloading"));

        reloadCalled = false;
        reloadOK     = false;
        xmlCalled    = false;
        xmlOK        = false;
        assertEquals(1, rm.reload().length);
        assertTrue(reloadCalled);
        // Not Necessarily so, soe other relodable may have been called before
        // assertTrue(xmlCalled); 
        assertTrue(!rm.reload("testReloading"));
        
        reloadCalled = false;
        reloadOK     = true;
        xmlCalled    = false;
        xmlOK        = true;
        assertEquals(0, rm.reloadKnown().length);
        assertEquals(0, rm.reloadUnknown());
        assertEquals(0, rm.reloadKnown(new String[0]).length);
        assertEquals(1, rm.reloadKnown(new String[] { "unknown"} ).length);
        assertEquals(0, rm.reloadKnown(new String[] { "testReloading"} ).length);
        assertTrue(reloadCalled);
        // Not Necessarily so, soe other relodable may have been called before
        // assertTrue(xmlCalled); 

        reloadCalled = false;
        reloadOK     = false;
        xmlCalled    = false;
        xmlOK        = false;
        assertEquals(1, rm.reloadKnown().length);
        assertEquals(0, rm.reloadUnknown());
        assertEquals(0, rm.reloadKnown(new String[0]).length);
        assertEquals(1, rm.reloadKnown(new String[] { "unknown"} ).length);
        assertEquals(1, rm.reloadKnown(new String[] { "testReloading"} ).length);
        assertTrue(reloadCalled);
        // Not Necessarily so, soe other relodable may have been called before
        // assertTrue(xmlCalled); 

        reloadCalled = false;
        reloadOK     = false;
        xmlCalled    = false;
        xmlOK        = false;
        
        assertEquals("Testcase for Reloadable Manager",
                     rm.getDescription("testReloading"));

        assertTrue(StringServices.contains(rm.getKnown(),"testReloading"));

        rm.removeReloadable(this);
        assertNull(rm.get("testMain"));
    }

	/** Test of {@link ReloadableManager#reloadKnownReporting(java.util.Collection)}. */
	public void testReloadKnownReporting() {
		ReloadableManager rm = ReloadableManager.getInstance();
		RuntimeException problem = new IllegalStateException("Expected reload failure.");
		Reloadable failing = new Reloadable() {
			@Override
			public boolean reload() {
				throw problem;
			}

			@Override
			public String getName() {
				return "testReloadKnownReporting.failing";
			}

			@Override
			public String getDescription() {
				return null;
			}

			@Override
			public boolean usesXMLProperties() {
				return false;
			}
		};
		rm.addReloadable(this);
		rm.addReloadable(failing);
		try {
			reloadCalled = false;
			reloadOK = false;
			xmlOK = false;
			Map<String, ReloadFailure> failures = rm.reloadKnownReporting(
				List.of("unknown", failing.getName(), getName()));
			assertTrue(reloadCalled);
			assertEquals(List.of("unknown", failing.getName(), getName()), List.copyOf(failures.keySet()));
			assertNull(failures.get("unknown").cause());
			assertNotNull(failures.get("unknown").reason());
			assertSame(problem, failures.get(failing.getName()).cause());
			assertNull(failures.get(getName()).cause());
			assertNotNull(failures.get(getName()).reason());

			reloadOK = true;
			assertEquals(Map.of(), rm.reloadKnownReporting(List.of(getName())));
			assertEquals(1, rm.reloadKnown(new String[] { getName(), failing.getName() }).length);
		} finally {
			rm.removeReloadable(failing);
			rm.removeReloadable(this);
		}
	}

    /**
     * @see com.top_logic.basic.Reloadable#getDescription()
     */
    @Override
	public String getDescription() {
        return "Testcase for Reloadable Manager";
    }

    /**
     * @see com.top_logic.basic.Reloadable#reload()
     */
    @Override
	public boolean reload() {
        reloadCalled = true;
        return reloadOK;
    }

    /**
     * @see com.top_logic.basic.Reloadable#usesXMLProperties()
     */
    @Override
	public boolean usesXMLProperties() {
        xmlCalled = true;
        return xmlOK;
    }


    /** Return the suite of Tests to perform */
    public static Test suite () {
        return ModuleTestSetup.setupModule(TestReloadableManager.class);
        // return = new TestReloadableManager("testTypes");
    }

    /** Main function for direct execution */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }

}
