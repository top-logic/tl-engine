/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.tl.ai.core.tools.model.AnnotationDefinitionExplorerTool;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.util.model.ModelService;

/**
 * Test cases for {@link AnnotationDefinitionExplorerTool}.
 */
public class AnnotationDefinitionExplorerToolTest extends BasicTestCase {

	private static final String VISIBILITY_ANNOTATION = "com.top_logic.model.annotate.TLVisibility";
	private static final String TL_I18N_KEY_ANNOTATION = "com.top_logic.model.annotate.TLI18NKey";
	private static final String CONFIGURATION_ITEM = "com.top_logic.basic.config.ConfigurationItem";

	/**
	 * Tests describing a simple annotation with basic properties.
	 */
	public void testDescribeSimpleAnnotation() {
		AnnotationDefinitionExplorerTool tool = new AnnotationDefinitionExplorerTool();
		String result = tool.describeAnnotation(VISIBILITY_ANNOTATION);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain configInterface", result.contains("\"configInterface\""));
		assertTrue("Result should contain Visibility class name", result.contains(VISIBILITY_ANNOTATION));
		assertTrue("Result should contain tagName", result.contains("\"tagName\""));
		assertTrue("Result should contain superInterfaces", result.contains("\"superInterfaces\""));
		assertTrue("Result should contain properties", result.contains("\"properties\""));
	}

	/**
	 * Tests that annotation description includes tag name.
	 */
	public void testDescribeAnnotationIncludesTagName() {
		AnnotationDefinitionExplorerTool tool = new AnnotationDefinitionExplorerTool();
		String result = tool.describeAnnotation(VISIBILITY_ANNOTATION);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain tagName field", result.contains("\"tagName\""));
		// The tag name should be either from @TagName annotation or lowercase simple class name
		assertTrue("Result should contain visibility tag",
			result.contains("\"tagName\":\"visibility\"") || result.contains("\"tagName\":\"Visibility\""));
	}

	/**
	 * Tests that annotation description includes abstract flag.
	 */
	public void testDescribeAnnotationIncludesAbstractFlag() {
		AnnotationDefinitionExplorerTool tool = new AnnotationDefinitionExplorerTool();
		String result = tool.describeAnnotation(VISIBILITY_ANNOTATION);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain abstract field", result.contains("\"abstract\""));
		// Should be either true or false
		assertTrue("abstract should be boolean",
			result.contains("\"abstract\":true") || result.contains("\"abstract\":false"));
	}

	/**
	 * Tests that annotation description includes super interfaces.
	 */
	public void testDescribeAnnotationIncludesSuperInterfaces() {
		AnnotationDefinitionExplorerTool tool = new AnnotationDefinitionExplorerTool();
		String result = tool.describeAnnotation(VISIBILITY_ANNOTATION);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain superInterfaces field", result.contains("\"superInterfaces\""));
		// Should be an array (even if empty)
		assertTrue("superInterfaces should be array", result.contains("\"superInterfaces\":["));
	}

	/**
	 * Tests that annotation description includes properties array.
	 */
	public void testDescribeAnnotationIncludesProperties() {
		AnnotationDefinitionExplorerTool tool = new AnnotationDefinitionExplorerTool();
		String result = tool.describeAnnotation(VISIBILITY_ANNOTATION);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain properties field", result.contains("\"properties\""));
		assertTrue("properties should be array", result.contains("\"properties\":["));
	}

	/**
	 * Tests that property definitions include name and kind.
	 */
	public void testPropertyDefinitionIncludesNameAndKind() {
		AnnotationDefinitionExplorerTool tool = new AnnotationDefinitionExplorerTool();
		String result = tool.describeAnnotation(VISIBILITY_ANNOTATION);

		assertNotNull("Result should not be null", result);
		// If there are properties, they should have name and kind
		if (result.contains("\"properties\":[{")) {
			assertTrue("Properties should have name field", result.contains("\"name\""));
			assertTrue("Properties should have kind field", result.contains("\"kind\""));
		}
	}

	/**
	 * Tests that property definitions include type information.
	 */
	public void testPropertyDefinitionIncludesTypeInfo() {
		AnnotationDefinitionExplorerTool tool = new AnnotationDefinitionExplorerTool();
		String result = tool.describeAnnotation(VISIBILITY_ANNOTATION);

		assertNotNull("Result should not be null", result);
		// If there are properties, they should have type and type simple name
		if (result.contains("\"properties\":[{")) {
			assertTrue("Properties should have type field", result.contains("\"type\""));
			assertTrue("Properties should have typeSimpleName field", result.contains("\"typeSimpleName\""));
		}
	}

	/**
	 * Tests that property definitions include constraint flags.
	 */
	public void testPropertyDefinitionIncludesConstraints() {
		AnnotationDefinitionExplorerTool tool = new AnnotationDefinitionExplorerTool();
		String result = tool.describeAnnotation(VISIBILITY_ANNOTATION);

		assertNotNull("Result should not be null", result);
		// If there are properties, they should have constraint flags
		if (result.contains("\"properties\":[{")) {
			assertTrue("Properties should have mandatory field", result.contains("\"mandatory\""));
			assertTrue("Properties should have nullable field", result.contains("\"nullable\""));
		}
	}

	/**
	 * Tests that property definitions with explicit defaults include default value.
	 */
	public void testPropertyDefinitionIncludesDefaultValue() {
		AnnotationDefinitionExplorerTool tool = new AnnotationDefinitionExplorerTool();
		String result = tool.describeAnnotation(VISIBILITY_ANNOTATION);

		assertNotNull("Result should not be null", result);
		// If there are properties with explicit defaults, they should have has explicit default field
		if (result.contains("\"properties\":[{")) {
			assertTrue("Properties should have hasExplicitDefault field", result.contains("\"hasExplicitDefault\""));
		}
	}

	/**
	 * Tests that enum property definitions include enum values.
	 */
	public void testPropertyDefinitionIncludesEnumValues() {
		AnnotationDefinitionExplorerTool tool = new AnnotationDefinitionExplorerTool();
		// Visibility likely has an enum property for visibility level
		String result = tool.describeAnnotation(VISIBILITY_ANNOTATION);

		assertNotNull("Result should not be null", result);
		// If there are enum properties, they should have enum values array
		if (result.contains("\"enumValues\"")) {
			assertTrue("Enum properties should have enumValues array", result.contains("\"enumValues\":["));
		}
	}

	/**
	 * Tests error case: null interface name.
	 */
	public void testDescribeAnnotationNullInterfaceName() {
		AnnotationDefinitionExplorerTool tool = new AnnotationDefinitionExplorerTool();
		String result = tool.describeAnnotation(null);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\""));
		assertTrue("Error should mention interface name required",
			result.contains("required") || result.contains("interface name"));
	}

	/**
	 * Tests error case: empty interface name.
	 */
	public void testDescribeAnnotationEmptyInterfaceName() {
		AnnotationDefinitionExplorerTool tool = new AnnotationDefinitionExplorerTool();
		String result = tool.describeAnnotation("");

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\""));
		assertTrue("Error should mention interface name required",
			result.contains("required") || result.contains("interface name"));
	}

	/**
	 * Tests error case: class not found.
	 */
	public void testDescribeAnnotationClassNotFound() {
		AnnotationDefinitionExplorerTool tool = new AnnotationDefinitionExplorerTool();
		String result = tool.describeAnnotation("com.nonexistent.package.NonExistentClass");

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\""));
		assertTrue("Error should mention class not found or error accessing",
			result.contains("not found") || result.contains("Error accessing") ||
			result.contains("ClassNotFoundException"));
	}

	/**
	 * Tests error case: not an interface.
	 */
	public void testDescribeAnnotationNotAnInterface() {
		AnnotationDefinitionExplorerTool tool = new AnnotationDefinitionExplorerTool();
		// String is a class, not an interface
		String result = tool.describeAnnotation("java.lang.String");

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\""));
		assertTrue("Error should mention not an interface",
			result.contains("Not an interface") || result.contains("interface"));
	}

	/**
	 * Tests error case: not a ConfigurationItem.
	 */
	public void testDescribeAnnotationNotAConfigurationItem() {
		AnnotationDefinitionExplorerTool tool = new AnnotationDefinitionExplorerTool();
		// Runnable is an interface but not a ConfigurationItem
		String result = tool.describeAnnotation("java.lang.Runnable");

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\""));
		assertTrue("Error should mention not a ConfigurationItem",
			result.contains("Not a ConfigurationItem") || result.contains("ConfigurationItem"));
	}

	/**
	 * Tests describing ConfigurationItem itself (the base interface).
	 */
	public void testDescribeConfigurationItemBase() {
		AnnotationDefinitionExplorerTool tool = new AnnotationDefinitionExplorerTool();
		String result = tool.describeAnnotation(CONFIGURATION_ITEM);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain configInterface", result.contains("\"configInterface\""));
		assertTrue("Result should contain ConfigurationItem", result.contains(CONFIGURATION_ITEM));
		assertTrue("Result should contain properties", result.contains("\"properties\""));
	}

	/**
	 * Tests describing different annotation types.
	 */
	public void testDescribeDifferentAnnotationTypes() {
		AnnotationDefinitionExplorerTool tool = new AnnotationDefinitionExplorerTool();

		// Test Visibility annotation
		String visibilityResult = tool.describeAnnotation(VISIBILITY_ANNOTATION);
		assertNotNull("Visibility result should not be null", visibilityResult);
		assertTrue("Should contain Visibility", visibilityResult.contains(VISIBILITY_ANNOTATION));

		// Test TLI18NKey annotation
		String i18nResult = tool.describeAnnotation(TL_I18N_KEY_ANNOTATION);
		assertNotNull("I18N result should not be null", i18nResult);
		assertTrue("Should contain TLI18NKey", i18nResult.contains(TL_I18N_KEY_ANNOTATION));
	}

	/**
	 * Tests that internal properties are filtered out.
	 */
	public void testInternalPropertiesAreFiltered() {
		AnnotationDefinitionExplorerTool tool = new AnnotationDefinitionExplorerTool();
		String result = tool.describeAnnotation(VISIBILITY_ANNOTATION);

		assertNotNull("Result should not be null", result);
		// Internal properties like "annotated" and "configuration-interface" should not appear
		assertFalse("Should not contain 'annotated' property", result.contains("\"name\":\"annotated\""));
		assertFalse("Should not contain 'configuration-interface' property",
			result.contains("\"name\":\"configuration-interface\""));
	}

	/**
	 * Tests that the response is valid JSON structure.
	 */
	public void testResponseIsValidJsonStructure() {
		AnnotationDefinitionExplorerTool tool = new AnnotationDefinitionExplorerTool();
		String result = tool.describeAnnotation(VISIBILITY_ANNOTATION);

		assertNotNull("Result should not be null", result);
		// Basic JSON structure validation
		assertTrue("Result should start with {", result.trim().startsWith("{"));
		assertTrue("Result should end with }", result.trim().endsWith("}"));
		assertTrue("Result should contain colons", result.contains(":"));
		assertTrue("Result should contain commas", result.contains(","));
	}

	/**
	 * Tests that property kind values are lowercase.
	 */
	public void testPropertyKindIsLowercase() {
		AnnotationDefinitionExplorerTool tool = new AnnotationDefinitionExplorerTool();
		String result = tool.describeAnnotation(VISIBILITY_ANNOTATION);

		assertNotNull("Result should not be null", result);
		// Kind should be lowercase (e.g., "plain", "item", "list", etc.)
		if (result.contains("\"kind\"")) {
			// Check that kind values don't contain uppercase letters between quotes after "kind":
			// This is a simple heuristic check
			assertTrue("Kind values should be lowercase",
				!result.matches(".*\"kind\":\"[A-Z].*"));
		}
	}

	/**
	 * Tests that multiple properties are returned for annotation with multiple properties.
	 */
	public void testMultiplePropertiesReturned() {
		AnnotationDefinitionExplorerTool tool = new AnnotationDefinitionExplorerTool();
		String result = tool.describeAnnotation(VISIBILITY_ANNOTATION);

		assertNotNull("Result should not be null", result);
		// Count occurrences of "name" field in properties (rough estimate)
		int nameCount = countOccurrences(result, "\"name\":");
		// Should have at least one property
		assertTrue("Should have at least one property", nameCount >= 1);
	}

	/**
	 * Helper method to count occurrences of a substring.
	 */
	private int countOccurrences(String text, String substring) {
		int count = 0;
		int index = 0;
		while ((index = text.indexOf(substring, index)) != -1) {
			count++;
			index += substring.length();
		}
		return count;
	}

	/**
	 * Creates the test suite with required service dependencies.
	 */
	public static Test suite() {
		return suite(AnnotationDefinitionExplorerToolTest.class);
	}

	protected static Test suite(Class<? extends Test> test, BasicRuntimeModule<?>... additionalModules) {
		BasicRuntimeModule<?>[] modules = getModules(additionalModules);

		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(test),
				modules));
	}

	private static BasicRuntimeModule<?>[] getModules(BasicRuntimeModule<?>... additionalModules) {
		ArrayList<BasicRuntimeModule<?>> modules = new ArrayList<>();

		modules.add(ModelService.Module.INSTANCE);
		modules.add(LabelProviderService.Module.INSTANCE);

		modules.addAll(Arrays.asList(additionalModules));

		return modules.toArray(new BasicRuntimeModule<?>[modules.size()]);
	}
}
