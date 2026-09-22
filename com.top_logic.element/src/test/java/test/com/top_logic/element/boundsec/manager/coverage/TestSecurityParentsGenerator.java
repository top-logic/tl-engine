/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.boundsec.manager.coverage;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.element.util.ElementWebTestSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.element.boundsec.manager.coverage.SecurityCoverageCheck;
import com.top_logic.element.boundsec.manager.coverage.SecurityParentsGenerator;
import com.top_logic.element.boundsec.manager.coverage.TypeCoverage;
import com.top_logic.element.boundsec.manager.rule.PathElement;
import com.top_logic.element.boundsec.manager.rule.config.NavigationRuleConfig;
import com.top_logic.element.boundsec.manager.rule.config.PathElementConfig;
import com.top_logic.element.boundsec.manager.rule.config.SecurityParentsConfig;

/**
 * Test for the {@link SecurityParentsGenerator} against the model module
 * {@code TestSecurityCoverage} of the element test application.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestSecurityParentsGenerator extends BasicTestCase {

	/** Name of the analyzed test model module. */
	private static final String MODULE = "TestSecurityCoverage";

	/** The only type of the module that is contained in exactly one composition. */
	private static final String SINGLE_CONTAINED = MODULE + ":SingleContained";

	/** The composition the proposed rule navigates backwards. */
	private static final String CONTAINER_REFERENCE = MODULE + ":Container#singles";

	/** Id of the rule proposed for {@link #SINGLE_CONTAINED}. */
	private static final String GENERATED_ID = "SingleContained_securityParent";

	public void testCollect() {
		SecurityParentsConfig generated = new SecurityParentsGenerator().collect(analyze());
		assertEquals("Exactly the type contained in a single composition gets a proposal.",
			List.of(GENERATED_ID), testModuleIds(generated));
		assertGeneratedRule(rule(generated, GENERATED_ID));
		assertSortedByType(generated);
	}

	public void testToXml() throws Exception {
		SecurityParentsGenerator generator = new SecurityParentsGenerator();
		SecurityParentsConfig generated = generator.collect(analyze());
		SecurityParentsConfig parsed = readSecurityParents(generator.toXml(generated));
		assertEquals(ids(generated), ids(parsed));
		assertGeneratedRule(rule(parsed, GENERATED_ID));
	}

	private static void assertGeneratedRule(NavigationRuleConfig rule) {
		assertEquals(SINGLE_CONTAINED, rule.getMetaElement());
		assertTrue("The rule applies to the sub types of the type, too.", rule.isInherit());
		List<PolymorphicConfiguration<? extends PathElement>> path = rule.getPathElements();
		assertEquals("The rule navigates a single step.", 1, path.size());
		PathElementConfig step = (PathElementConfig) path.get(0);
		assertEquals(CONTAINER_REFERENCE, step.getAttribute().qualifiedName());
		assertTrue("The composition is navigated backwards.", step.isInverse());
	}

	private static List<String> ids(SecurityParentsConfig config) {
		return config.getRules().stream().map(NavigationRuleConfig::getId).toList();
	}

	/** The ids of the rules defined for a type of {@link #MODULE}. */
	private static List<String> testModuleIds(SecurityParentsConfig config) {
		return config.getRules().stream()
			.filter(rule -> rule.getMetaElement().startsWith(MODULE + ":"))
			.map(NavigationRuleConfig::getId)
			.toList();
	}

	private static NavigationRuleConfig rule(SecurityParentsConfig config, String id) {
		return config.getRules().stream()
			.filter(candidate -> id.equals(candidate.getId()))
			.findFirst()
			.orElseThrow(() -> new AssertionError("No rule '" + id + "' in " + ids(config)));
	}

	private static void assertSortedByType(SecurityParentsConfig config) {
		List<String> types = config.getRules().stream().map(NavigationRuleConfig::getMetaElement).toList();
		assertEquals("The rules are collected in a stable order.", types.stream().sorted().toList(), types);
	}

	private static List<TypeCoverage> analyze() {
		return SecurityCoverageCheck.getInstance().analyze();
	}

	/**
	 * Reads the given XML back into the configuration it renders.
	 */
	private static SecurityParentsConfig readSecurityParents(String xml) throws Exception {
		Protocol log = new BufferingProtocol();
		Map<String, ConfigurationDescriptor> descriptors =
			Collections.singletonMap(SecurityParentsGenerator.SECURITY_PARENTS_TAG,
				TypedConfiguration.getConfigurationDescriptor(SecurityParentsConfig.class));
		SecurityParentsConfig result = (SecurityParentsConfig) new ConfigurationReader(
			new DefaultInstantiationContext(log), descriptors)
				.setSource(CharacterContents.newContent(xml))
				.read();
		log.checkErrors();
		return result;
	}

	/** Return the suite of tests to perform. */
	public static Test suite() {
		Test suite = ServiceTestSetup.createSetup(new TestSuite(TestSecurityParentsGenerator.class),
			SecurityCoverageCheck.Module.INSTANCE);
		return ElementWebTestSetup.createElementWebTestSetup(suite);
	}

}
