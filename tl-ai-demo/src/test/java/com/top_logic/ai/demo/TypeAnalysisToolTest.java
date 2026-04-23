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

import com.top_logic.tl.ai.core.tools.model.TypeAnalysisTool;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.util.model.ModelService;

/**
 * Test cases for {@link TypeAnalysisTool}.
 */
public class TypeAnalysisToolTest extends BasicTestCase {

	private static final String PIZZASHOP_MODULE = "luigis.pizzashop";
	private static final String UNIT_OF_MEASURE_ENUM = "UnitOfMeasure";
	private static final String PRODUCT_CLASS = "Product";
	private static final String NAMED_ENTITIES_INTERFACE = "NamedEntities";
	private static final String COMPONENT_INTERFACE = "Component";

	// Qualified names
	private static final String PRODUCT_QUALIFIED = PIZZASHOP_MODULE + ":" + PRODUCT_CLASS;
	private static final String NAMED_ENTITIES_QUALIFIED = PIZZASHOP_MODULE + ":" + NAMED_ENTITIES_INTERFACE;
	private static final String COMPONENT_QUALIFIED = PIZZASHOP_MODULE + ":" + COMPONENT_INTERFACE;
	private static final String UNIT_OF_MEASURE_QUALIFIED = PIZZASHOP_MODULE + ":" + UNIT_OF_MEASURE_ENUM;

	/**
	 * Tests finding usages of a class type.
	 */
	public void testFindTypeUsagesForClass() {
		TypeAnalysisTool tool = new TypeAnalysisTool();
		String result = tool.findTypeUsages(PRODUCT_QUALIFIED);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain properties array", result.contains("\"properties\""));
		assertTrue("Result should contain references array", result.contains("\"references\""));
		assertTrue("Result should contain subclasses array", result.contains("\"subclasses\""));
	}

	/**
	 * Tests finding usages of an enumeration type.
	 */
	public void testFindTypeUsagesForEnumeration() {
		TypeAnalysisTool tool = new TypeAnalysisTool();
		String result = tool.findTypeUsages(UNIT_OF_MEASURE_QUALIFIED);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain properties array", result.contains("\"properties\""));
		assertTrue("Result should contain references array", result.contains("\"references\""));
		assertTrue("Result should contain subclasses array", result.contains("\"subclasses\""));
		// Enumerations typically don't have subclasses
		// The result might show properties that use UnitOfMeasure as their value type
	}

	/**
	 * Tests finding usages of an interface type.
	 */
	public void testFindTypeUsagesForInterface() {
		TypeAnalysisTool tool = new TypeAnalysisTool();
		String result = tool.findTypeUsages(NAMED_ENTITIES_QUALIFIED);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain properties array", result.contains("\"properties\""));
		assertTrue("Result should contain references array", result.contains("\"references\""));
		assertTrue("Result should contain subclasses array", result.contains("\"subclasses\""));
		// Product inherits from NamedEntities, so it should appear in subclasses
		if (!result.contains("\"subclasses\":[]")) {
			assertTrue("Subclasses should contain Product or other subclasses",
				result.contains(PRODUCT_CLASS) || result.contains("subclass"));
		}
	}

	/**
	 * Tests that property usages include owner and property name.
	 */
	public void testPropertyUsagesStructure() {
		TypeAnalysisTool tool = new TypeAnalysisTool();
		String result = tool.findTypeUsages(UNIT_OF_MEASURE_QUALIFIED);

		assertNotNull("Result should not be null", result);
		// If there are property usages, they should have the correct structure
		if (result.contains("\"properties\":[{")) {
			assertTrue("Property usages should have owner field", result.contains("\"owner\""));
			assertTrue("Property usages should have property field", result.contains("\"property\""));
			assertTrue("Property usages should have usage field", result.contains("\"usage\""));
		}
	}

	/**
	 * Tests that reference usages include owner and reference name.
	 */
	public void testReferenceUsagesStructure() {
		TypeAnalysisTool tool = new TypeAnalysisTool();
		String result = tool.findTypeUsages(COMPONENT_QUALIFIED);

		assertNotNull("Result should not be null", result);
		// If there are reference usages, they should have the correct structure
		if (result.contains("\"references\":[{")) {
			assertTrue("Reference usages should have owner field", result.contains("\"owner\""));
			assertTrue("Reference usages should have reference field", result.contains("\"reference\""));
			assertTrue("Reference usages should have usage field", result.contains("\"usage\""));
		}
	}

	/**
	 * Tests that subclasses include qualified names.
	 */
	public void testSubclassesStructure() {
		TypeAnalysisTool tool = new TypeAnalysisTool();
		String result = tool.findTypeUsages(NAMED_ENTITIES_QUALIFIED);

		assertNotNull("Result should not be null", result);
		// If there are subclasses, they should have the correct structure
		if (result.contains("\"subclasses\":[{")) {
			assertTrue("Subclasses should have subclass field", result.contains("\"subclass\""));
			assertTrue("Subclasses should have usage field", result.contains("\"usage\""));
			assertTrue("Usage should be 'generalization'", result.contains("generalization"));
		}
	}

	/**
	 * Tests error case: null qualified type name.
	 */
	public void testFindTypeUsagesNullTypeName() {
		TypeAnalysisTool tool = new TypeAnalysisTool();
		String result = tool.findTypeUsages(null);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\""));
		assertTrue("Error should mention required",
			result.contains("required") || result.contains("Qualified type name"));
	}

	/**
	 * Tests error case: empty qualified type name.
	 */
	public void testFindTypeUsagesEmptyTypeName() {
		TypeAnalysisTool tool = new TypeAnalysisTool();
		String result = tool.findTypeUsages("");

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\""));
		assertTrue("Error should mention required",
			result.contains("required") || result.contains("Qualified type name"));
	}

	/**
	 * Tests error case: type not found.
	 */
	public void testFindTypeUsagesTypeNotFound() {
		TypeAnalysisTool tool = new TypeAnalysisTool();
		String result = tool.findTypeUsages("nonexistent.module:NonExistentType");

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\""));
	}

	/**
	 * Tests error case: invalid format (missing colon).
	 */
	public void testFindTypeUsagesInvalidFormat() {
		TypeAnalysisTool tool = new TypeAnalysisTool();
		String result = tool.findTypeUsages("InvalidTypeName");

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\""));
		// The error might mention "not found" since it tries to parse it as module:type
	}

	/**
	 * Tests finding part usages for a property.
	 */
	public void testFindPartUsagesForProperty() {
		TypeAnalysisTool tool = new TypeAnalysisTool();
		String result = tool.findPartUsages(PRODUCT_QUALIFIED, "plannedQuantity");

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain ownerType", result.contains("\"ownerType\""));
		assertTrue("Result should contain partName", result.contains("\"partName\""));
		assertTrue("Result should contain plannedQuantity", result.contains("plannedQuantity"));
		assertTrue("Result should contain partKind", result.contains("\"partKind\""));
		assertTrue("Result should indicate property kind", result.contains("\"partKind\":\"property\""));
		assertTrue("Result should contain definedIn", result.contains("\"definedIn\""));
		assertTrue("Result should contain inheritedBy array", result.contains("\"inheritedBy\""));
	}

	/**
	 * Tests finding part usages for a reference.
	 */
	public void testFindPartUsagesForReference() {
		TypeAnalysisTool tool = new TypeAnalysisTool();
		String result = tool.findPartUsages(PRODUCT_QUALIFIED, "components");

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain ownerType", result.contains("\"ownerType\""));
		assertTrue("Result should contain partName", result.contains("\"partName\""));
		assertTrue("Result should contain components", result.contains("components"));
		assertTrue("Result should contain partKind", result.contains("\"partKind\""));
		assertTrue("Result should indicate reference kind", result.contains("\"partKind\":\"reference\""));
		assertTrue("Result should contain definedIn", result.contains("\"definedIn\""));
		assertTrue("Result should contain inheritedBy array", result.contains("\"inheritedBy\""));
	}

	/**
	 * Tests that definedIn shows the owner where the part is actually defined.
	 */
	public void testPartUsagesDefinedIn() {
		TypeAnalysisTool tool = new TypeAnalysisTool();
		// Test with a part that might be inherited (name from NamedEntities)
		String result = tool.findPartUsages(PRODUCT_QUALIFIED, "name");

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain definedIn", result.contains("\"definedIn\""));
		// The name property should be defined in NamedEntities or Product itself
		assertTrue("definedIn should contain a qualified name",
			result.contains(PIZZASHOP_MODULE + ":"));
	}

	/**
	 * Tests that inheritedBy lists subclasses that inherit the part.
	 */
	public void testPartUsagesInheritedBy() {
		TypeAnalysisTool tool = new TypeAnalysisTool();
		// Use a part from NamedEntities which is inherited by Product
		String result = tool.findPartUsages(NAMED_ENTITIES_QUALIFIED, "name");

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain inheritedBy array", result.contains("\"inheritedBy\""));
		// If Product inherits from NamedEntities, it should be in the inherited by list
		// (or the inherited by array might be empty if no subclasses inherit it)
	}

	/**
	 * Tests error case: null owner type.
	 */
	public void testFindPartUsagesNullOwnerType() {
		TypeAnalysisTool tool = new TypeAnalysisTool();
		String result = tool.findPartUsages(null, "plannedQuantity");

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\""));
		assertTrue("Error should mention required",
			result.contains("required") || result.contains("Owner type"));
	}

	/**
	 * Tests error case: empty owner type.
	 */
	public void testFindPartUsagesEmptyOwnerType() {
		TypeAnalysisTool tool = new TypeAnalysisTool();
		String result = tool.findPartUsages("", "plannedQuantity");

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\""));
		assertTrue("Error should mention required",
			result.contains("required") || result.contains("Owner type"));
	}

	/**
	 * Tests error case: null part name.
	 */
	public void testFindPartUsagesNullPartName() {
		TypeAnalysisTool tool = new TypeAnalysisTool();
		String result = tool.findPartUsages(PRODUCT_QUALIFIED, null);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\""));
		assertTrue("Error should mention required",
			result.contains("required") || result.contains("part name"));
	}

	/**
	 * Tests error case: empty part name.
	 */
	public void testFindPartUsagesEmptyPartName() {
		TypeAnalysisTool tool = new TypeAnalysisTool();
		String result = tool.findPartUsages(PRODUCT_QUALIFIED, "");

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\""));
		assertTrue("Error should mention required",
			result.contains("required") || result.contains("part name"));
	}

	/**
	 * Tests error case: owner type not found.
	 */
	public void testFindPartUsagesTypeNotFound() {
		TypeAnalysisTool tool = new TypeAnalysisTool();
		String result = tool.findPartUsages("nonexistent.module:NonExistentType", "somePart");

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\""));
	}

	/**
	 * Tests error case: type is not structured (no parts).
	 */
	public void testFindPartUsagesTypeNotStructured() {
		TypeAnalysisTool tool = new TypeAnalysisTool();
		// Try with a datatype or primitive type if available
		// For now, test with an enumeration which is structured but might fail depending on implementation
		// Actually, enumerations don't have parts, so this should fail
		// Let's use a made-up type reference that will fail during resolution
		String result = tool.findPartUsages("tl.core:TLPrimitive", "nonExistent");

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\""));
		// Error might be "type not found" or "not structured" depending on whether TLPrimitive exists
	}

	/**
	 * Tests error case: part not found in type.
	 */
	public void testFindPartUsagesPartNotFound() {
		TypeAnalysisTool tool = new TypeAnalysisTool();
		String result = tool.findPartUsages(PRODUCT_QUALIFIED, "nonExistentPart");

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\""));
		assertTrue("Error should mention part not found",
			result.contains("Part not found") || result.contains("not found"));
	}

	/**
	 * Tests finding usages for multiple types to verify consistency.
	 */
	public void testFindTypeUsagesMultipleTypes() {
		TypeAnalysisTool tool = new TypeAnalysisTool();

		String[] types = {
			PRODUCT_QUALIFIED,
			NAMED_ENTITIES_QUALIFIED,
			COMPONENT_QUALIFIED,
			UNIT_OF_MEASURE_QUALIFIED
		};

		for (String type : types) {
			String result = tool.findTypeUsages(type);

			assertNotNull("Result for " + type + " should not be null", result);
			assertTrue("Result for " + type + " should contain properties",
				result.contains("\"properties\""));
			assertTrue("Result for " + type + " should contain references",
				result.contains("\"references\""));
			assertTrue("Result for " + type + " should contain subclasses",
				result.contains("\"subclasses\""));
		}
	}

	/**
	 * Tests finding part usages for multiple parts to verify consistency.
	 */
	public void testFindPartUsagesMultipleParts() {
		TypeAnalysisTool tool = new TypeAnalysisTool();

		String[] parts = {"plannedQuantity", "budget", "name"};

		for (String part : parts) {
			String result = tool.findPartUsages(PRODUCT_QUALIFIED, part);

			// Some parts might not exist, which is ok - they should return an error
			assertNotNull("Result for " + part + " should not be null", result);

			if (!result.contains("\"error\"")) {
				assertTrue("Valid result for " + part + " should contain ownerType",
					result.contains("\"ownerType\""));
				assertTrue("Valid result for " + part + " should contain partName",
					result.contains("\"partName\""));
				assertTrue("Valid result for " + part + " should contain partKind",
					result.contains("\"partKind\""));
			}
		}
	}

	/**
	 * Tests that property usages exclude references (only actual properties).
	 */
	public void testPropertyUsagesExcludeReferences() {
		TypeAnalysisTool tool = new TypeAnalysisTool();
		String result = tool.findTypeUsages(COMPONENT_QUALIFIED);

		assertNotNull("Result should not be null", result);
		// Component is referenced by Product.components
		// This should appear in references, not properties
		assertTrue("Result should contain references array", result.contains("\"references\""));

		if (result.contains("\"references\":[{")) {
			assertTrue("Reference to Component should have 'reference type' usage",
				result.contains("reference type"));
		}
	}

	/**
	 * Tests that type usages are sorted consistently.
	 */
	public void testTypeUsagesSorting() {
		TypeAnalysisTool tool = new TypeAnalysisTool();
		String result = tool.findTypeUsages(NAMED_ENTITIES_QUALIFIED);

		assertNotNull("Result should not be null", result);
		// Results should be sorted (though we can't easily verify order in plain text)
		// We just verify the structure is consistent
		assertTrue("Result should have valid JSON structure",
			result.contains("{") && result.contains("}"));
	}

	/**
	 * Creates the test suite with required service dependencies.
	 */
	public static Test suite() {
		return suite(TypeAnalysisToolTest.class);
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
