/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.util.HashMap;
import java.util.Map;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.AliasManager;
import com.top_logic.basic.AliasedProperties;

/**
 * <p>Test class for the alias manager.</p>
 * <p>
 * Remark to terminology: Originally the AliasManager
 * only handled paths. Attempts to use non-paths as aliases
 * had unexpected side-effects. Therefore we differ between
 * paths and non-paths.
 * </p>
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class TestAliasManager extends TestCase {

    public TestAliasManager (String aName) {
        super (aName);
    }
    
	public void testEnvironmentVariable() {
		assertFalse(System.getProperties().containsKey("NotExistingEnvironmentVariable"));

		assertCorrectAliasedNonPath("value1", "%ENVIRONMENT1%");
		assertCorrectAliasedNonPath("More value1 replacements value2  inOneVariable", "%ENVIRONMENT2%");
		// Non existing variable is replaced by empty string.
		assertCorrectAliasedNonPath("", "%ENVIRONMENT3%");
		assertCorrectAliasedNonPath("default-value", "%ENVIRONMENT5%");
		assertCorrectAliasedNonPath("value1", "%ENVIRONMENT6%");
		assertCorrectAliasedNonPath("{:\\default-value\\:}", "%ENVIRONMENT7%");
		assertCorrectAliasedNonPath("", "%ENVIRONMENT8%");
	}

	public void testEnvironmentVariableWithSpecialChars() {
		assertCorrectAliasedNonPath("value\\withSpecial$Chars", "%ENVIRONMENT4%");
	}

    /** Trivial functions to reach 100% coverage */
    public void testTrivial() {
        assertNotNull(AliasManager.getInstance().toString());
    }

    public void testWebPath() {
		String value1 = AliasManager.getInstance().replace("%TEST_VAR%/../Karl");

		assertNotNull(value1);
		assertTrue(value1.indexOf('%') < 0);

		setTestVar("tmp/Erna");
        
        AliasedProperties props = new AliasedProperties();
		props.put("webPath", "%TEST_VAR%");
		assertEquals("./webapp", props.getProperty("webPath"));

        try {
			String value2 = AliasManager.getInstance().replace("%TEST_VAR%/../Karl");

			assertNotNull(value2);
			assertTrue(value2.indexOf('%') < 0);
        }  finally {
            this.reset ();
        }
    }

    public void testReplaceOnce () {
        this.assertCorrectAliasedPath ("PATH1",         "%PATH1%");
        this.assertCorrectAliasedPath ("PATH1%",        "%PATH1%%");
        this.assertCorrectAliasedPath ("PATH1/2/3",     "%PATH1%/2/3");
        this.assertCorrectAliasedPath ("PATH1/3",       "1/%PATH1%/3");
        //File.separator is necessary due to internal canonicalization.
        this.assertCorrectAliasedPath ("PATH1/PATH2/3",
                                       "1/%PATH2%/3");
    }

    public void testGetAlias () {
        AliasManager amgr = AliasManager.getInstance ();

		assertEquals(null, amgr.getAlias("%NOSUCHTHING%"));
        assertCorrectPath ("PATH1", amgr.getAlias("%PATH1%"));
        // Note: "Compounded" aliases are not guaranteed to have an
        // alias free return value. Thus we can not test with e.g NON-PATH2.
    }

    /** Internal helper method to set the WebPath */
	protected void setTestVar(String aPath) {
		Map<String, String> theAliases = new HashMap<>();
		theAliases.put("%TEST_VAR%", aPath);
        // theAliases.put (AliasManager.APP_CONTEXT, getAppContext (aPath));

        AliasManager.getInstance ().setBaseAliases (theAliases);

    }


    /** Reset Web-Path so later Tests do not fail ... */
    public void reset() {
		setTestVar("./webapp");
    }

    /**
     * Assert that two paths match.
     * <p>
     * More specifically, we check that the actual path ends with
     * the specified part of the expected path.
     * </p>
     *
     * @param anExpectedPathSuffix the <em>suffix</em> of the expected path
     * @param aPath                the actual path.
     *
     * #author Michael Eriksson
     */
    public void assertCorrectPath (String anExpectedPathSuffix, String aPath) {
        assertTrue ("Path <" + aPath + "> expected to end with <"
                         + anExpectedPathSuffix+ ">.",
                     aPath.endsWith (anExpectedPathSuffix));
    }

    /**
     * Analogous to assertCorrectPath, but runs the second argument
     * through the AliasManager before checking.
     *
     * @param anExpectedPathSuffix the <em>suffix</em> of the expected path
     * @param aPathAlias           the actual path.
     *
     * #author Michael Eriksson
     */
    public void assertCorrectAliasedPath (String anExpectedPathSuffix,
                                          String aPathAlias) {
        this.assertCorrectPath (anExpectedPathSuffix ,
            AliasManager.getInstance ().replace (aPathAlias));
    }

    /**
     * Assert that two non-paths match.
     *
     * @param anExpectedNonPath the expected non-path
     * @param aNonPath          the actual non-path
     *
     * #author Michael Eriksson
     */
    public void assertCorrectNonPath (String anExpectedNonPath,
                                      String aNonPath) {
        assertEquals ("Non-Paths must match.", anExpectedNonPath, aNonPath);
    }

    /**
     * Analogous to assertCorrectNonPath, but runs the second argument
     * through the AliasManager before checking.
     *
     * @param anExpectedNonPath       the expected non-path
     * @param aNonPathAlias           the actual non-path.
     *
     * #author Michael Eriksson
     */
    public void assertCorrectAliasedNonPath (String anExpectedNonPath,
                                             String aNonPathAlias) {
        this.assertCorrectNonPath (anExpectedNonPath,
            AliasManager.getInstance ().replace (aNonPathAlias));
    }

	private static class EnvironmentVariableSetup extends TestSetup {

		private final String _key;

		private final String _value;

		public EnvironmentVariableSetup(String key, String value, Test test) {
			super(test);
			_key = key;
			_value = value;
		}

		@Override
		protected void setUp() throws Exception {
			super.setUp();
			assertFalse("Key '" + _key + "' already known in system properties",
				System.getProperties().containsKey(_key));
			System.setProperty(_key, _value);
			assertEquals("Setting value '" + _value + "' for property '" + _key + "' fails.", _value,
				System.getProperty(_key));
		}

		@Override
		protected void tearDown() throws Exception {
			System.clearProperty(_key);
			super.tearDown();
		}

	}

    /**
     * the suite of test to perform
     */
    public static Test suite () {

        TestSuite suite = new TestSuite (TestAliasManager.class);

		Test acutalTest = BasicTestSetup.createBasicTestSetup(suite);

		// Setup variable in separate test setup <b>before</b> starting XMLProperties to ensure
		// replacement works correct.
		EnvironmentVariableSetup setupVariable1 =
			new EnvironmentVariableSetup("TestEnvironmentVariable", "value1", acutalTest);
		EnvironmentVariableSetup setupVariable2 =
			new EnvironmentVariableSetup("TestEnvironmentVariable2", "value2", setupVariable1);
		EnvironmentVariableSetup setupVariable3 =
			new EnvironmentVariableSetup("TestEnvironmentVariableWithSpecialChar", "value\\withSpecial$Chars",
				setupVariable2);
		return setupVariable3;
    }

    /**
     * main function for direct testing.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }
}
