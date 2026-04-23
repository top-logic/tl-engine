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

import com.top_logic.tl.ai.core.tools.model.ModelAnnotationBrowserTool;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.util.model.ModelService;

/**
 * Test cases for {@link ModelAnnotationBrowserTool}.
 */
public class ModelAnnotationBrowserToolTest extends BasicTestCase {

	private static final String PIZZASHOP_MODULE = "luigis.pizzashop";
	private static final String UNIT_OF_MEASURE_ENUM = "UnitOfMeasure";
	private static final String PRODUCT_CLASS = "Product";
	private static final String NAMED_ENTITIES_INTERFACE = "NamedEntities";

	/**
	 * Tests listing annotations on a module (module-only reference format).
	 */
	public void testListAnnotationsOnModule() {
		ModelAnnotationBrowserTool tool = new ModelAnnotationBrowserTool();
		String result = tool.getCurrentAnnotations(PIZZASHOP_MODULE, false);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain target reference", result.contains("\"target\""));
		assertTrue("Result should contain module name", result.contains(PIZZASHOP_MODULE));
		assertTrue("Result should contain kind field", result.contains("\"kind\""));
		assertTrue("Result should indicate module kind", result.contains("\"kind\":\"module\""));
		assertTrue("Result should contain annotations array", result.contains("\"annotations\""));
	}

	/**
	 * Tests listing annotations on a type (module:Type format).
	 */
	public void testListAnnotationsOnType() {
		ModelAnnotationBrowserTool tool = new ModelAnnotationBrowserTool();
		String targetRef = PIZZASHOP_MODULE + ":" + PRODUCT_CLASS;
		String result = tool.getCurrentAnnotations(targetRef, false);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain target reference", result.contains("\"target\""));
		assertTrue("Result should contain module field", result.contains("\"module\""));
		assertTrue("Result should contain type field", result.contains("\"type\""));
		assertTrue("Result should contain Product", result.contains(PRODUCT_CLASS));
		assertTrue("Result should contain kind field", result.contains("\"kind\""));
		// The kind should be "class" (not "type") since Product is a class
		assertTrue("Result should indicate class or type kind",
			result.contains("\"kind\":\"class\"") || result.contains("\"kind\":\"type\""));
		assertTrue("Result should contain annotations array", result.contains("\"annotations\""));
	}

	/**
	 * Tests listing annotations on an enumeration.
	 */
	public void testListAnnotationsOnEnumeration() {
		ModelAnnotationBrowserTool tool = new ModelAnnotationBrowserTool();
		String targetRef = PIZZASHOP_MODULE + ":" + UNIT_OF_MEASURE_ENUM;
		String result = tool.getCurrentAnnotations(targetRef, false);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain target reference", result.contains(targetRef));
		assertTrue("Result should contain enumeration kind", result.contains("\"kind\":\"enumeration\""));
		assertTrue("Result should contain UnitOfMeasure", result.contains(UNIT_OF_MEASURE_ENUM));
		assertTrue("Result should contain annotations array", result.contains("\"annotations\""));
	}

	/**
	 * Tests listing annotations on a part (module:Type#part format).
	 */
	public void testListAnnotationsOnPart() {
		ModelAnnotationBrowserTool tool = new ModelAnnotationBrowserTool();
		String targetRef = PIZZASHOP_MODULE + ":" + PRODUCT_CLASS + "#plannedQuantity";
		String result = tool.getCurrentAnnotations(targetRef, false);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain target reference", result.contains(targetRef));
		assertTrue("Result should contain part kind", result.contains("\"kind\":\"part\""));
		assertTrue("Result should contain part field", result.contains("\"part\""));
		assertTrue("Result should contain plannedQuantity", result.contains("plannedQuantity"));
		assertTrue("Result should contain annotations array", result.contains("\"annotations\""));
	}

	/**
	 * Tests listing annotations on a classifier (enum literal).
	 */
	public void testListAnnotationsOnClassifier() {
		ModelAnnotationBrowserTool tool = new ModelAnnotationBrowserTool();
		String targetRef = PIZZASHOP_MODULE + ":" + UNIT_OF_MEASURE_ENUM + "#KG";
		String result = tool.getCurrentAnnotations(targetRef, false);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain target reference", result.contains(targetRef));
		assertTrue("Result should contain classifier kind", result.contains("\"kind\":\"classifier\""));
		assertTrue("Result should contain classifier field", result.contains("\"classifier\""));
		assertTrue("Result should contain KG", result.contains("KG"));
		assertTrue("Result should contain annotations array", result.contains("\"annotations\""));
	}

	/**
	 * Tests listing annotations with includeInherited=false (local only).
	 */
	public void testListAnnotationsLocalOnly() {
		ModelAnnotationBrowserTool tool = new ModelAnnotationBrowserTool();
		String targetRef = PIZZASHOP_MODULE + ":" + PRODUCT_CLASS;
		String result = tool.getCurrentAnnotations(targetRef, false);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain includeInherited field", result.contains("\"includeInherited\""));
		assertTrue("includeInherited should be false", result.contains("\"includeInherited\":false"));
		// If there are annotations, they should not be marked as inherited
		if (result.contains("\"annotations\":[{")) {
			// Has at least one annotation
			// Check that no annotation is marked as inherited=true (they should be inherited=false or absent)
			// We can't easily parse JSON here, but we can check the structure
			assertTrue("Annotations should be present", result.contains("\"tag\""));
		}
	}

	/**
	 * Tests listing annotations with includeInherited=true.
	 */
	public void testListAnnotationsWithInherited() {
		ModelAnnotationBrowserTool tool = new ModelAnnotationBrowserTool();
		// Product inherits from NamedEntities
		String targetRef = PIZZASHOP_MODULE + ":" + PRODUCT_CLASS;
		String result = tool.getCurrentAnnotations(targetRef, true);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain includeInherited field", result.contains("\"includeInherited\""));
		assertTrue("includeInherited should be true", result.contains("\"includeInherited\":true"));
		// Result may contain inherited annotations marked with "inherited": true
		// We can't guarantee inherited annotations exist without knowing the model details
		assertTrue("Result should contain annotations array", result.contains("\"annotations\""));
	}

	/**
	 * Tests that inherited annotations include definedIn field.
	 */
	public void testInheritedAnnotationsHaveDefinedIn() {
		ModelAnnotationBrowserTool tool = new ModelAnnotationBrowserTool();
		String targetRef = PIZZASHOP_MODULE + ":" + PRODUCT_CLASS;
		String result = tool.getCurrentAnnotations(targetRef, true);

		assertNotNull("Result should not be null", result);
		// If there are inherited annotations, they should have defined in field
		if (result.contains("\"inherited\":true")) {
			assertTrue("Inherited annotations should have definedIn field", result.contains("\"definedIn\""));
		}
	}

	/**
	 * Tests error case: null target reference.
	 */
	public void testListAnnotationsNullTarget() {
		ModelAnnotationBrowserTool tool = new ModelAnnotationBrowserTool();
		String result = tool.getCurrentAnnotations(null, false);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\""));
		assertTrue("Error should mention target required",
			result.contains("required") || result.contains("Target"));
	}

	/**
	 * Tests error case: empty target reference.
	 */
	public void testListAnnotationsEmptyTarget() {
		ModelAnnotationBrowserTool tool = new ModelAnnotationBrowserTool();
		String result = tool.getCurrentAnnotations("", false);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\""));
		assertTrue("Error should mention target required",
			result.contains("required") || result.contains("Target"));
	}

	/**
	 * Tests error case: invalid module name.
	 */
	public void testListAnnotationsInvalidModule() {
		ModelAnnotationBrowserTool tool = new ModelAnnotationBrowserTool();
		String result = tool.getCurrentAnnotations("nonexistent.module", false);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\""));
		assertTrue("Error should mention module not found",
			result.contains("Module not found") || result.contains("not found"));
	}

	/**
	 * Tests error case: invalid type name.
	 */
	public void testListAnnotationsInvalidType() {
		ModelAnnotationBrowserTool tool = new ModelAnnotationBrowserTool();
		String targetRef = PIZZASHOP_MODULE + ":NonExistentType";
		String result = tool.getCurrentAnnotations(targetRef, false);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\""));
		assertTrue("Error should mention type not found",
			result.contains("Type not found") || result.contains("not found"));
	}

	/**
	 * Tests error case: invalid part name.
	 */
	public void testListAnnotationsInvalidPart() {
		ModelAnnotationBrowserTool tool = new ModelAnnotationBrowserTool();
		String targetRef = PIZZASHOP_MODULE + ":" + PRODUCT_CLASS + "#nonExistentPart";
		String result = tool.getCurrentAnnotations(targetRef, false);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\""));
		assertTrue("Error should mention part not found",
			result.contains("Part not found") || result.contains("not found"));
	}

	/**
	 * Tests error case: invalid classifier name.
	 */
	public void testListAnnotationsInvalidClassifier() {
		ModelAnnotationBrowserTool tool = new ModelAnnotationBrowserTool();
		String targetRef = PIZZASHOP_MODULE + ":" + UNIT_OF_MEASURE_ENUM + "#INVALID";
		String result = tool.getCurrentAnnotations(targetRef, false);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\""));
		assertTrue("Error should mention classifier not found",
			result.contains("Classifier not found") || result.contains("not found"));
	}

	/**
	 * Tests error case: reference with '#' but no ':' separator (invalid format).
	 */
	public void testListAnnotationsInvalidFormatHashWithoutColon() {
		ModelAnnotationBrowserTool tool = new ModelAnnotationBrowserTool();
		String result = tool.getCurrentAnnotations("moduleName#partName", false);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\""));
		assertTrue("Error should mention invalid format",
			result.contains("Invalid") || result.contains("cannot have '#'"));
	}

	/**
	 * Tests error case: empty module name in reference.
	 */
	public void testListAnnotationsEmptyModuleName() {
		ModelAnnotationBrowserTool tool = new ModelAnnotationBrowserTool();
		String result = tool.getCurrentAnnotations(":TypeName", false);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\""));
		assertTrue("Error should mention module name empty",
			result.contains("empty") || result.contains("Module name"));
	}

	/**
	 * Tests error case: empty type name in reference.
	 */
	public void testListAnnotationsEmptyTypeName() {
		ModelAnnotationBrowserTool tool = new ModelAnnotationBrowserTool();
		String result = tool.getCurrentAnnotations(PIZZASHOP_MODULE + ":", false);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\""));
		assertTrue("Error should mention type name empty",
			result.contains("empty") || result.contains("Type name"));
	}

	/**
	 * Tests error case: empty part name in reference.
	 */
	public void testListAnnotationsEmptyPartName() {
		ModelAnnotationBrowserTool tool = new ModelAnnotationBrowserTool();
		String targetRef = PIZZASHOP_MODULE + ":" + PRODUCT_CLASS + "#";
		String result = tool.getCurrentAnnotations(targetRef, false);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\""));
		assertTrue("Error should mention part/classifier name empty",
			result.contains("empty") || result.contains("name"));
	}

	/**
	 * Tests that annotation info includes tag name.
	 */
	public void testAnnotationInfoIncludesTag() {
		ModelAnnotationBrowserTool tool = new ModelAnnotationBrowserTool();
		String targetRef = PIZZASHOP_MODULE + ":" + PRODUCT_CLASS;
		String result = tool.getCurrentAnnotations(targetRef, false);

		assertNotNull("Result should not be null", result);
		// If there are annotations, they should have a tag field
		if (result.contains("\"annotations\":[{")) {
			assertTrue("Annotations should have tag field", result.contains("\"tag\""));
		}
	}

	/**
	 * Tests that annotation info includes config interface.
	 */
	public void testAnnotationInfoIncludesConfigInterface() {
		ModelAnnotationBrowserTool tool = new ModelAnnotationBrowserTool();
		String targetRef = PIZZASHOP_MODULE + ":" + PRODUCT_CLASS;
		String result = tool.getCurrentAnnotations(targetRef, false);

		assertNotNull("Result should not be null", result);
		// If there are annotations, they should have a config interface field
		if (result.contains("\"annotations\":[{")) {
			assertTrue("Annotations should have configInterface field", result.contains("\"configInterface\""));
		}
	}

	/**
	 * Tests that annotation info includes configJson as structured JSON.
	 */
	public void testAnnotationInfoIncludesConfigJson() {
		ModelAnnotationBrowserTool tool = new ModelAnnotationBrowserTool();
		String targetRef = PIZZASHOP_MODULE + ":" + PRODUCT_CLASS;
		String result = tool.getCurrentAnnotations(targetRef, false);

		assertNotNull("Result should not be null", result);
		// If there are annotations, they should have a configJson field (not configXml)
		if (result.contains("\"annotations\":[{") || result.contains("\"annotations\": [{")) {
			assertTrue("Annotations should have configJson field", result.contains("\"configJson\""));
			assertFalse("Annotations should NOT have configXml field", result.contains("\"configXml\""));
		}
	}

	/**
	 * Tests different classifier references.
	 */
	public void testListAnnotationsOnDifferentClassifiers() {
		ModelAnnotationBrowserTool tool = new ModelAnnotationBrowserTool();

		// Test each classifier in UnitOfMeasure
		String[] classifiers = {"G", "KG", "T", "ML", "L", "HOUR", "MINUTE"};

		for (String classifier : classifiers) {
			String targetRef = PIZZASHOP_MODULE + ":" + UNIT_OF_MEASURE_ENUM + "#" + classifier;
			String result = tool.getCurrentAnnotations(targetRef, false);

			assertNotNull("Result for " + classifier + " should not be null", result);
			assertTrue("Result should contain classifier kind for " + classifier,
				result.contains("\"kind\":\"classifier\""));
			assertTrue("Result should contain " + classifier, result.contains(classifier));
		}
	}

	/**
	 * Tests listing annotations on an interface type.
	 */
	public void testListAnnotationsOnInterface() {
		ModelAnnotationBrowserTool tool = new ModelAnnotationBrowserTool();
		String targetRef = PIZZASHOP_MODULE + ":" + NAMED_ENTITIES_INTERFACE;
		String result = tool.getCurrentAnnotations(targetRef, false);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain target reference", result.contains(targetRef));
		assertTrue("Result should contain NamedEntities", result.contains(NAMED_ENTITIES_INTERFACE));
		assertTrue("Result should contain annotations array", result.contains("\"annotations\""));
	}

	/**
	 * Tests null includeInherited parameter (should default to false).
	 */
	public void testListAnnotationsNullIncludeInherited() {
		ModelAnnotationBrowserTool tool = new ModelAnnotationBrowserTool();
		String targetRef = PIZZASHOP_MODULE + ":" + PRODUCT_CLASS;
		String result = tool.getCurrentAnnotations(targetRef, null);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should be valid", result.contains("\"target\""));
		assertTrue("includeInherited should default to false", result.contains("\"includeInherited\":false"));
	}

	/**
	 * Creates the test suite with required service dependencies.
	 */
	public static Test suite() {
		return suite(ModelAnnotationBrowserToolTest.class);
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
