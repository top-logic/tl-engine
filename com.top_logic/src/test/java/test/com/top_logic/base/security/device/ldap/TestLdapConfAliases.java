/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.security.device.ldap;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestSetup;
import test.com.top_logic.basic.CustomConfigurationDecorator;
import test.com.top_logic.basic.SimpleDecoratedTestSetup;
import test.com.top_logic.basic.TestUtils;

import com.top_logic.basic.AliasManager;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.MultiProperties;

/**
 * Test that the LDAP configuration {@value #LDAP_CONF} shipped with the framework takes its alias
 * values from system properties (or environment variables) and falls back to its defaults.
 */
public class TestLdapConfAliases extends TestCase {

	/** The LDAP configuration file under test. */
	static final String LDAP_CONF = "/WEB-INF/conf/ldapConf.xml";

	/** System property defining the alias {@value #ALIAS_URL}. */
	static final String PROPERTY_URL = "ldap_url";

	/** System property defining the alias {@value #ALIAS_CREDENTIAL}. */
	static final String PROPERTY_CREDENTIAL = "ldap_credential";

	/** Value given for {@link #PROPERTY_URL}. */
	static final String URL_VALUE = "ldaps://ldap.test.example:636";

	/** Value given for {@link #PROPERTY_CREDENTIAL}. */
	static final String CREDENTIAL_VALUE = "test-secret";

	private static final String ALIAS_URL = "%LDAP_URL%";

	private static final String ALIAS_CREDENTIAL = "%LDAP_CREDENTIAL%";

	/**
	 * Aliases given by system properties resolve to the property values.
	 */
	public void testValuesFromSystemProperties() {
		assertAlias(URL_VALUE, ALIAS_URL);
		assertAlias(CREDENTIAL_VALUE, ALIAS_CREDENTIAL);
		assertEquals("Bind with " + CREDENTIAL_VALUE + " at " + URL_VALUE,
			AliasManager.getInstance().replace("Bind with " + ALIAS_CREDENTIAL + " at " + ALIAS_URL));
	}

	/**
	 * Aliases without a system property or environment variable resolve to their defaults.
	 */
	public void testDefaults() {
		assertAlias("you@your.domain", "%LDAP_PRINCIPAL%");
		assertAlias("DC=YourDC", "%LDAP_BASE_DN%");
		assertAlias("(objectClass=Person)", "%LDAP_FILTER%");
		assertAlias("CN=YourCN,DC=YourDC", "%LDAP_GROUP_1%");
		assertAlias("", "%LDAP_GROUP_2%");
		assertAlias("", "%LDAP_GROUP_3%");
		assertAlias("false", "%LDAP_ALLOW_PWD_CHANGE%");
		assertAlias("false", "%LDAP_NESTED_GROUPS%");
		assertAlias("/WEB-INF/database/ldap/all-mapping.properties", "%LDAP_MAPPING_ALL%");
		assertAlias("/WEB-INF/database/ldap/person-mapping.properties", "%LDAP_MAPPING_PERSON%");
		assertAlias("/WEB-INF/database/ldap/group-mapping.properties", "%LDAP_MAPPING_GROUP%");
		assertAlias("optional", "%LDAP_MFA_REQUIREMENT%");
	}

	private static void assertAlias(String expected, String alias) {
		assertEquals("Value of alias " + alias, expected, AliasManager.getInstance().getAlias(alias));
	}

	/**
	 * {@link CustomConfigurationDecorator} that sets the system properties {@link #PROPERTY_URL}
	 * and {@link #PROPERTY_CREDENTIAL} and adds {@value #LDAP_CONF} to the current configuration.
	 */
	static class LdapConfDecorator extends CustomConfigurationDecorator {

		@Override
		public void setup(SetupAction innerSetup) throws Exception {
			setProperty(PROPERTY_URL, URL_VALUE);
			setProperty(PROPERTY_CREDENTIAL, CREDENTIAL_VALUE);
			super.setup(innerSetup);
		}

		private static void setProperty(String key, String value) {
			assertNull("System property '" + key + "' already set.", System.getProperty(key));
			System.setProperty(key, value);
		}

		@Override
		protected void installConfiguration() throws Exception {
			MultiProperties.restartWithConfigs(FileManager.getInstance().getData(LDAP_CONF), null);
		}

		@Override
		public void tearDown(SetupAction innerSetup) throws Exception {
			try {
				super.tearDown(innerSetup);
			} finally {
				System.clearProperty(PROPERTY_URL);
				System.clearProperty(PROPERTY_CREDENTIAL);
			}
		}

	}

	/**
	 * Suite of tests in this class.
	 */
	public static Test suite() {
		Test decorated =
			new SimpleDecoratedTestSetup(new LdapConfDecorator(), new TestSuite(TestLdapConfAliases.class));
		return TestUtils.doNotMerge(BasicTestSetup.createBasicTestSetup(decorated));
	}

}
