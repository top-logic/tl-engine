/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContent;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.element.boundsec.manager.rule.GroupWithName;
import com.top_logic.element.boundsec.manager.rule.PathElement;
import com.top_logic.element.boundsec.manager.rule.config.RoleRulesConfig;
import com.top_logic.element.boundsec.manager.rule.config.SingletonRuleConfig;

/**
 * Settles the one piece of XML {@code doc/security/role-model-worked-example.md} flagged as
 * unconfirmed: whether {@code <group-with-name>} belongs inside a {@code <path>} wrapper within a
 * {@code <singleton-rule>}.
 *
 * <p>
 * <b>Both spellings are valid, and they parse to the same rule.</b>
 * {@code NavigationRuleConfig.getPathElements()} is annotated {@code @Name("path")} <em>and</em>
 * {@code @DefaultContainer}, and a default container means the entries may be written either wrapped in
 * their element or directly as children of the enclosing one. Ticket #29221's description shows the
 * unwrapped form; the worked example and {@code secdemo.config.xml} use the wrapper, because a path with
 * two steps is unambiguous that way.
 * </p>
 *
 * <p>
 * Asserted by parsing, not by reading annotations, because "this XML is accepted" is the claim the
 * document makes and an annotation is only the reason it is true. No application boot is needed: the
 * configuration framework resolves both forms on its own.
 * </p>
 */
public class TestRoleRulePathNesting extends BasicTestCase {

	private static final String WRAPPED = """
		<role-rules>
			<rules>
				<singleton-rule id="tier" target="SecurityStructure#ROOT" role="projectPartner">
					<path>
						<group-with-name name="projectPartners"/>
					</path>
				</singleton-rule>
			</rules>
		</role-rules>
		""";

	private static final String UNWRAPPED = """
		<role-rules>
			<rules>
				<singleton-rule id="tier" target="SecurityStructure#ROOT" role="projectPartner">
					<group-with-name name="projectPartners"/>
				</singleton-rule>
			</rules>
		</role-rules>
		""";

	/** The form the worked example and the fixture use. */
	public void testPathWrapperIsAccepted() {
		assertGroupPath(read(WRAPPED));
	}

	/** The form ticket #29221's description shows. */
	public void testUnwrappedStepIsAcceptedToo() {
		assertGroupPath(read(UNWRAPPED));
	}

	/** And they are the same rule, so the document may pick either without changing behaviour. */
	public void testBothFormsProduceTheSameRule() {
		SingletonRuleConfig wrapped = ruleOf(read(WRAPPED));
		SingletonRuleConfig unwrapped = ruleOf(read(UNWRAPPED));

		assertEquals(wrapped.getTarget(), unwrapped.getTarget());
		assertEquals(wrapped.getRole(), unwrapped.getRole());
		assertEquals(groupNameOf(wrapped), groupNameOf(unwrapped));
	}

	private static void assertGroupPath(RoleRulesConfig rules) {
		SingletonRuleConfig rule = ruleOf(rules);

		assertEquals("SecurityStructure#ROOT", rule.getTarget());
		assertEquals(List.of("projectPartner"), rule.getRole());
		assertEquals("projectPartners", groupNameOf(rule));
	}

	private static String groupNameOf(SingletonRuleConfig rule) {
		List<PolymorphicConfiguration<? extends PathElement>> path = rule.getPathElements();
		assertEquals("The path must hold exactly the one group element: " + path, 1, path.size());
		PolymorphicConfiguration<? extends PathElement> step = path.get(0);
		assertTrue("The step must be a <group-with-name>, but was " + step,
			step instanceof GroupWithName.Config);
		return ((GroupWithName.Config) step).getName();
	}

	private static SingletonRuleConfig ruleOf(RoleRulesConfig rules) {
		assertEquals(1, rules.getRules().size());
		Object rule = rules.getRules().get(0);
		assertTrue("Expected a <singleton-rule>, got " + rule, rule instanceof SingletonRuleConfig);
		return (SingletonRuleConfig) rule;
	}

	private static RoleRulesConfig read(String xml) {
		ConfigurationDescriptor descriptor =
			TypedConfiguration.getConfigurationDescriptor(RoleRulesConfig.class);
		CharacterContent content = CharacterContents.newContent(xml, "role-rules");
		// Collections.singletonMap rather than Map.of: the reader asserts that the map holds no null
		// key, and an immutable Map.of throws on containsKey(null) instead of answering it.
		Map<String, ConfigurationDescriptor> descriptors =
			Collections.singletonMap(RoleRulesConfig.XML_TAG_ROLE_RULES, descriptor);
		try {
			return (RoleRulesConfig) new ConfigurationReader(
				SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, descriptors)
					.setSource(content).read();
		} catch (ConfigurationException ex) {
			throw new AssertionError("The configuration framework refused this XML:\n" + xml, ex);
		}
	}

	/**
	 * Creates the test suite.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestRoleRulePathNesting.class)));
	}
}
