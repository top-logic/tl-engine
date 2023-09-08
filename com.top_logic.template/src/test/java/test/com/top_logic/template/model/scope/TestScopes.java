/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.template.model.scope;

import static test.com.top_logic.template.TemplateXMLTestUtil.*;

import java.util.HashMap;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.template.xml.source.TemplateSource;
import com.top_logic.template.xml.source.TemplateSourceFactory;

/**
 * Tests if scopes work correctly.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class TestScopes extends TestCase {

	/**
	 * Tests if redefining a variable in a nested scope is forbidden.
	 */
	public void testScopeNestingVariableHiding() {
		Pattern expected = Pattern.compile(".*Example_Variable.*");
		assertExpansionFailure(getTemplate("TestScopeNestingVariableHiding.xml"), expected);
	}

	/**
	 * Tests that a variable cannot leave its definition scope and cannot be accessed outside of it.
	 */
	public void testScopeVariableLeavingScopeOfTemplate() {
		Pattern expected = Pattern.compile(".*Example_Variable.*");
		assertExpansionFailure(getTemplate("TestScopeVariableLeavingScopeOfTemplate.xml"), expected);
	}

	/**
	 * Tests that a variable can be accessed in scopes nested within its definition scope.
	 */
	public void testScopeVariableReadFromInnerScopeOfTemplate() {
		String expected = "Example Value" + "Example Value" + "Example Value";
		assertExpansion(getTemplate("TestScopeVariableReadFromInnerScopeOfTemplate.xml"), expected);
	}

	public void testScopeVariableLeavingScopeOfIfThen() {
		Pattern expected = Pattern.compile(".*Example_Variable.*");
		assertExpansionFailure(getTemplate("TestScopeVariableLeavingScopeOfIfThen.xml"), expected);
	}

	public void testScopeVariableReadFromInnerScopeOfIfThen() {
		String expected = "Example Value" + "Example Value" + "Example Value";
		assertExpansion(getTemplate("TestScopeVariableReadFromInnerScopeOfIfThen.xml"), expected);
	}

	public void testScopeVariableLeavingScopeOfIfElse() {
		Pattern expected = Pattern.compile(".*Example_Variable.*");
		assertExpansionFailure(getTemplate("TestScopeVariableLeavingScopeOfIfElse.xml"), expected);
	}

	public void testScopeVariableReadFromInnerScopeOfIfElse() {
		String expected = "Example Value" + "Example Value" + "Example Value";
		assertExpansion(getTemplate("TestScopeVariableReadFromInnerScopeOfIfElse.xml"), expected);
	}

	public void testScopeVariableLeavingScopeOfFor() {
		Pattern expected = Pattern.compile(".*Example_Variable.*");
		assertExpansionFailure(getTemplate("TestScopeVariableLeavingScopeOfFor.xml"), expected);
	}

	public void testScopeVariableReadFromInnerScopeOfFor() {
		String expected = "Example Value" + "Example Value" + "Example Value";
		assertExpansion(getTemplate("TestScopeVariableReadFromInnerScopeOfFor.xml"), expected);
	}

	public void testScopeNoReuseInForLoop() {
		String expected = "Example Value A" + "Example Value B" + "Example Value C";
		assertExpansion(getTemplate("TestScopeNoReuseInForLoop.xml"), expected);
	}

	public void testScopeVariableLeavingScopeOfDefault() {
		Pattern expected = Pattern.compile(".*Example_Variable.*");
		assertExpansionFailure(getTemplate("TestScopeVariableLeavingScopeOfDefault.xml"), expected);
	}

	public void testScopeSeparationBetweenDefaults() {
		Pattern expected = Pattern.compile(".*Example_Variable.*");
		assertExpansionFailure(getTemplate("TestScopeSeparationBetweenDefaults.xml"), expected);
	}

	public void testScopeSeparationBetweenDefaultEntries() {
		Pattern expected = Pattern.compile(".*Example_Variable.*");
		assertExpansionFailure(getTemplate("TestScopeSeparationBetweenDefaultEntries.xml"), expected);
	}

	public void testScopeVariableLeavingScopeOfInvoke() {
		Pattern expected = Pattern.compile(".*Example_Variable.*");
		assertExpansionFailure(getTemplate("TestScopeVariableLeavingScopeOfInvoke.xml"), expected);
	}

	public void testScopeVariableReadFromInnerScopeOfInvoke() {
		String expected = ""
			+ "Out1(Example Value)Out1"
			+ "In(ParameterValue(Example Value)ParameterValue)In"
			+ "Out2(Example Value)Out2";
		assertExpansion(getTemplate("TestScopeVariableReadFromInnerScopeOfInvoke.xml"), expected);
	}

	public void testScopeSeparationBetweenInvokeParameters() {
		Pattern expected = Pattern.compile(".*Example_Variable.*");
		assertExpansionFailure(getTemplate("TestScopeSeparationBetweenInvokeParameters.xml"), expected);
	}

	/**
	 * Test the expansion of a template where a variable is defined within an if that is within a
	 * for-loop. In the two iterations of the loop, the variable has two different types. This
	 * should test whether the the two variable definitions are kept separately or get mixed up
	 * resulting in a type error being reported.
	 */
	public void testScopeSwitchingTypes() {
		HashMap<String, Object> parameters = new HashMap<>();
		HashMap<String, Object> stringContainerContainer = new HashMap<>();
		HashMap<String, Object> integerContainerContainer = new HashMap<>();
		HashMap<String, Object> stringContainer = new HashMap<>();
		HashMap<String, Object> integerContainer = new HashMap<>();
		stringContainer.put("Example_Attribute", "fubar");
		integerContainer.put("Example_Attribute", 23);
		stringContainerContainer.put("Example_Attribute", stringContainer);
		integerContainerContainer.put("Example_Attribute", integerContainer);
		parameters.put("String_Parameter", stringContainerContainer);
		parameters.put("Integer_Parameter", integerContainerContainer);

		String expected = "fubar" + "23";
		assertExpansion(getTemplate("TestScopeSwitchingTypes.xml"), parameters, expected);
	}

	private TemplateSource getTemplate(String filename) {
		return createTemplateSource(filename, getClass());
	}

	/**
	 * Creates a suite containing the tests in this class and taking care of the necessary setups
	 * and teardowns.
	 */
	public static Test suite() {
		// The setup is needed for some of the error messages that are being thrown when something
		// is broken. If everything is green, the setup would not be needed.
		return ModuleTestSetup.setupModule(ServiceTestSetup.createSetup(TestScopes.class,
			TemplateSourceFactory.Module.INSTANCE));
	}

}
