/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.boundsec.manager.coverage;

import java.io.File;
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
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.module.TypedRuntimeModule.ModuleConfiguration;
import com.top_logic.element.boundsec.manager.ElementAccessManager;
import com.top_logic.element.boundsec.manager.coverage.SecurityCoverageCheck;
import com.top_logic.element.boundsec.manager.coverage.SecurityParentsGenerator;
import com.top_logic.element.boundsec.manager.coverage.TypeCoverage;
import com.top_logic.element.boundsec.manager.rule.PathElement;
import com.top_logic.element.boundsec.manager.rule.config.NavigationRuleConfig;
import com.top_logic.element.boundsec.manager.rule.config.PathElementConfig;
import com.top_logic.element.boundsec.manager.rule.config.SecurityParentsConfig;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.util.autoconf.InAppServiceConfigStore;

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

	/** Id of the rule an application defined before the generator ran. */
	private static final String EXISTING_ID = "ExistingParent";

	/** Name of the file holding the stored access manager configuration. */
	private static final String CONFIG_FILE = AccessManager.class.getName() + InAppServiceConfigStore.FILE_SUFFIX;

	/** The access manager the element test application runs. */
	private static final String ACCESS_MANAGER_CLASS =
		"com.top_logic.element.boundsec.manager.StorageAccessManager";

	/**
	 * The access manager configuration of the underlying configuration layers, as
	 * {@code ElementConf.config.xml} defines it.
	 */
	private static final String BASE_CONFIG = """
			<application>
				<services>
					<config service-class="com.top_logic.tool.boundsec.manager.AccessManager">
						<instance class="%s">
							<security-parents>
								<rule
									id="%s"
									inherit="true"
									meta-element="%s"
								>
									<path>
										<step attribute="%s"/>
									</path>
								</rule>
							</security-parents>
						</instance>
					</config>
				</services>
			</application>
			""".formatted(ACCESS_MANAGER_CLASS, EXISTING_ID, SINGLE_CONTAINED, CONTAINER_REFERENCE);

	/**
	 * A stored configuration replacing the underlying configuration layers, as the service editor
	 * writes it.
	 */
	private static final String OVERRIDING_CONFIG = """
			<application xmlns:config="http://www.top-logic.com/ns/config/6.0">
				<services>
					<config
						config:override="true"
						service-class="com.top_logic.tool.boundsec.manager.AccessManager"
					>
						<instance class="%s">
							<security-parents>
								<rule
									id="%s"
									inherit="true"
									meta-element="%s"
								>
									<path>
										<step attribute="%s"/>
									</path>
								</rule>
							</security-parents>
						</instance>
					</config>
				</services>
			</application>
			""".formatted(ACCESS_MANAGER_CLASS, EXISTING_ID, SINGLE_CONTAINED, CONTAINER_REFERENCE);

	public void testCollect() {
		SecurityParentsConfig generated = generator(new File("ignored")).collect(analyze());

		assertEquals("Exactly the type contained in a single composition gets a proposal.",
			List.of(GENERATED_ID), testModuleIds(generated));
		assertGeneratedRule(rule(generated, GENERATED_ID));
		assertSortedByType(generated);
	}

	public void testToXml() throws Exception {
		SecurityParentsGenerator generator = generator(new File("ignored"));
		String xml = generator.toXml(generator.collect(analyze()));

		SecurityParentsConfig parsed = readSecurityParents(xml);
		assertEquals(ids(generator.collect(analyze())), ids(parsed));
		assertGeneratedRule(rule(parsed, GENERATED_ID));
	}

	public void testWriteToNewFile() throws Exception {
		File file = configFile("testWriteToNewFile");
		SecurityParentsGenerator generator = generator(file);

		assertEquals(file, generator.writeToAutoconf(generator.collect(analyze())));
		assertTrue("The configuration is stored.", file.exists());
		assertFalse("A generated entry is layered onto the underlying configuration.",
			InAppServiceConfigStore.isOverriding(file));

		ModuleConfiguration merged = accessManagerEntry(overlay(BASE_CONFIG, file));
		assertEquals("The implementation class of the underlying configuration stays in effect: " + content(file),
			ACCESS_MANAGER_CLASS, instance(merged).getImplementationClass().getName());
		assertEquals("The rules of both configuration layers are kept: " + content(file),
			List.of(EXISTING_ID, GENERATED_ID), testModuleIds(instance(merged).getSecurityParents()));
	}

	public void testWriteTwiceIsIdempotent() throws Exception {
		File file = configFile("testWriteTwiceIsIdempotent");
		SecurityParentsGenerator generator = generator(file);

		generator.writeToAutoconf(generator.collect(analyze()));
		String first = content(file);
		generator.writeToAutoconf(generator.collect(analyze()));

		assertEquals("A rule is replaced by its id, not appended a second time.", first, content(file));
	}

	public void testWriteToOverridingFile() throws Exception {
		File file = configFile("testWriteToOverridingFile");
		FileUtilities.writeStringToFile(OVERRIDING_CONFIG, file, StringServices.UTF8);
		SecurityParentsGenerator generator = generator(file);

		generator.writeToAutoconf(generator.collect(analyze()));

		assertTrue("The stored entry keeps replacing the underlying configuration.",
			InAppServiceConfigStore.isOverriding(file));
		ModuleConfiguration stored = accessManagerEntry(read(file));
		assertEquals("The stored implementation class is kept.",
			ACCESS_MANAGER_CLASS, instance(stored).getImplementationClass().getName());
		assertEquals("The generated rule is appended to the stored ones.",
			List.of(EXISTING_ID, GENERATED_ID), testModuleIds(instance(stored).getSecurityParents()));
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

	private static SecurityParentsGenerator generator(File file) {
		return new SecurityParentsGenerator(file);
	}

	private static List<TypeCoverage> analyze() {
		return SecurityCoverageCheck.getInstance().analyze();
	}

	private static File configFile(String testName) {
		return new File(createdCleanTestDir(testName), CONFIG_FILE);
	}

	private static String content(File file) throws Exception {
		return FileUtilities.readFileToString(file, StringServices.UTF8);
	}

	private static ApplicationConfig.Config read(File file) throws Exception {
		return InAppServiceConfigStore.read(file);
	}

	/**
	 * Reads the given file on top of the given base configuration, in the same way as an
	 * application layers its configuration onto the one of the framework.
	 */
	private static ApplicationConfig.Config overlay(String base, File increment) throws Exception {
		Protocol log = new BufferingProtocol();
		ApplicationConfig.Config baseConfig = (ApplicationConfig.Config) newAppConfigReader(log)
			.setSource(CharacterContents.newContent(base))
			.read();
		ApplicationConfig.Config result = (ApplicationConfig.Config) newAppConfigReader(log)
			.setBaseConfig(baseConfig)
			.setSource(BinaryDataFactory.createBinaryData(increment))
			.read();
		log.checkErrors();
		return result;
	}

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

	private static ConfigurationReader newAppConfigReader(Protocol log) {
		Map<String, ConfigurationDescriptor> descriptors =
			Collections.singletonMap(InAppServiceConfigStore.APPLICATION_TAG,
				TypedConfiguration.getConfigurationDescriptor(ApplicationConfig.Config.class));
		return new ConfigurationReader(new DefaultInstantiationContext(log), descriptors);
	}

	private static ModuleConfiguration accessManagerEntry(ApplicationConfig.Config config) {
		ModuleConfiguration result = config.getServices().get(AccessManager.class);
		assertNotNull("The access manager is configured.", result);
		return result;
	}

	private static ElementAccessManager.Config instance(ModuleConfiguration entry) {
		return (ElementAccessManager.Config) entry.getInstance();
	}

	/** Return the suite of tests to perform. */
	public static Test suite() {
		Test suite = ServiceTestSetup.createSetup(new TestSuite(TestSecurityParentsGenerator.class),
			SecurityCoverageCheck.Module.INSTANCE);
		return ElementWebTestSetup.createElementWebTestSetup(suite);
	}

}
