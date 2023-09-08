/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.processor;

import java.io.IOException;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

import org.w3c.dom.Document;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ExpectedFailure;
import test.com.top_logic.basic.ExpectedFailureProtocol;

import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.layout.processor.ConstantLayout;
import com.top_logic.layout.processor.LayoutInline;
import com.top_logic.layout.processor.ResolveFailure;

/**
 * Test case for {@link LayoutInline}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestLayoutInline extends LayoutModelTest {

	private LayoutInline _inliner;

	public TestLayoutInline(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_inliner = new LayoutInline(getResolver());
	}

	@Override
	protected void tearDown() throws Exception {
		_inliner = null;

		super.tearDown();
	}

	/**
	 * Direct reference as top-level element.
	 */
	public void testA() throws IOException, ResolveFailure {
		doTestInline("test-1-expected.xml", "a.xml");
	}

	/**
	 * Indirect reference as top-level element and complex arguments.
	 */
	public void test1() throws IOException, ResolveFailure {
		doTestInline("test-1-expected.xml", "test-1.xml");
	}

	/**
	 * Content injection.
	 */
	public void test2() throws IOException, ResolveFailure {
		doTestInline("test-2-expected.xml", "test-2.xml");
	}

	/**
	 * Fragment injection.
	 */
	public void test3() throws IOException, ResolveFailure {
		doTestInline("test-3-expected.xml", "test-3.xml");
	}

	/**
	 * Fragment injecting another fragment.
	 */
	public void test4() throws IOException, ResolveFailure {
		doTestInline("test-40-expected.xml", "test-4-fragment-include.xml");
	}

	/**
	 * Cross-referencing parameters.
	 */
	public void test5() throws IOException, ResolveFailure {
		doTestInline("test-50-expected.xml", "test-5-cross-parameter-reference.xml");
	}

	/**
	 * Referencing a parameter in template arguments that is defined within the template with a
	 * default value.
	 */
	public void test6() throws IOException, ResolveFailure {
		doTestInline("test-60-expected.xml", "test-6-cross-parameter-reference-of-default-parameter.xml");
	}

	/**
	 * Referencing a parameter from an outer scope within template arguments.
	 */
	public void test7() throws IOException, ResolveFailure {
		doTestInline("test-70-expected.xml", "test-7-parameter-from-outer-scope.xml");
	}

	/**
	 * Referencing variables from different scopes.
	 */
	public void testC() throws IOException, ResolveFailure {
		doTestInline("test-C0-expected.xml", "test-C-referencing-variables-from-different-scopes.xml");
	}

	/**
	 * Including template in template argument.
	 */
	public void testD() throws IOException, ResolveFailure {
		doTestInline("test-D0-expected.xml", "test-D-include-in-param.xml");
	}

	/**
	 * Including template in template argument.
	 */
	public void testE() throws IOException, ResolveFailure {
		doTestInline("test-E0-expected.xml", "test-E-variable-reference-in-default-values.xml");
	}

	/**
	 * Passing complex parameters through attribute values.
	 */
	public void testG() throws IOException, ResolveFailure {
		doTestInline("test-G0-expected.xml", "test-G-complex-argument-passing.xml");
	}

	/**
	 * Passing complex parameters empty default.
	 */
	public void testH() throws IOException, ResolveFailure {
		doTestInline("test-H0-expected.xml", "test-H-complex-argument-with-empty-default.xml");
	}

	public void testI() throws IOException, ResolveFailure {
		doTestInline("test-I0-expected.xml", "test-I0-template0.xml");
	}

	public void testJ() throws IOException, ResolveFailure {
		doTestInline("test-J0-expected.xml", "test-J0-template0.xml");
	}

	public void testExpandStructureInAttributeContext() throws IOException, ResolveFailure {
		doTestInline("test-K2-expected.xml", "test-K0-expand-structure-in-attribute-context.xml");
	}

	public void testL() throws IOException, ResolveFailure {
		doTestInline("test-L2-expected.xml", "test-L0-expand-default-structure-in-attribute-context.xml");
	}

	public void testM() throws IOException, ResolveFailure {
		doTestInline("test-M2-expected.xml", "test-M0-list-argument.xml");
	}

	public void testP() throws IOException, ResolveFailure {
		doTestInline("test-P2-expected.xml", "test-P0-expand-multi-line-text.xml");
	}

	public void testParameterInDefaultValue() throws IOException, ResolveFailure {
		doTestInline("test-N2-expected.xml", "test-N0-parameter-in-default-value.xml");
	}

	public void testZ4() throws IOException, ResolveFailure {
		doTestInlineFail("Not in reference context, must not use parameter cross reference syntax: '#{p1}'",
			"test-Z-fail4-cross-parameter-reference-in-default-value.xml");
	}

	public void testZ5() throws IOException, ResolveFailure {
		doTestInlineFail("Access to undefined variable 'outer'",
			"test-Z-fail5-outer-variable-access-in-default-value.xml");
	}

	public void testZ6() throws IOException, ResolveFailure {
		doTestInlineFail("Cyclic variable reference",
			"test-Z-fail6-cyclic-variable-reference.xml");
	}

	public void testZ7() throws IOException, ResolveFailure {
		doTestInlineFail("Cyclic parameter reference",
			"test-Z-fail7-cyclic-parameter-reference.xml");
	}

	public void testZ8() throws IOException, ResolveFailure {
		doTestInlineFail("Access to undefined include argument 'p2'",
			"test-Z-fail8-undefined-include-argument.xml");
	}

	public void testZ9() throws IOException, ResolveFailure {
		doTestInlineFail("No argument for mandatory parameters [p2]",
			"test-Z-fail9-undefined-mandatory-parameter.xml");
	}

	private void doTestInlineFail(String expectedFailureContents, String layoutName) throws ResolveFailure, IOException {
		setProtocol(new ExpectedFailureProtocol());
		try {
			ConstantLayout rootLayout = process(qualifyLayout(layoutName));

			Document resultDocument = rootLayout.getLayoutDocument();

			// Debugging.
			System.out.println("Expected failure '" + expectedFailureContents + "' but produced document:");
			DOMUtil.serializeXMLDocument(System.out, true, resultDocument);

			fail("Expected processing to fail with message '" + expectedFailureContents + "'.");
		} catch (ExpectedFailure ex) {
			BasicTestCase.assertContains("Wrong failure message.", expectedFailureContents, ex.getMessage());
		}
	}

	private void doTestInline(String expectedLayoutName, String sourceLayoutName) throws ResolveFailure, IOException {
		ConstantLayout rootLayout = process(qualifyLayout(sourceLayoutName));

		Document resultDocument = rootLayout.getLayoutDocument();

		Document expectedDocument = getResolver().getLayout(qualifyLayout(expectedLayoutName)).getLayoutDocument();

		try {
			BasicTestCase.assertEqualsDocument(ONLY_NO_NAMESPACE, expectedDocument, resultDocument);
		} catch (AssertionFailedError ex) {
			System.out.println("Expected '" + expectedLayoutName + "' but produced:");
			DOMUtil.serializeXMLDocument(System.out, true, resultDocument);
			throw ex;
		}
	}

	private static String qualifyLayout(String layout) {
		return TestLayoutInline.class.getName() + '/' + layout;
	}

	private ConstantLayout process(String sourceLayoutName) throws ResolveFailure {
		ConstantLayout rootLayout = getResolver().getLayout(sourceLayoutName);
		_inliner.inline(rootLayout);
		getResolver().getProtocol().checkErrors();
		return rootLayout;
	}

	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(TestLayoutInline.class);
	}

}
