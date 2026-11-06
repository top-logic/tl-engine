/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.security.auth.pac4j.provisioning;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.accesscontrol.ExternalUserMapping;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.security.auth.pac4j.config.Pac4jConfigFactory;
import com.top_logic.security.auth.pac4j.provisioning.NameConflict;
import com.top_logic.security.auth.pac4j.provisioning.OidcProvisioningUserMapping;
import com.top_logic.security.auth.pac4j.provisioning.OidcProvisioningUserMapping.Config;

/**
 * Tests that {@link OidcProvisioningUserMapping} is configured as <code>user-mapping</code> of a
 * pac4j client, in the form the developer article shows.
 */
public class TestOidcProvisioningConfiguration extends TestCase {

	/**
	 * The <code>user-mapping</code> of a client takes the mapping with its options, the group
	 * mapping table and the listeners.
	 */
	public void testUserMappingOfClient() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestOidcProvisioningConfiguration.class);
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap("config",
			TypedConfiguration.getConfigurationDescriptor(Pac4jConfigFactory.Config.class));
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(
			new ClassRelativeBinaryContent(TestOidcProvisioningConfiguration.class, "provisioning-clients.config.xml"));
		Pac4jConfigFactory.Config<?> config = (Pac4jConfigFactory.Config<?>) reader.read();
		context.checkErrors();

		ExternalUserMapping userMapping = config.getClients().get("test-oidc").getUserMapping();
		assertTrue(userMapping instanceof OidcProvisioningUserMapping);
		Config mappingConfig = ((OidcProvisioningUserMapping) userMapping).getConfig();

		assertTrue(mappingConfig.getCreateAccounts());
		assertFalse(mappingConfig.getLinkByName());
		assertFalse(mappingConfig.getLinkByEmail());
		assertTrue(mappingConfig.getRequireVerifiedEmail());
		assertEquals(NameConflict.RENAME, mappingConfig.getNameConflict());
		assertEquals(List.of("example.com", "example.org"), mappingConfig.getAllowedEmailDomains());
		assertEquals("groups", mappingConfig.getGroupClaim());
		assertEquals(List.of("Guests"), mappingConfig.getDefaultGroups());
		assertEquals(Map.of(
			"/developers", Set.of("Developers"),
			"/admins", Set.of("Administrators", "Developers")), mappingConfig.getGroupMapping());
		assertEquals(1, mappingConfig.getListeners().size());
		assertEquals(TestOidcLoginListeners.RecordingListener.class,
			mappingConfig.getListeners().get(0).getImplementationClass());
		assertEquals(OidcProvisioningUserMapping.PREFERRED_USERNAME_CLAIM, mappingConfig.getLoginNameClaim());
	}

	/**
	 * Without configuration, the mapping only finds existing accounts by name and refuses on a
	 * name conflict.
	 */
	public void testDefaults() {
		Config config = TypedConfiguration.newConfigItem(Config.class);

		assertFalse(config.getCreateAccounts());
		assertTrue(config.getLinkByName());
		assertFalse(config.getLinkByEmail());
		assertFalse(config.getRequireVerifiedEmail());
		assertEquals(NameConflict.REFUSE, config.getNameConflict());
		assertEquals(OidcProvisioningUserMapping.GIVEN_NAME_CLAIM, config.getFirstNameClaim());
		assertEquals(OidcProvisioningUserMapping.FAMILY_NAME_CLAIM, config.getLastNameClaim());
		assertEquals(OidcProvisioningUserMapping.EMAIL_CLAIM, config.getEmailClaim());
		assertEquals(OidcProvisioningUserMapping.EMAIL_VERIFIED_CLAIM, config.getEmailVerifiedClaim());
		assertTrue(config.getGroupMapping().isEmpty());
		assertTrue(config.getDefaultGroups().isEmpty());
	}

	/**
	 * The suite of tests to execute.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestOidcProvisioningConfiguration.class, TypeIndex.Module.INSTANCE);
	}

}
