/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.mandatory;

import static com.top_logic.basic.config.TypedConfiguration.*;
import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;
import test.com.top_logic.basic.ExpectedFailure;
import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;
import test.com.top_logic.basic.config.TypedConfigurationSzenario;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.DerivedUsesMandatory;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.MandatoryNoDefaultValueInstantiation;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.MandatoryTest;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.MandatoryTest2;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.MandatoryTest3;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.MandatoryWithNullDefaultFormat;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.Regression20851_1;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.Regression20851_2;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.ScenarioContainerNoAutoload.ScenarioTypeAddMandatoryInMultipleSupers;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.ScenarioContainerNoAutoload.ScenarioTypeImplicitDefault2;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.ScenarioContainerNoAutoload.ScenarioTypeSubOf_DefaultByTypeParameter;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.ScenarioContainerNoAutoload.ScenarioTypeSubOf_DefaultByTypeParameterSpecialized;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.ScenarioContainerNoAutoload.ScenarioTypeSubOf_FormattedDefaultAnnotation;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.ScenarioContainerNoAutoload.ScenarioTypeSubOf_ImplementationClassDefault;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.ScenarioContainerNoAutoload.ScenarioTypeSubOf_ImplementationClassDefaultSpecialized;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.ScenarioContainerNoAutoload.ScenarioTypeSubOf_ItemDefaultAnnotation;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.ScenarioContainerNoAutoload.ScenarioTypeSubOf_NoTypeParameter1;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.ScenarioContainerNoAutoload.ScenarioTypeSubOf_NoTypeParameter2;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.ScenarioContainerNoAutoload.ScenarioTypeSubOf_NoTypeParameter3;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.ScenarioContainerNoAutoload.ScenarioTypeSubOf_NoTypeParameter4;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.ScenarioContainerNoAutoload.ScenarioTypeSubOf_StringDefaultAnnotation;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.ScenarioTypeA;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.ScenarioTypeB;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.ScenarioTypeD;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.ScenarioTypeE;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.ScenarioTypeF;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.ScenarioTypeG;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.ScenarioTypeH;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.ScenarioTypeI;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.ScenarioTypeIndirectRecursiveConfigA;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.ScenarioTypeInstanceFormatDefault;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.ScenarioTypeL;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.ScenarioTypeList;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.ScenarioTypeListWithConfiguredInstances;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.ScenarioTypeM;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.ScenarioTypeMandatoryList;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.ScenarioTypeMandatoryMap;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.ScenarioTypeMandatoryProperty;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.ScenarioTypeMap;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.ScenarioTypeMapWithConfiguredInstances;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.ScenarioTypeO;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.ScenarioTypeP;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.ScenarioTypeRecursiveConfig;
import test.com.top_logic.basic.config.mandatory.ScenarioMandatory.ScenarioTypeWithConfiguredInstance;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ConfigBuilder;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationErrorProtocol;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;

/**
 * Test case for {@link ConfigurationReader}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestMandatoryAnnotation extends AbstractTypedConfigurationTestCase implements TypedConfigurationSzenario {

	public void testMandatoryAttributeNeedsNoDefaultValue() {
		MandatoryNoDefaultValueInstantiation newConfigItem =
			TypedConfiguration.newConfigItem(MandatoryNoDefaultValueInstantiation.class);
		assertNotNull(newConfigItem);
		assertTrue("There is no need to access default value in mandatory attributes.",
			MandatoryNoDefaultValueInstantiation.ConfigValueProvider.DEFAULT_VALUE_CALLS.isEmpty());

	}

	public void testCheckValid() {
		ScenarioTypeMandatoryProperty config = TypedConfiguration.newConfigItem(ScenarioTypeMandatoryProperty.class);
		config.setTestProperty(0);
		config.check(ConfigurationErrorProtocol.INSTANCE);
	}

	public void testCheckValidList() {
		ScenarioTypeMandatoryList config = TypedConfiguration.newConfigItem(ScenarioTypeMandatoryList.class);
		ScenarioTypeMandatoryProperty entry = TypedConfiguration.newConfigItem(ScenarioTypeMandatoryProperty.class);
		entry.setTestProperty(42);
		config.getValues().add(entry);
		config.check(ConfigurationErrorProtocol.INSTANCE);
	}

	public void testCheckValidListBuilder() throws AssertionFailedError {
		ConfigBuilder builder = TypedConfiguration.createConfigBuilder(ScenarioTypeMandatoryList.class);
		initFailureTest();
		try {
			builder.createConfig(context);
			assertTrue("Mandatory property is not set.", context.hasErrors());
		} catch (NullPointerException ex) {
			throw BasicTestCase.fail("Ticket #23177: Instantiation must not fail with RuntimeException.", ex);
		}
		builder = TypedConfiguration.createConfigBuilder(ScenarioTypeMandatoryList.class);
		initDefaultTest();
		ScenarioTypeMandatoryProperty entry = TypedConfiguration.newConfigItem(ScenarioTypeMandatoryProperty.class);
		entry.setTestProperty(42);
		builder.update(builder.descriptor().getProperty(ScenarioTypeMandatoryList.VALUES),
			Collections.singletonList(entry));
		ConfigurationItem config = builder.createConfig(context);
		assertFalse(context.hasErrors());
		config.check(ConfigurationErrorProtocol.INSTANCE);
	}

	public void testCheckValidMap() {
		ScenarioTypeMandatoryMap config = TypedConfiguration.newConfigItem(ScenarioTypeMandatoryMap.class);
		ScenarioTypeMandatoryProperty entry = TypedConfiguration.newConfigItem(ScenarioTypeMandatoryProperty.class);
		entry.setTestProperty(42);
		config.getValues().put(entry.getTestProperty(), entry);
		config.check(ConfigurationErrorProtocol.INSTANCE);
	}

	public void testCheckValidMapBuilder() throws AssertionFailedError {
		ConfigBuilder builder = TypedConfiguration.createConfigBuilder(ScenarioTypeMandatoryMap.class);
		initFailureTest();
		try {
			builder.createConfig(context);
			assertTrue("Mandatory property is not set.", context.hasErrors());
		} catch (NullPointerException ex) {
			throw BasicTestCase.fail("Ticket #23177: Instantiation must not fail with RuntimeException.", ex);
		}
		builder = TypedConfiguration.createConfigBuilder(ScenarioTypeMandatoryMap.class);
		initDefaultTest();
		ScenarioTypeMandatoryProperty entry = TypedConfiguration.newConfigItem(ScenarioTypeMandatoryProperty.class);
		entry.setTestProperty(42);
		builder.update(builder.descriptor().getProperty(ScenarioTypeMandatoryMap.VALUES),
			Collections.singletonMap(entry.getTestProperty(), entry));
		ConfigurationItem config = builder.createConfig(context);
		assertFalse(context.hasErrors());
		config.check(ConfigurationErrorProtocol.INSTANCE);
	}

	public void testCheckInvalid() {
		ScenarioTypeMandatoryProperty config = TypedConfiguration.newConfigItem(ScenarioTypeMandatoryProperty.class);
		protocol = new BufferingProtocol();
		config.check(protocol);
		assertTrue("Unset mandatory property was not reported as error.", protocol.hasErrors());
	}

	public void testCheckMandatoryAfterRead() {
		initFailureTest();
		try {
			read("<ScenarioTypeMandatoryProperty/>");
			fail("Must fail since mandatory property is not set.");
		} catch (ConfigurationException ex) {
			assertContains("mandatory but not set", ex.getMessage());
		}
	}

	public void testCheckInvalidList() {
		ScenarioTypeMandatoryList config = TypedConfiguration.newConfigItem(ScenarioTypeMandatoryList.class);
		protocol = new BufferingProtocol();
		config.check(protocol);
		assertTrue("Unset mandatory property was not reported as error.", protocol.hasErrors());
	}

	public void testCheckInvalidMap() {
		ScenarioTypeMandatoryMap config = TypedConfiguration.newConfigItem(ScenarioTypeMandatoryMap.class);
		protocol = new BufferingProtocol();
		config.check(protocol);
		assertTrue("Unset mandatory property was not reported as error.", protocol.hasErrors());
	}

	public void testSimple() {
		assertMandatory(ScenarioTypeMandatoryProperty.class, "test-property", "Property is not mandatory.");
	}

	public void testSimpleValid() throws ConfigurationException {
		ScenarioTypeMandatoryProperty config =
			(ScenarioTypeMandatoryProperty) readWithScenario("<ScenarioTypeMandatoryProperty test-property='3' />");
		assertEquals(3, config.getTestProperty());
	}

	public void testSimpleInvalid() throws ConfigurationException {
		try {
			readWithScenario("<ScenarioTypeMandatoryProperty />");
		} catch (AssertionFailedError error) {
			// Good
			return;
		}
		fail("Mandatory property is not set, but instantiation did not fail.");
	}

	public void testInSubclass() {
		assertMandatory(ScenarioTypeE.class, "test-property", "Property is not mandatory.");
	}

	public void testInSubclassValid() throws ConfigurationException {
		ScenarioTypeE config = (ScenarioTypeE) readWithScenario("<ScenarioTypeE test-property='3' />");
		assertEquals(3, config.getTestProperty());
	}

	public void testInSubclassInvalid() throws ConfigurationException {
		try {
			readWithScenario("<ScenarioTypeE />");
		} catch (AssertionFailedError error) {
			// Good
			return;
		}
		fail("Mandatory property is not set, but instantiation did not fail.");
	}

	public void testInSuperclass() {
		assertMandatory(ScenarioTypeA.class, "test-property", "Property is not mandatory.");
	}

	public void testInSuperclassValid() throws ConfigurationException {
		ScenarioTypeA config = (ScenarioTypeA) readWithScenario("<ScenarioTypeA test-property='3' />");
		assertEquals(3, config.getTestProperty());
	}

	public void testInSuperclassInvalid() throws ConfigurationException {
		try {
			readWithScenario("<ScenarioTypeA />");
		} catch (AssertionFailedError error) {
			// Good
			return;
		}
		fail("Ticket #12554: Mandatory property is not set, but instantiation did not fail.");
	}

	public void testItemWithMandatoryPropertyNotMentioned() throws ConfigurationException {
		ScenarioTypeB config = (ScenarioTypeB) readWithScenario("<ScenarioTypeB />");

		assertNull(config.getConfigWithMandatory());
	}

	public void testItemWithMandatoryPropertyMentionedValid() throws ConfigurationException {
		ScenarioTypeB config = (ScenarioTypeB) readWithScenario(
			"<ScenarioTypeB><config-with-mandatory test-property='4' /></ScenarioTypeB>");

		assertEquals(4, config.getConfigWithMandatory().getTestProperty());
	}

	public void testItemWithMandatoryPropertyMentionedInvalid() throws ConfigurationException {
		try {
			readWithScenario("<ScenarioTypeB><config-with-mandatory /></ScenarioTypeB>");
		} catch (AssertionFailedError error) {
			// Good
			return;
		}
		fail("Ticket #12554: Config mentions a property with a mandatory property, which is not set, but instantiation did not fail.");
	}

	public void testItemWithMandatoryPropertyMentionedButSetOnlyInRefinement() throws ConfigurationException {
		String baseXml = "<ScenarioTypeB><config-with-mandatory /></ScenarioTypeB>";
		String refinementXml = "<ScenarioTypeB><config-with-mandatory test-property='4' /></ScenarioTypeB>";
		ScenarioTypeB config = (ScenarioTypeB) readWithScenario(baseXml, refinementXml);

		assertEquals(4, config.getConfigWithMandatory().getTestProperty());
	}

	public void testConfiguredInstanceWithMandatoryPropertyNotMentioned() throws ConfigurationException {
		ScenarioTypeWithConfiguredInstance config =
			(ScenarioTypeWithConfiguredInstance) readWithScenario("<ScenarioTypeWithConfiguredInstance />");

		assertNull(config.getExample());
	}

	public void testConfiguredInstanceWithMandatoryPropertyMentionedValid() throws ConfigurationException {
		ScenarioTypeWithConfiguredInstance config =
			(ScenarioTypeWithConfiguredInstance) readWithScenario(
			"<ScenarioTypeWithConfiguredInstance><example test-property='4' /></ScenarioTypeWithConfiguredInstance>");

		assertEquals(4, config.getExample().getConfig().getTestProperty());
	}

	public void testConfiguredInstanceWithMandatoryPropertyMentionedInvalid() throws ConfigurationException {
		try {
			readWithScenario("<ScenarioTypeWithConfiguredInstance><example /></ScenarioTypeWithConfiguredInstance>");
		} catch (AssertionFailedError error) {
			// Good
			return;
		}
		fail("Config mentions a ConfiguredInstance with a mandatory property, which is not set, but instantiation did not fail.");
	}

	public void testConfiguredInstanceWithMandatoryPropertyMentionedButSetOnlyInRefinement()
			throws ConfigurationException {
		String baseXml =
			"<ScenarioTypeWithConfiguredInstance><example /></ScenarioTypeWithConfiguredInstance>";
		String refinementXml =
			"<ScenarioTypeWithConfiguredInstance><example test-property='4' /></ScenarioTypeWithConfiguredInstance>";
		ScenarioTypeWithConfiguredInstance config =
			(ScenarioTypeWithConfiguredInstance) readWithScenario(baseXml, refinementXml);

		assertEquals(4, config.getExample().getConfig().getTestProperty());
	}

	public void testListOfConfigsWithMandatoryPropertyEmpty() throws ConfigurationException {
		ScenarioTypeList config = (ScenarioTypeList) readWithScenario("<ScenarioTypeList />");

		assertTrue(config.getConfigsWithMandatory().isEmpty());
	}

	public void testListOfConfigsWithMandatoryPropertyValid() throws ConfigurationException {
		ScenarioTypeList config = (ScenarioTypeList) readWithScenario("<ScenarioTypeList><configs-with-mandatory>"
			+ "<entry key-property='1' test-property='5' /><entry key-property='2' test-property='6' />"
			+ "</configs-with-mandatory></ScenarioTypeList>");

		assertEquals(2, config.getConfigsWithMandatory().size());
		assertEquals(5, config.getConfigsWithMandatory().get(0).getTestProperty());
		assertEquals(6, config.getConfigsWithMandatory().get(1).getTestProperty());
	}

	public void testListOfConfigsWithMandatoryPropertyInvalid() throws ConfigurationException {
		try {
			readWithScenario("<ScenarioTypeList><configs-with-mandatory>"
				+ "<entry key-property='1' /><entry key-property='2' />"
				+ "</configs-with-mandatory></ScenarioTypeList>");
		} catch (AssertionFailedError error) {
			// Good
			return;
		}
		fail("Ticket #12554: Config contains a list with two configs, whose mandatory property is not set,"
			+ " but instantiation did not fail.");
	}

	public void testListOfConfigsWithMandatoryPropertyUpdate() throws ConfigurationException {
		String baseXml = "<ScenarioTypeList><configs-with-mandatory>"
			+ "<entry key-property='1' /><entry key-property='2' />"
			+ "</configs-with-mandatory></ScenarioTypeList>";
		String refinementXml = "<ScenarioTypeList><configs-with-mandatory>"
			+ "<entry key-property='1' test-property='5' /><entry key-property='2' test-property='6' />"
			+ "</configs-with-mandatory></ScenarioTypeList>";
		ScenarioTypeList config = (ScenarioTypeList) readWithScenario(baseXml, refinementXml);

		assertEquals(2, config.getConfigsWithMandatory().size());
		assertEquals(5, config.getConfigsWithMandatory().get(0).getTestProperty());
		assertEquals(6, config.getConfigsWithMandatory().get(1).getTestProperty());
	}

	public void testListOfConfiguredInstancesWithMandatoryPropertyEmpty() throws ConfigurationException {
		ScenarioTypeListWithConfiguredInstances config =
			(ScenarioTypeListWithConfiguredInstances) readWithScenario("<ScenarioTypeListWithConfiguredInstances />");

		assertTrue(config.getConfigsWithMandatory().isEmpty());
	}

	public void testListOfConfiguredInstancesWithMandatoryPropertyValid() throws ConfigurationException {
		ScenarioTypeListWithConfiguredInstances config =
			(ScenarioTypeListWithConfiguredInstances) readWithScenario("<ScenarioTypeListWithConfiguredInstances><configs-with-mandatory>"
				+ "<entry key-property='1' test-property='5' /><entry key-property='2' test-property='6' />"
				+ "</configs-with-mandatory></ScenarioTypeListWithConfiguredInstances>");

		assertEquals(2, config.getConfigsWithMandatory().size());
		assertEquals(5, config.getConfigsWithMandatory().get(0).getConfig().getTestProperty());
		assertEquals(6, config.getConfigsWithMandatory().get(1).getConfig().getTestProperty());
	}

	public void testListOfConfiguredInstancesWithMandatoryPropertyInvalid() throws ConfigurationException {
		try {
			readWithScenario("<ScenarioTypeListWithConfiguredInstances><configs-with-mandatory>"
				+ "<entry key-property='1' /><entry key-property='2' />"
				+ "</configs-with-mandatory></ScenarioTypeListWithConfiguredInstances>");
		} catch (AssertionFailedError error) {
			// Good
			return;
		}
		fail("Config contains a list with two ConfiguredInstances, whose mandatory property is not set,"
			+ " but instantiation did not fail.");
	}

	public void testListOfConfiguredInstancesWithMandatoryPropertyUpdate() throws ConfigurationException {
		String baseXml = "<ScenarioTypeListWithConfiguredInstances><configs-with-mandatory>"
			+ "<entry key-property='1' /><entry key-property='2' />"
			+ "</configs-with-mandatory></ScenarioTypeListWithConfiguredInstances>";
		String refinementXml = "<ScenarioTypeListWithConfiguredInstances><configs-with-mandatory>"
			+ "<entry key-property='1' test-property='5' /><entry key-property='2' test-property='6' />"
			+ "</configs-with-mandatory></ScenarioTypeListWithConfiguredInstances>";
		ScenarioTypeListWithConfiguredInstances config =
			(ScenarioTypeListWithConfiguredInstances) readWithScenario(baseXml, refinementXml);

		assertEquals(2, config.getConfigsWithMandatory().size());
		assertEquals(5, config.getConfigsWithMandatory().get(0).getConfig().getTestProperty());
		assertEquals(6, config.getConfigsWithMandatory().get(1).getConfig().getTestProperty());
	}

	public void testMapOfConfigsWithMandatoryPropertyEmpty() throws ConfigurationException {
		ScenarioTypeMap config = (ScenarioTypeMap) readWithScenario("<ScenarioTypeMap />");

		assertTrue(config.getConfigsWithMandatory().isEmpty());
	}

	public void testMapOfConfigsWithMandatoryPropertyValid() throws ConfigurationException {
		ScenarioTypeMap config = (ScenarioTypeMap) readWithScenario("<ScenarioTypeMap><configs-with-mandatory>"
			+ "<entry key-property='1' test-property='5' /><entry key-property='2' test-property='6' />"
			+ "</configs-with-mandatory></ScenarioTypeMap>");

		assertEquals(2, config.getConfigsWithMandatory().size());
		assertEquals(5, config.getConfigsWithMandatory().get(1).getTestProperty());
		assertEquals(6, config.getConfigsWithMandatory().get(2).getTestProperty());
	}

	public void testMapOfConfigsWithMandatoryPropertyInvalid() throws ConfigurationException {
		try {
			readWithScenario("<ScenarioTypeMap><configs-with-mandatory>"
				+ "<entry key-property='1' /><entry key-property='2' />"
				+ "</configs-with-mandatory></ScenarioTypeMap>");
		} catch (AssertionFailedError error) {
			// Good
			return;
		}
		fail("Ticket #12554: Config contains a map with two configs, whose mandatory property is not set,"
			+ " but instantiation did not fail.");
	}

	public void testMapOfConfigsWithMandatoryPropertyUpdate() throws ConfigurationException {
		String baseXml = "<ScenarioTypeMap><configs-with-mandatory>"
			+ "<entry key-property='1' /><entry key-property='2' />"
			+ "</configs-with-mandatory></ScenarioTypeMap>";
		String refinementXml = "<ScenarioTypeMap><configs-with-mandatory>"
			+ "<entry key-property='1' test-property='5' /><entry key-property='2' test-property='6' />"
			+ "</configs-with-mandatory></ScenarioTypeMap>";
		ScenarioTypeMap config = (ScenarioTypeMap) readWithScenario(baseXml, refinementXml);

		assertEquals(2, config.getConfigsWithMandatory().size());
		assertEquals(5, config.getConfigsWithMandatory().get(1).getTestProperty());
		assertEquals(6, config.getConfigsWithMandatory().get(2).getTestProperty());
	}

	public void testMapOfConfiguredInstancesWithMandatoryPropertyEmpty() throws ConfigurationException {
		ScenarioTypeMapWithConfiguredInstances config =
			(ScenarioTypeMapWithConfiguredInstances) readWithScenario("<ScenarioTypeMapWithConfiguredInstances />");

		assertTrue(config.getConfigsWithMandatory().isEmpty());
	}

	public void testMapOfConfiguredInstancesWithMandatoryPropertyValid() throws ConfigurationException {
		ScenarioTypeMapWithConfiguredInstances config =
			(ScenarioTypeMapWithConfiguredInstances) readWithScenario("<ScenarioTypeMapWithConfiguredInstances><configs-with-mandatory>"
				+ "<entry key-property='1' test-property='5' /><entry key-property='2' test-property='6' />"
				+ "</configs-with-mandatory></ScenarioTypeMapWithConfiguredInstances>");

		assertEquals(2, config.getConfigsWithMandatory().size());
		assertEquals(5, config.getConfigsWithMandatory().get(1).getConfig().getTestProperty());
		assertEquals(6, config.getConfigsWithMandatory().get(2).getConfig().getTestProperty());
	}

	public void testMapOfConfiguredInstancesWithMandatoryPropertyInvalid() throws ConfigurationException {
		try {
			readWithScenario("<ScenarioTypeMapWithConfiguredInstances><configs-with-mandatory>"
				+ "<entry key-property='1' /><entry key-property='2' />"
				+ "</configs-with-mandatory></ScenarioTypeMapWithConfiguredInstances>");
		} catch (AssertionFailedError error) {
			// Good
			return;
		}
		fail("Config contains a map with two ConfiguredInstances, whose mandatory property is not set,"
			+ " but instantiation did not fail.");
	}

	public void testMapOfConfiguredInstancesWithMandatoryPropertyUpdate() throws ConfigurationException {
		String baseXml = "<ScenarioTypeMapWithConfiguredInstances><configs-with-mandatory>"
			+ "<entry key-property='1' /><entry key-property='2' />"
			+ "</configs-with-mandatory></ScenarioTypeMapWithConfiguredInstances>";
		String refinementXml = "<ScenarioTypeMapWithConfiguredInstances><configs-with-mandatory>"
			+ "<entry key-property='1' test-property='5' /><entry key-property='2' test-property='6' />"
			+ "</configs-with-mandatory></ScenarioTypeMapWithConfiguredInstances>";
		ScenarioTypeMapWithConfiguredInstances config =
			(ScenarioTypeMapWithConfiguredInstances) readWithScenario(baseXml, refinementXml);

		assertEquals(2, config.getConfigsWithMandatory().size());
		assertEquals(5, config.getConfigsWithMandatory().get(1).getConfig().getTestProperty());
		assertEquals(6, config.getConfigsWithMandatory().get(2).getConfig().getTestProperty());
	}

	public void testMandatoryListEmpty() throws ConfigurationException {
		ScenarioTypeD config = (ScenarioTypeD) readWithScenario("<ScenarioTypeD test-list-property='' />");

		assertTrue(config.getTestListProperty().isEmpty());
	}

	public void testMandatoryListNonEmpty() throws ConfigurationException {
		ScenarioTypeD config = (ScenarioTypeD) readWithScenario("<ScenarioTypeD test-list-property='7,8' />");

		assertEquals(2, config.getTestListProperty().size());
		assertEquals("7", config.getTestListProperty().get(0));
		assertEquals("8", config.getTestListProperty().get(1));
	}

	public void testMandatoryListInvalid() throws ConfigurationException {
		try {
			readWithScenario("<ScenarioTypeD />");
		} catch (AssertionFailedError error) {
			// Good
			return;
		}
		fail("Ticket #12554: Config contains a list with two configs, whose mandatory property is not set, but instantiation did not fail.");
	}

	public void testSetOnlyInRefinement() throws Throwable {
		String firstDefinition = "<ScenarioTypeMandatoryProperty />";
		String refinement = "<ScenarioTypeMandatoryProperty test-property='5' />";
		ScenarioTypeMandatoryProperty item =
			(ScenarioTypeMandatoryProperty) readWithScenario(firstDefinition, refinement);
		assertEquals(5, item.getTestProperty());
	}

	public void testSetOnlyInBaseConfig() throws Throwable {
		String firstDefinition = "<ScenarioTypeMandatoryProperty test-property='6' />";
		String refinement = "<ScenarioTypeMandatoryProperty />";
		ScenarioTypeMandatoryProperty item = null;
		try {
			item = (ScenarioTypeMandatoryProperty) readWithScenario(firstDefinition, refinement);
		} catch (AssertionFailedError ex) {
			throw BasicTestCase.fail("Ticket #12554: " + ex.getMessage(), ex);
		}
		assertEquals(6, item.getTestProperty());
	}

	public void testSetInBaseConfigAndRefinement() throws Throwable {
		String firstDefinition = "<ScenarioTypeMandatoryProperty test-property='7' />";
		String refinement = "<ScenarioTypeMandatoryProperty test-property='8' />";
		ScenarioTypeMandatoryProperty item =
			(ScenarioTypeMandatoryProperty) readWithScenario(firstDefinition, refinement);
		assertEquals(8, item.getTestProperty());
	}

	public void testSetNeitherInBaseConfigNorInRefinement() throws ConfigurationException {
		String firstDefinition = "<ScenarioTypeMandatoryProperty />";
		String refinement = "<ScenarioTypeMandatoryProperty />";
		try {
			readWithScenario(firstDefinition, refinement);
		} catch (AssertionFailedError error) {
			// Good
			return;
		}
		fail("Property is set neither in base config nor in refinement, but instantiation did not fail.");
	}

	public void testPropertyMultipleInheritanceOrderMandatoryFirst() {
		Class<ScenarioTypeL> configInterface = ScenarioTypeL.class;
		String propertyName = "test-property";
		String failureMessage = "Mandatory annotation was not inherited from first super interface.";

		assertMandatory(configInterface, propertyName, failureMessage);
	}

	public void testPropertyMultipleInheritanceOrderMandatorySecond() {
		Class<ScenarioTypeM> configInterface = ScenarioTypeM.class;
		String propertyName = "test-property";
		String failureMessage = "Mandatory annotation was not inherited from second super interface.";

		assertMandatory(configInterface, propertyName, failureMessage);
	}

	public void testPropertyIndirectMultipleInheritanceOrderMandatoryFirst() {
		Class<ScenarioTypeO> configInterface = ScenarioTypeO.class;
		String propertyName = "test-property";
		String failureMessage = "Mandatory annotation was not inherited from super interface of first super interface.";

		assertMandatory(configInterface, propertyName, failureMessage);
	}

	public void testPropertyIndirectMultipleInheritanceOrderMandatorySecond() {
		Class<ScenarioTypeP> configInterface = ScenarioTypeP.class;
		String propertyName = "test-property";
		String failureMessage =
			"Mandatory annotation was not inherited from super interface of second super interface.";

		assertMandatory(configInterface, propertyName, failureMessage);
	}

	public void testMultipleInheritanceOrderMandatoryFirst() {
		Class<ScenarioTypeF> configInterface = ScenarioTypeF.class;
		String propertyName = "test-property";
		String failureMessage = "Mandatory annotation was not inherited from first super interface.";

		assertMandatory(configInterface, propertyName, failureMessage);
	}

	public void testMultipleInheritanceOrderMandatorySecond() {
		Class<ScenarioTypeG> configInterface = ScenarioTypeG.class;
		String propertyName = "test-property";
		String failureMessage = "Mandatory annotation was not inherited from second super interface.";

		assertMandatory(configInterface, propertyName, failureMessage);
	}

	public void testIndirectMultipleInheritanceOrderMandatoryFirst() {
		Class<ScenarioTypeH> configInterface = ScenarioTypeH.class;
		String propertyName = "test-property";
		String failureMessage = "Mandatory annotation was not inherited from super interface of first super interface.";

		assertMandatory(configInterface, propertyName, failureMessage);
	}

	public void testIndirectMultipleInheritanceOrderMandatorySecond() {
		Class<ScenarioTypeI> configInterface = ScenarioTypeI.class;
		String propertyName = "test-property";
		String failureMessage =
			"Mandatory annotation was not inherited from super interface of second super interface.";

		assertMandatory(configInterface, propertyName, failureMessage);
	}

	public void testAddMandatoryInMultipleSupers() {
		Class<ScenarioTypeAddMandatoryInMultipleSupers> config = ScenarioTypeAddMandatoryInMultipleSupers.class;

		String propertyName = "test-property";
		String failureMessage =
			"Mandatory annotation was not inherited when both super interfaces redeclare it as mandatory.";

		assertMandatory(config, propertyName, failureMessage);
	}

	public void testMandatoryOverridesDefault_StringDefaultAnnotation() {
		assertMandatory(ScenarioTypeSubOf_StringDefaultAnnotation.class, "test-property",
			"Property is not mandatory, when the super interface has a StringDefault annotation.");
	}

	public void testMandatoryOverridesDefault_FormattedDefaultAnnotation() {
		assertMandatory(ScenarioTypeSubOf_FormattedDefaultAnnotation.class, "test-property",
			"Property is not mandatory, when the super interface has a FormattedDefault annotation.");
	}

	public void testMandatoryOverridesDefault_ItemDefaultAnnotation() {
		assertMandatory(ScenarioTypeSubOf_ItemDefaultAnnotation.class, "test-property",
			"Property is not mandatory, when the super interface has a ItemDefault annotation.");
	}

	public void testMandatoryOverridesDefault_ImplementationClassDefault() {
		assertMandatory(ScenarioTypeSubOf_ImplementationClassDefault.class, "class",
			"Property is not mandatory, when the super interface specifies the default implementation class"
				+ " with the ClassDefault annotation.");
	}

	public void testMandatoryOverridesDefault_ImplementationClassDefaultSpecialized() {
		assertMandatory(ScenarioTypeSubOf_ImplementationClassDefaultSpecialized.class, "class",
			"Property is not mandatory, when the super interface specifies the default implementation class"
				+ " with the ClassDefault annotation and the sub interface"
				+ " defines a sub type of that as type parameter in the getter.");
	}

	public void testMandatoryOverridesDefault_DefaultByTypeParameter() {
		assertMandatory(ScenarioTypeSubOf_DefaultByTypeParameter.class, "class",
			"Property is not mandatory, when the super interface specifies the default implementation class"
				+ " via the type parameter.");
	}

	public void testMandatoryOverridesDefault_DefaultByTypeParameterSpecialized() {
		assertMandatory(ScenarioTypeSubOf_DefaultByTypeParameterSpecialized.class, "class",
			"Property is not mandatory, when the super interface specifies the default implementation class"
				+ " via the type parameter and the sub interface"
				+ " defines a sub type of that as type parameter in the getter.");
	}

	public void testMandatoryOverridesDefault_NoTypeParameter1() {
		assertMandatory(ScenarioTypeSubOf_NoTypeParameter1.class, "class",
			"Property is not mandatory, when the super interface ignores the type parameter.");
	}

	public void testMandatoryOverridesDefault_NoTypeParameter2() {
		assertMandatory(ScenarioTypeSubOf_NoTypeParameter2.class, "class",
			"Property is not mandatory, when the super interface ignores the type parameter.");
	}

	public void testMandatoryOverridesDefault_NoTypeParameter3() {
		assertMandatory(ScenarioTypeSubOf_NoTypeParameter3.class, "class",
			"Property is not mandatory, when the super interface ignores the type parameter.");
	}

	public void testMandatoryOverridesDefault_NoTypeParameter4() {
		assertMandatory(ScenarioTypeSubOf_NoTypeParameter4.class, "class",
			"Property is not mandatory, when the super interface ignores the type parameter.");
	}

	public void testImplicitDefault() {
		assertMandatory(ScenarioTypeImplicitDefault2.class, "class",
			"Property is not mandatory, when interface refines the type parameter.");
	}

	public void testRecursionNoValue() throws ConfigurationException {
		ScenarioTypeRecursiveConfig config = (ScenarioTypeRecursiveConfig) readWithScenario(
			"<ScenarioTypeRecursiveConfig />");

		assertNotNull(config);
		assertNull(config.getTestProperty());
	}

	public void testRecursionWithValue() throws ConfigurationException {
		ScenarioTypeRecursiveConfig config = (ScenarioTypeRecursiveConfig) readWithScenario(
			"<ScenarioTypeRecursiveConfig><test-property><test-property><test-property>"
				+ "</test-property></test-property></test-property></ScenarioTypeRecursiveConfig>");

		assertNotNull(config);
		assertNotNull(config.getTestProperty());
		assertNotNull(config.getTestProperty().getTestProperty());
		assertNotNull(config.getTestProperty().getTestProperty().getTestProperty());
		assertNull(config.getTestProperty().getTestProperty().getTestProperty().getTestProperty());
	}

	public void testRecursionIndirectNoValue() throws ConfigurationException {
		ScenarioTypeIndirectRecursiveConfigA config = (ScenarioTypeIndirectRecursiveConfigA) readWithScenario(
			"<ScenarioTypeIndirectRecursiveConfigA />");

		assertNotNull(config);
		assertNull(config.getTestProperty());
	}

	public void testRecursionIndirectWithValue() throws ConfigurationException {
		ScenarioTypeIndirectRecursiveConfigA config = (ScenarioTypeIndirectRecursiveConfigA) readWithScenario(
			"<ScenarioTypeIndirectRecursiveConfigA><test-property><test-property><test-property>"
				+ "</test-property></test-property></test-property></ScenarioTypeIndirectRecursiveConfigA>");

		assertNotNull(config);
		assertNotNull(config.getTestProperty());
		assertNotNull(config.getTestProperty().getTestProperty());
		assertNotNull(config.getTestProperty().getTestProperty().getTestProperty());
		assertNull(config.getTestProperty().getTestProperty().getTestProperty().getTestProperty());
	}

	public void testInstanceFormatDefault() throws ConfigurationException {
		ScenarioTypeInstanceFormatDefault config = (ScenarioTypeInstanceFormatDefault) readWithScenario(
			"<ScenarioTypeInstanceFormatDefault />");

		assertNotNull(config);
		assertNotNull(config.getTestProperty());
	}

	public void testMandatory() throws ConfigurationException {
		MandatoryTest value = readMandatoryTest("<m string-mandatory='hello' int-mandatory='42' />");
		assertEquals("hello", value.getStringMandatory());
		assertEquals(42, value.getIntMandatory());
	}

	public void testMandatoryEmpty() throws ConfigurationException {
		MandatoryTest value = readMandatoryTest("<m string-mandatory='' int-mandatory='0' />");
		assertEquals("", value.getStringMandatory());
		assertEquals(0, value.getIntMandatory());
	}

	public void testMandatoryFail() {
		initFailureTest();
		try {
			readMandatoryTest("<m int-mandatory='42' />");
		} catch (ConfigurationException ex) {
			// Expected.
			return;
		} catch (ExpectedFailure ex) {
			// Expected.
			return;
		}
		fail("Missing mandatory property not detected.");
	}

	public void testMandatoryOverloading() {
		initFailureTest();
		String mandatorySet = "<m string-mandatory='hello' int-mandatory='42' />";
		String notEachAttributeRedefined = "<m int-mandatory='43' />";
		MandatoryTest item = null;
		try {
			item = readMandatoryTest(mandatorySet, notEachAttributeRedefined);
		} catch (ConfigurationException ex) {
			throw BasicTestCase.fail("Ticket #12554: " + ex.getMessage(), ex);
		}
		assertSame(43, item.getIntMandatory());
		assertEquals("hello", item.getStringMandatory());
	}

	public void testRedeclareMandatory() {
		initFailureTest();
		try {
			readMandatoryTest("<m3 string-mandatory='hello' />");
		} catch (ConfigurationException ex) {
			// Expected.
			return;
		} catch (ExpectedFailure ex) {
			// Expected.
			return;
		}
		fail("Property redeclared mandatory property.");
	}

	public void testInheritMandatory() {
		initFailureTest();
		try {
			readMandatoryTest("<m2 int-mandatory='42' />");
		} catch (ConfigurationException ex) {
			// Expected.
			return;
		} catch (ExpectedFailure ex) {
			// Expected.
			return;
		}
		fail("Missing mandatory property at sub property not detected.");
	}

	public void testNotMandatoryAsDefaultValue() throws ConfigurationException {
		MandatoryTest mandatoryTest = readMandatoryTest("<m2 string-mandatory='hello' />");
		assertInstanceof(mandatoryTest, MandatoryTest2.class);
		assertEquals("hello", mandatoryTest.getStringMandatory());
		assertEquals(MandatoryTest2.DEFAULT_INT_MANDATORY, mandatoryTest.getIntMandatory());
	}

	public void testDerivedUsesMandatory() {
		DerivedUsesMandatory config = TypedConfiguration.newConfigItem(DerivedUsesMandatory.class);
		PropertyDescriptor mandatoryProperty = getProperty(DerivedUsesMandatory.class, DerivedUsesMandatory.MANDATORY);
		PropertyDescriptor derivedProperty = getProperty(DerivedUsesMandatory.class, DerivedUsesMandatory.DERIVED);
		/* The raw derived value is null, but the implicit defaults still apply. The derived value
		 * is therefore 0. */
		assertEquals(0, config.value(derivedProperty));
		int expectedValue = 1;
		config.setMandatory(expectedValue);
		assertEquals(expectedValue, config.getDerived());
		config.reset(mandatoryProperty);
		assertEquals(0, config.value(derivedProperty));
	}

	/**
	 * Bug: If a property of a non-"plain" kind is mandatory but not set, the ConfigWriter skips all
	 * other non-plain properties in that ConfigItem that he did not already write out.
	 */
	public void testRegression20851_1() throws XMLStreamException {
		Regression20851_1 item = newConfigItem(Regression20851_1.class);
		ConfigurationItem innerItem = newConfigItem(ConfigurationItem.class);
		item.setA(innerItem);
		item.setC(innerItem);
		String xml = toXML(item);
		assertTrue(xml.contains("<a"));
		assertFalse(xml.contains("<b"));
		assertTrue(xml.contains("<c"));
	}

	/**
	 * Bug: The ConfigWriter writes non-mandatory properties of a non-"plain" kind, even if they are
	 * not set.
	 */
	public void testRegression20851_2() throws XMLStreamException {
		Regression20851_2 item = newConfigItem(Regression20851_2.class);
		String xml = toXML(item);
		assertFalse(xml.contains("<a"));
	}

	public void testMandatoryPropertyWithNullDefaultFormat() throws ConfigurationException {
		String[] sources = { "<m foo='bar'/>" };
		ConfigurationItem item = read(Collections.singletonMap("m",
			TypedConfiguration.getConfigurationDescriptor(MandatoryWithNullDefaultFormat.class)), sources);
		assertNotNull(item);

		MandatoryWithNullDefaultFormat newItem = TypedConfiguration.newConfigItem(MandatoryWithNullDefaultFormat.class);
		assertNotNull(newItem);
		assertFalse(newItem.valueSet(newItem.descriptor().getProperty(MandatoryWithNullDefaultFormat.FOO)));
	}

	// Utils

	private void assertMandatory(Class<? extends ConfigurationItem> configInterface, String propertyName,
			String failureMessage) {
		ConfigurationDescriptor configDescriptor = TypedConfiguration.getConfigurationDescriptor(configInterface);
		PropertyDescriptor propertyDescriptor = configDescriptor.getProperty(propertyName);
		assertTrue(failureMessage, propertyDescriptor.isMandatory());
	}

	private MandatoryTest readMandatoryTest(String... sources) throws ConfigurationException {
		Map<String, ConfigurationDescriptor> configs = new HashMap<>();
		configs.put("m", TypedConfiguration.getConfigurationDescriptor(MandatoryTest.class));
		configs.put("m2", TypedConfiguration.getConfigurationDescriptor(MandatoryTest2.class));
		configs.put("m3", TypedConfiguration.getConfigurationDescriptor(MandatoryTest3.class));
		return (MandatoryTest) read(configs, sources);
	}

	private ConfigurationItem readWithScenario(String... sources) throws ConfigurationException {
		return read(ScenarioMandatory.class, sources);
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return createDescriptorMap(Regression20851_1.class, Regression20851_2.class,
			ScenarioTypeMandatoryProperty.class);
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestMandatoryAnnotation.class));
	}

}
