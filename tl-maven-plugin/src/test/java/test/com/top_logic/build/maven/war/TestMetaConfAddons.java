/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.build.maven.war;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Test;

import com.top_logic.build.maven.war.TLAppWar;

/**
 * Test case for {@link TLAppWar#metaConfAddons(String, Properties)}.
 */
public class TestMetaConfAddons {

	/** Without further properties, the references of the base property are added. */
	@Test
	public void testBaseOnly() {
		assertArrayEquals(new String[] { "db-h2.xml", "enable-debug.xml" },
			TLAppWar.metaConfAddons(" db-h2.xml, enable-debug.xml ", new Properties()));
	}

	/**
	 * Properties of several profiles contribute their references after the base property, in the
	 * order of the property names.
	 */
	@Test
	public void testPrefixedProperties() {
		Properties properties = new Properties();
		properties.setProperty(TLAppWar.META_CONF_ADDONS, "ignored.xml");
		properties.setProperty(TLAppWar.META_CONF_ADDONS_PREFIX + "pac4j-keycloak", "pac4j-keycloak.xml");
		properties.setProperty(TLAppWar.META_CONF_ADDONS_PREFIX + "ldap", "ldapConf.xml");
		properties.setProperty(TLAppWar.META_CONF_ADDONS_PREFIX + "pac4j-azure", "pac4j-azure.xml, ");
		properties.setProperty("tl.app.metaConfAddonsOther", "other.xml");

		assertArrayEquals(
			new String[] { "db-h2.xml", "ldapConf.xml", "pac4j-azure.xml", "pac4j-keycloak.xml" },
			TLAppWar.metaConfAddons("db-h2.xml", properties));
	}

	/** An empty base property adds nothing of its own. */
	@Test
	public void testEmptyBase() {
		Properties properties = new Properties();
		properties.setProperty(TLAppWar.META_CONF_ADDONS_PREFIX + "ldap", "ldapConf.xml");

		assertArrayEquals(new String[] { "ldapConf.xml" }, TLAppWar.metaConfAddons("", properties));
		assertArrayEquals(new String[] { "ldapConf.xml" }, TLAppWar.metaConfAddons(null, properties));
	}

}
