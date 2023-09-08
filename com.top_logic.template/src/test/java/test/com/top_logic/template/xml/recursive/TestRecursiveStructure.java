/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.template.xml.recursive;

import static test.com.top_logic.template.TemplateXMLTestUtil.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.template.xml.source.TemplateSource;

/**
 * Tests recursive structures.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class TestRecursiveStructure extends TestCase {

	/** Tests for a root node with no children. */
	public void testBinaryTreeOnlyRoot() {
		Map<String, Object> rootNode = trinaryMap("Left_Child", "", "Value", 1, "Right_Child", "");
		Map<String, Object> parameterValues = Collections.<String, Object> singletonMap("Binary_Tree_Root", rootNode);

		String expected = "(1)";
		assertExpansion(getTemplate("TestBinaryTree.xml"), parameterValues, expected);
	}

	/** Tests for a root node with two direct children. */
	public void testBinaryTreeRootWithTwoChildren() {
		Map<String, Object> rootNode = trinaryMap(
			"Left_Child", trinaryMap(
				"Left_Child", "",
				"Value", 1,
				"Right_Child", ""),
			"Value", 2,
			"Right_Child", trinaryMap(
				"Left_Child", "",
				"Value", 3,
				"Right_Child", ""));
		Map<String, Object> parameterValues = Collections.<String, Object> singletonMap("Binary_Tree_Root", rootNode);

		String expected = "((1)2(3))";
		assertExpansion(getTemplate("TestBinaryTree.xml"), parameterValues, expected);
	}

	/**
	 * Tests for a root node (first level) with two direct children (second level) and four indirect
	 * children (third level).
	 */
	public void testBinaryTreeThreeFullLevelChildren() {
		Map<String, Object> rootNode = trinaryMap(
			"Left_Child", trinaryMap(
				"Left_Child", trinaryMap(
					"Left_Child", "",
					"Value", 1,
					"Right_Child", ""),
				"Value", 2,
				"Right_Child", trinaryMap(
					"Left_Child", "",
					"Value", 3,
					"Right_Child", "")),
			"Value", 4,
			"Right_Child", trinaryMap(
				"Left_Child", trinaryMap(
					"Left_Child", "",
					"Value", 5,
					"Right_Child", ""),
				"Value", 6,
				"Right_Child", trinaryMap(
					"Left_Child", "",
					"Value", 7,
					"Right_Child", "")));
		Map<String, Object> parameterValues = Collections.<String, Object> singletonMap("Binary_Tree_Root", rootNode);

		String expected = "(((1)2(3))4((5)6(7)))";
		assertExpansion(getTemplate("TestBinaryTree.xml"), parameterValues, expected);
	}

	private Map<String, Object> trinaryMap(
			String firstKey, Object firstValue,
			String secondKey, Object secondValue,
			String thirdKey, Object thirdValue) {
		Map<String, Object> result = new HashMap<>();
		result.put(firstKey, firstValue);
		result.put(secondKey, secondValue);
		result.put(thirdKey, thirdValue);
		return result;
	}

	private TemplateSource getTemplate(String filename) {
		return createTemplateSource(filename, getClass());
	}

	/**
	 * Create the {@link TestSuite} with the necessary setups for this {@link Test}.
	 */
	public static Test suite() {
		// The setup is needed for some of the error messages that are being thrown when something
		// is broken. If everything is green, the setup would not be needed.
		return ModuleTestSetup.setupModule(TestRecursiveStructure.class);
	}

}
