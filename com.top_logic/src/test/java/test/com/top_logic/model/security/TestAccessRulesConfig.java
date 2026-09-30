/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.security;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.model.annotate.security.AccessGrant;
import com.top_logic.model.annotate.security.AccessRevoke;
import com.top_logic.model.annotate.security.AccessRule;
import com.top_logic.model.annotate.security.RoleConfig;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.model.security.SecurityConfigurationService.ModelAccessRights;

/**
 * Test for reading the access rules of a {@link SecurityConfigurationService.Config} from
 * configuration.
 */
@SuppressWarnings("javadoc")
public class TestAccessRulesConfig extends TestCase {

	private static final String CONFIG_TAG = "config";

	private static final String PERSON = "tl.accounts:Person";

	private static final String FRAMEWORK_CONFIG = """
			<config>
				<security-config>
					<class name="tl.accounts:Person">
						<grant inherit="true" operation="Read" roles="PersonalProfile"/>
						<grant inherit="true" operation="Write" roles="PersonalProfile"/>
					</class>
				</security-config>
			</config>
			""";

	private static final String APPLICATION_CONFIG = """
			<config>
				<security-config>
					<class name="tl.accounts:Person">
						<grant operation="Read" roles="AccountViewer, AccountEditor"/>
						<revoke operation="Write" roles="AccountEditor"/>
						<revoke operation="Delete"/>
					</class>
				</security-config>
			</config>
			""";

	public void testApplicationRulesFollowFrameworkRules() throws Exception {
		List<AccessRule> rules = personRules(overlay(FRAMEWORK_CONFIG, APPLICATION_CONFIG));

		assertEquals("The rules of both configuration layers are kept.", 5, rules.size());

		assertGrant(rules.get(0), "Read", true, "PersonalProfile");
		assertGrant(rules.get(1), "Write", true, "PersonalProfile");
		assertGrant(rules.get(2), "Read", false, "AccountViewer", "AccountEditor");
	}

	public void testRevokeWithRoles() throws Exception {
		List<AccessRule> rules = personRules(overlay(FRAMEWORK_CONFIG, APPLICATION_CONFIG));

		AccessRule revoke = rules.get(3);
		assertInstanceof(AccessRevoke.class, revoke);
		assertEquals("Write", revoke.getOperation().id());
		assertEquals(List.of("AccountEditor"), roleNames(revoke));
	}

	public void testRevokeWithoutRolesDropsOperation() throws Exception {
		List<AccessRule> rules = personRules(overlay(FRAMEWORK_CONFIG, APPLICATION_CONFIG));

		AccessRule revoke = rules.get(4);
		assertInstanceof(AccessRevoke.class, revoke);
		assertEquals("Delete", revoke.getOperation().id());
		assertEquals("A revocation without roles drops the whole operation entry.",
			Collections.emptyList(), roleNames(revoke));
	}

	public void testOperationIsMandatory() {
		try {
			read(new BufferingProtocol(), """
					<config>
						<security-config>
							<class name="tl.accounts:Person">
								<grant roles="PersonalProfile"/>
							</class>
						</security-config>
					</config>
					""");
		} catch (ConfigurationException ex) {
			assertTrue("The missing operation is reported: " + ex.getMessage(),
				ex.getMessage().contains("getOperation()"));
			return;
		}
		fail("A rule without an operation must be rejected.");
	}

	private static void assertGrant(AccessRule rule, String operation, boolean inherit, String... roles) {
		assertInstanceof(AccessGrant.class, rule);
		assertEquals(operation, rule.getOperation().id());
		assertEquals(inherit, rule.isInherit());
		assertEquals(List.of(roles), roleNames(rule));
	}

	private static void assertInstanceof(Class<?> expected, AccessRule rule) {
		assertTrue("Expected a " + expected.getSimpleName() + ", got: " + rule, expected.isInstance(rule));
	}

	private static List<String> roleNames(AccessRule rule) {
		return rule.getRoles().stream().map(RoleConfig::getName).toList();
	}

	private static List<AccessRule> personRules(SecurityConfigurationService.Config config) {
		Map<String, ModelAccessRights> securityConfig = config.getSecurityConfig();
		ModelAccessRights personRights = securityConfig.get(PERSON);
		assertNotNull("Rights for '" + PERSON + "' are configured.", personRights);
		return personRights.getGrants();
	}

	/**
	 * Reads the given overlay configuration on top of the given base configuration, in the same way
	 * as an application layers its configuration onto the one of the framework.
	 */
	private static SecurityConfigurationService.Config overlay(String base, String increment) throws Exception {
		Protocol log = new BufferingProtocol();
		SecurityConfigurationService.Config baseConfig = read(log, base);
		ConfigurationItem result = newReader(log)
			.setBaseConfig(baseConfig)
			.setSource(CharacterContents.newContent(increment))
			.read();
		log.checkErrors();
		return (SecurityConfigurationService.Config) result;
	}

	private static SecurityConfigurationService.Config read(Protocol log, String xml)
			throws ConfigurationException {
		return (SecurityConfigurationService.Config) newReader(log)
			.setSource(CharacterContents.newContent(xml))
			.read();
	}

	private static ConfigurationReader newReader(Protocol log) {
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(CONFIG_TAG,
			TypedConfiguration.getConfigurationDescriptor(SecurityConfigurationService.Config.class));
		return new ConfigurationReader(new DefaultInstantiationContext(log), descriptors);
	}

	/**
	 * The suite of tests.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestAccessRulesConfig.class, TypeIndex.Module.INSTANCE));
	}

}
