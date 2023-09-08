/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.template.xml.settings.whitespace;

import static test.com.top_logic.template.TemplateXMLTestUtil.*;

import java.util.HashMap;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.template.xml.source.TemplateSource;

/**
 * Tests for the header setting for ignoring or keeping whitespaces.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class TestOutputWhitespaces extends TestCase {

	/** Tests if whitespaces are ignored, when 'ignore-whitespaces' is set to 'true'. */
	public void testIgnoreInTemplate() {
		HashMap<String, Object> parameterValues = new HashMap<>();

		String expected = ""
			+ "<foo>"
			+ "<!--   Surrounded by 3 Spaces on each side   -->"
			+ "<!--			Surrounded by 3 Tabs on each side			-->"
			+ "<!--\n\n\nSurrounded by 3 Linebreaks on each side\n\n\n-->"
			+ "<!--3 Spaces are right   3 Spaces are left-->"
			+ "<!--3 Tabs are right			3 Tabs are left-->"
			+ "<!--3 Linebreaks are right\n\n\n3 Linebreak are left-->"
			+ "</foo>"
			+ "<bar>Surrounded by 3 Spaces on each side</bar>"
			+ "<bar>Surrounded by 3 Tabs on each side</bar>"
			+ "<bar>Surrounded by 3 Linebreaks on each side</bar>"
			+ "<bar>3 Spaces are right   3 Spaces are left</bar>"
			+ "<bar>3 Tabs are right			3 Tabs are left</bar>"
			+ "<bar>3 Linebreaks are right\n\n\n3 Linebreak are left</bar>";
		assertExpansion(getTemplate("TestIgnoreInTemplate.xml"), parameterValues, expected);
	}

	/** Tests if whitespaces in the template are kept, if 'ignore-whitespaces' is set to 'false'. */
	public void testKeepInTemplate() {
		HashMap<String, Object> parameterValues = new HashMap<>();

		String expected = ""
			+ "\n\t\t<foo>"
			+ "\n\t\t\t<!--   Surrounded by 3 Spaces on each side   -->"
			+ "\n\t\t\t<!--			Surrounded by 3 Tabs on each side			-->"
			+ "\n\t\t\t<!--\n\n\nSurrounded by 3 Linebreaks on each side\n\n\n-->"
			+ "\n\t\t\t<!--3 Spaces are right   3 Spaces are left-->"
			+ "\n\t\t\t<!--3 Tabs are right			3 Tabs are left-->"
			+ "\n\t\t\t<!--3 Linebreaks are right\n\n\n3 Linebreak are left-->"
			+ "\n\t\t</foo>"
			+ "\n\t\t<bar>   Surrounded by 3 Spaces on each side   </bar>"
			+ "\n\t\t<bar>			Surrounded by 3 Tabs on each side			</bar>"
			+ "\n\t\t<bar>\n\n\nSurrounded by 3 Linebreaks on each side\n\n\n</bar>"
			+ "\n\t\t<bar>3 Spaces are right   3 Spaces are left</bar>"
			+ "\n\t\t<bar>3 Tabs are right			3 Tabs are left</bar>"
			+ "\n\t\t<bar>3 Linebreaks are right\n\n\n3 Linebreak are left</bar>"
			+ "\n\t";
		assertExpansion(getTemplate("TestKeepInTemplate.xml"), parameterValues, expected);
	}

	/**
	 * Tests if whitespaces in default value definitions are kept, if 'ignore-whitespaces' is set to
	 * 'false'.
	 */
	public void testKeepInDefaultValue() {
		HashMap<String, Object> parameterValues = new HashMap<>();

		String expected = "\n\t\t"
			+ "\n\t\t\t\t\t<foo>"
			+ "\n\t\t\t\t\t\t<!--   Surrounded by 3 Spaces on each side   -->"
			+ "\n\t\t\t\t\t\t<!--			Surrounded by 3 Tabs on each side			-->"
			+ "\n\t\t\t\t\t\t<!--\n\n\nSurrounded by 3 Linebreaks on each side\n\n\n-->"
			+ "\n\t\t\t\t\t\t<!--3 Spaces are right   3 Spaces are left-->"
			+ "\n\t\t\t\t\t\t<!--3 Tabs are right			3 Tabs are left-->"
			+ "\n\t\t\t\t\t\t<!--3 Linebreaks are right\n\n\n3 Linebreak are left-->"
			+ "\n\t\t\t\t\t</foo>"
			+ "\n\t\t\t\t\t<bar>   Surrounded by 3 Spaces on each side   </bar>"
			+ "\n\t\t\t\t\t<bar>			Surrounded by 3 Tabs on each side			</bar>"
			+ "\n\t\t\t\t\t<bar>\n\n\nSurrounded by 3 Linebreaks on each side\n\n\n</bar>"
			+ "\n\t\t\t\t\t<bar>3 Spaces are right   3 Spaces are left</bar>"
			+ "\n\t\t\t\t\t<bar>3 Tabs are right			3 Tabs are left</bar>"
			+ "\n\t\t\t\t\t<bar>3 Linebreaks are right\n\n\n3 Linebreak are left</bar>"
			+ "\n\t\t\t\t"
			+ "\n\t";
		assertExpansion(getTemplate("TestKeepInDefaultValue.xml"), parameterValues, expected);
	}

	/**
	 * Tests if whitespaces in default value definitions are ignored, if 'ignore-whitespaces' is set
	 * to 'true'.
	 */
	public void testIgnoreInDefaultValue() {
		HashMap<String, Object> parameterValues = new HashMap<>();

		String expected = ""
			+ "<foo>"
			+ "<!--   Surrounded by 3 Spaces on each side   -->"
			+ "<!--			Surrounded by 3 Tabs on each side			-->"
			+ "<!--\n\n\nSurrounded by 3 Linebreaks on each side\n\n\n-->"
			+ "<!--3 Spaces are right   3 Spaces are left-->"
			+ "<!--3 Tabs are right			3 Tabs are left-->"
			+ "<!--3 Linebreaks are right\n\n\n3 Linebreak are left-->"
			+ "</foo>"
			+ "<bar>Surrounded by 3 Spaces on each side</bar>"
			+ "<bar>Surrounded by 3 Tabs on each side</bar>"
			+ "<bar>Surrounded by 3 Linebreaks on each side</bar>"
			+ "<bar>3 Spaces are right   3 Spaces are left</bar>"
			+ "<bar>3 Tabs are right			3 Tabs are left</bar>"
			+ "<bar>3 Linebreaks are right\n\n\n3 Linebreak are left</bar>"
			+ "";
		assertExpansion(getTemplate("TestIgnoreInDefaultValue.xml"), parameterValues, expected);
	}

	/** Tests if whitespaces are kept in parameters, even if 'ignore-whitespaces' is set to 'true'. */
	public void testKeepInParameter() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("Example_Attribute_Type_String", " Value of String Parameter ");
		parameterValues.put("Example_Attribute_Type_Xml", "Some Text <!-- Some Comment --> More   Text <child-node />");

		String expected = " Value of String Parameter " + "Some Text <!-- Some Comment --> More   Text <child-node />";
		assertExpansion(getTemplate("TestKeepInParameter.xml"), parameterValues, expected);
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
		return ModuleTestSetup.setupModule(TestOutputWhitespaces.class);
	}

}
