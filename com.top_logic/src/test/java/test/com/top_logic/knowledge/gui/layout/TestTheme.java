/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.gui.layout;

import java.awt.Color;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.AssertNoErrorLogListener;
import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ExpectedFailure;
import test.com.top_logic.basic.ExpectedFailureProtocol;

import com.top_logic.basic.Log;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.gui.CSSBuffer;
import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeVar;
import com.top_logic.gui.config.ThemeConfig;
import com.top_logic.gui.config.ThemeConfig.StyleSheetRef;
import com.top_logic.gui.config.ThemeSetting.StringSetting;
import com.top_logic.gui.config.ThemeSettings;
import com.top_logic.gui.config.ThemeState;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.mig.html.DefaultResourceProvider;

/**
 * Test for {@link Theme}.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
@SuppressWarnings("javadoc")
public class TestTheme extends BasicTestCase {

    /**
     * Constructor a Testcase for given testName.
     */
    public TestTheme(String testName) {
        super(testName);
    }

	public void testResolveProperties() {
		Theme theme = new Theme("TestTheme1");
		ThemeConfig config = mkConfig("/themes/TestTheme1");

		ThemeSettings properties = mappings();
		putValue(properties, "KEY1", "value1");
		putValue(properties, "KEY2", "%KEY1%");
		putValue(properties, "KEY4", "before %KEY2% middle %KEY1% after");
		initialize(theme, null, config, properties);

		assertEquals("value1", theme.getRawValue("KEY1"));
		assertEquals("value1", theme.getRawValue("KEY2"));
		assertEquals("before value1 middle value1 after", theme.getRawValue("KEY4"));
	}

	public void testUndefinedReference() {
		Theme theme = new Theme("TestTheme1");
		ThemeConfig config = mkConfig("/themes/TestTheme1");

		ThemeSettings properties = mappings();
		putValue(properties, "KEY3", "%DOES_NOT_EXISTS%");
		try {
			initializeExpectFailure(theme, config, properties);
		} catch (ExpectedFailure ex) {
			assertContains("undefined setting 'DOES_NOT_EXISTS'", ex.getMessage());
		}
	}

	private void initializeExpectFailure(Theme theme, ThemeConfig config, ThemeSettings properties) {
		ExpectedFailureProtocol log = new ExpectedFailureProtocol();
		theme.initializeTheme(log, Collections.emptyMap(), null, config, properties);
		log.checkErrors();
		fail("Must not succeed.");
	}

	private ThemeSettings mappings() {
		return new ThemeSettings();
	}

	public void testResolvePropertiesQuote() {
		Theme theme = new Theme("TestTheme1");
		ThemeConfig config = mkConfig("/themes/TestTheme1");

		ThemeSettings properties = mappings();
		putValue(properties, "KEY1", "\\");
		putValue(properties, "KEY2", "$");
		putValue(properties, "KEY3", "quote %KEY1% and %KEY2%");
		initialize(theme, null, config, properties);

		assertEquals("\\", theme.getRawValue("KEY1"));
		assertEquals("$", theme.getRawValue("KEY2"));
		assertEquals("quote \\ and $", theme.getRawValue("KEY3"));
	}

	public void testTypedProperties() throws ConfigurationException {
		Theme theme = new Theme("TestTheme1");
		ThemeConfig config = mkConfig("/themes/TestTheme1");

		ThemeSettings properties = load(
			"<settings>" +
				"<boolean name='BOOLEAN' value='true'/>" +
				"<int name='INT' value='13'/>" +
				"<float name='FLOAT' value='41.99'/>" +
				"<string name='STRING' value='Hello world!'/>" +
				"<color name='COLOR' value='#ff0000'/>" +
				"<size name='DIM' value='50%'/>" +
				"<instance name='INSTANCE' interface='" + ResourceProvider.class.getName() + "' value='" +
					MetaResourceProvider.class.getName() + "'/>" +
				"<type name='CLASS' value='" + TestTheme.class.getName() + "'/>" +
			"</settings>");
		initialize(theme, null, config, properties);

		// Note: In regular applications, theme variables must be constants. The following is only
		// valid for testing.
		assertEquals(Boolean.TRUE, theme.getValue(ThemeVar.booleanVar("BOOLEAN")));
		assertEquals(Integer.valueOf(13), theme.getValue(ThemeVar.intVar("INT")));
		assertEquals(Float.valueOf(41.99f), theme.getValue(ThemeVar.floatVar("FLOAT")));
		assertEquals("Hello world!", theme.getValue(ThemeVar.stringVar("STRING")));
		assertEquals(new Color(255, 0, 0), theme.getValue(ThemeVar.colorVar("COLOR")));
		assertEquals(DisplayDimension.dim(50, DisplayUnit.PERCENT), theme.getValue(ThemeVar.dimVar("DIM")));
		assertEquals(MetaResourceProvider.INSTANCE,
			theme.getValue(ThemeVar.instanceVar("INSTANCE", ResourceProvider.class)));
		assertEquals(TestTheme.class,
			theme.getValue(ThemeVar.classVar("CLASS", Test.class)));
	}

	private ThemeSettings load(String themeSettingsXml) throws ConfigurationException {
		ThemeSettings result = new ThemeSettings();
		result.load(new AssertProtocol(), "TestTheme", CharacterContents.newContent(themeSettingsXml));
		return result;
	}

	public void testTypedPropertiesBuiltInDefaults() {
		Theme theme = new Theme("TestTheme1");
		ThemeConfig config = mkConfig("/themes/TestTheme1");
		initialize(theme, null, config, mappings());

		// Note: In regular applications, theme variables must be constants. The following is only
		// valid for testing.
		assertEquals(Boolean.FALSE, theme.getValue(ThemeVar.booleanVar("UNKNOWN")));
		assertEquals(Integer.valueOf(0), theme.getValue(ThemeVar.intVar("UNKNOWN")));
		assertEquals(Float.valueOf(0.0f), theme.getValue(ThemeVar.floatVar("UNKNOWN")));
		assertEquals(null, theme.getValue(ThemeVar.stringVar("UNKNOWN")));
		assertEquals(null, theme.getValue(ThemeVar.colorVar("UNKNOWN")));
		assertEquals(null, theme.getValue(ThemeVar.dimVar("UNKNOWN")));
		assertEquals(null,
			theme.getValue(ThemeVar.instanceVar("UNKNOWN", ResourceProvider.class)));
		assertEquals(null,
			theme.getValue(ThemeVar.classVar("UNKNOWN", Test.class)));
	}

	public void testTypedPropertiesDefaults() {
		Theme theme = new Theme("TestTheme1");
		ThemeConfig config = mkConfig("/themes/TestTheme1");
		initialize(theme, null, config, mappings());

		AssertNoErrorLogListener listener = new AssertNoErrorLogListener(true);
		listener.activate();
		try {
			// Note: In regular applications, theme variables must be constants. The following is
			// only
			// valid for testing.
			assertEquals(Boolean.TRUE, theme.getValue(ThemeVar.booleanVar("UNKNOWN", true)));
			assertEquals(Integer.valueOf(3), theme.getValue(ThemeVar.intVar("UNKNOWN", 3)));
			assertEquals(Float.valueOf(33.3f), theme.getValue(ThemeVar.floatVar("UNKNOWN", 33.3f)));
			assertEquals("default", theme.getValue(ThemeVar.stringVar("UNKNOWN", "default")));
			assertEquals(Color.red, theme.getValue(ThemeVar.colorVar("UNKNOWN", Color.red)));
			assertEquals(new Color(0, 255, 0), theme.getValue(ThemeVar.colorVar("UNKNOWN2", "#00FF00")));
			assertEquals(DisplayDimension.dim(10, DisplayUnit.PIXEL),
				theme.getValue(ThemeVar.dimVar("UNKNOWN", DisplayDimension.dim(10, DisplayUnit.PIXEL))));
			assertEquals(DefaultResourceProvider.INSTANCE,
				theme.getValue(ThemeVar.instanceVar("UNKNOWN", ResourceProvider.class,
					DefaultResourceProvider.INSTANCE)));
			assertEquals(DefaultResourceProvider.INSTANCE,
				theme.getValue(ThemeVar.instanceVarWithClassDefault("UNKNOWN", ResourceProvider.class,
					DefaultResourceProvider.class)));
			assertEquals(TestTheme.class,
				theme.getValue(ThemeVar.classVar("UNKNOWN", Test.class, TestTheme.class)));
		} finally {
			listener.deactivate();
		}

		listener.assertNoErrorLogged("Theme variable lookup caused error.");
	}

	public void testResolveFileLinkProperties() throws IOException {
		Theme theme1 = new Theme("TestTheme1");
		ThemeConfig config1 = mkConfig("/themes/TestTheme1");

		ThemeSettings properties = mappings();
		putValue(properties, "KEY1", "url(/data/a.txt)");
		putValue(properties, "KEY1a", "%KEY1%");
		putValue(properties, "KEY2", "url(/data/b.txt)");
		putValue(properties, "KEY2a", "%KEY2%");
		initialize(theme1, null, config1, properties);

		Theme theme2 = new Theme("TestTheme2");
		ThemeConfig config2 = mkConfig("/themes/TestTheme2");
		initialize(theme2, Arrays.asList(theme1), config2, mappings());

		assertCSSVariableDefinition(theme1, "url(/demo/themes/TestTheme1/data/a.txt)", "KEY1");
		assertCSSVariableDefinition(theme1, "url(/demo/themes/TestTheme1/data/a.txt)", "KEY1");
		assertCSSVariableDefinition(theme1, "url(/demo/themes/TestTheme1/data/a.txt)", "KEY1a");
		assertCSSVariableDefinition(theme1, "url(/demo/themes/TestTheme1/data/b.txt)", "KEY2");
		assertCSSVariableDefinition(theme1, "url(/demo/themes/TestTheme1/data/b.txt)", "KEY2a");

		assertCSSVariableDefinition(theme2, "url(/demo/themes/TestTheme2/data/a.txt)", "KEY1");
		assertCSSVariableDefinition(theme2, "url(/demo/themes/TestTheme2/data/a.txt)", "KEY1a");
		assertCSSVariableDefinition(theme2, "url(/demo/themes/TestTheme1/data/b.txt)", "KEY2");
		assertCSSVariableDefinition(theme2, "url(/demo/themes/TestTheme1/data/b.txt)", "KEY2a");
	}

	private void assertCSSVariableDefinition(Theme theme, String expectedValue, String themeVariableName) throws IOException {
		CSSBuffer buffer = new CSSBuffer();
		String cssVariableEvaluation = String.format(CSSBuffer.VARIABLE_EVALUATION_FORMAT, themeVariableName);
		buffer.readStyleSheet(null, new StringReader(cssVariableEvaluation));
		buffer.replaceVariables(null, "/demo", theme);
		String cssVariableDefinition = String.format(CSSBuffer.VARIABLE_DEFINITION_FORMAT, themeVariableName, expectedValue);
		assertContains(cssVariableDefinition, buffer.toString());
	}

	public void testResolveIllegalProperties() {
		Theme theme = new Theme("TestTheme1");
		ThemeConfig config = mkConfig("/themes/TestTheme1");

		ThemeSettings properties = mappings();
		putValue(properties, "KEY1", "%KEY2%");
		putValue(properties, "KEY2", "%KEY1%");
		try {
			initializeExpectFailure(theme, config, properties);
			fail("Cyclic alias definition in properties not detected.");
		} catch (ExpectedFailure ex) {
			// cyclic alias definition can not be resolved.
			assertContains("Cyclic reference in property 'KEY2'", ex.getMessage());
		}

	}

	private void putValue(ThemeSettings properties, String key, String value) {
		properties.addDefault(null, null, StringSetting.newExpressionInstance(key, value));
	}

	public void testInheritanceWithRecursiveVariables() {
		Theme theme1 = new Theme("TestTheme1");
		initialize(theme1, null, mkTestTheme1Config(), mappings1());

		Theme theme2 = new Theme("TestTheme2");
		initialize(theme2, Arrays.asList(theme1), mkTestTheme2Config(), mappings2());

		// Assert defaults.
		assertEquals("var1-value", theme1.getRawValue("VAR1"));
		assertEquals("var2-value", theme1.getRawValue("VAR2"));

		assertEquals("var1-value", theme1.getRawValue("VAR3"));
		assertEquals("var2-value", theme1.getRawValue("VAR4"));

		assertEquals("x-var1-value", theme1.getRawValue("VAR5"));
		assertEquals("y-var2-value", theme1.getRawValue("VAR6"));

		// Test inheritance.
		assertEquals("var1-new-value", theme2.getRawValue("VAR1"));
		assertEquals("var2-value", theme2.getRawValue("VAR2"));

		assertEquals("var1-new-value", theme2.getRawValue("VAR3"));
		assertEquals("var1-new-value", theme2.getRawValue("VAR4"));

		assertEquals("x-var1-new-value", theme2.getRawValue("VAR5"));
		assertEquals("y-var1-new-value", theme2.getRawValue("VAR6"));
	}

	private ThemeConfig mkTestTheme2Config() {
		return mkConfig("/themes/TestTheme2");
	}

	private ThemeSettings mappings2() {
		ThemeSettings properties = mappings();
		putValue(properties, "VAR1", "var1-new-value");
		putValue(properties, "VAR4", "%VAR3%");
		return properties;
	}

	private ThemeConfig mkTestTheme1Config() {
		return mkConfig("/themes/TestTheme1");
	}

	private ThemeSettings mappings1() {
		ThemeSettings properties = mappings();
		putValue(properties, "VAR1", "var1-value");
		putValue(properties, "VAR2", "var2-value");
		putValue(properties, "VAR3", "%VAR1%");
		putValue(properties, "VAR4", "%VAR2%");
		putValue(properties, "VAR5", "x-%VAR3%");
		putValue(properties, "VAR6", "y-%VAR4%");
		return properties;
	}

	public void testGetStyleSheets() {
		Theme defaultTheme = mkDefaultTheme();
        String     thePrefix = Theme.WORK_STYLE_PATH;
		String css = defaultTheme.getStyleSheet();

		assertNotNull("No style sheets returned.", css);
        assertTrue("Returned styles doesn't start with " + thePrefix, 
			css.startsWith(thePrefix));
    }

	public void testProperties() {
		Theme defaultTheme = mkDefaultTheme();
		String theSize = defaultTheme.getRawValue("TABBER_SIZE");
        assertNotNull(theSize);
		assertEquals("15", theSize);
    }
    
    /**
     * test if fallback to default works
     */
    public void testGetFileLink() throws Exception {
		Theme theme2 = mkDerivedTheme();
        String     theLink;
        
		theLink = theme2.getFileLink("/data/a.txt");
        assertNotNull(theLink);
		assertTrue("I'm not as blue as can be", theLink.indexOf("/themes/TestTheme2") > -1);

		theLink = theme2.getFileLink("/data/b.txt");
        assertNotNull(theLink);
		assertTrue("fallback to default does not work", theLink.indexOf("themes/TestTheme1/") > -1);
        
		assertNull(theme2.getFileLink(null));
		assertEquals("returnGiGo", theme2.getFileLink("returnGiGo"));
        
    }

	public void testMultipleInheritance() throws IOException {
		Theme theme1 = new Theme("TestTheme1");
		ThemeConfig config1 = mkConfig("/themes/TestTheme1");

		ThemeSettings properties1 = mappings();
		putValue(properties1, "KEY1", "url(/data/a.txt)");
		putValue(properties1, "KEY1a", "%KEY1%");
		putValue(properties1, "KEY2", "ExpectedVar");
		initialize(theme1, null, config1, properties1);

		Theme theme2 = new Theme("TestTheme2");
		ThemeConfig config2 = mkConfig("/themes/TestTheme2");

		ThemeSettings properties2 = mappings();
		putValue(properties2, "KEY1", "url(/data/a.txt)");
		putValue(properties2, "KEY2", "IgnoredVar");
		putValue(properties2, "KEY3", "ExpectedVarTheme2");
		initialize(theme2, null, config2, properties2);

		Theme theme3 = new Theme("TestTheme3");
		ThemeConfig config3 = mkConfig("/themes/TestTheme3");
		initialize(theme3, Arrays.asList(theme1, theme2), config3, mappings());

		assertCSSVariableDefinition(theme1, "url(/demo/themes/TestTheme1/data/a.txt)", "KEY1");
		assertCSSVariableDefinition(theme1, "url(/demo/themes/TestTheme1/data/a.txt)", "KEY1a");
		assertCSSVariableDefinition(theme1, "ExpectedVar", "KEY2");

		assertCSSVariableDefinition(theme2, "url(/demo/themes/TestTheme2/data/a.txt)", "KEY1");
		assertCSSVariableDefinition(theme2, "IgnoredVar", "KEY2");
		assertCSSVariableDefinition(theme2, "ExpectedVarTheme2", "KEY3");

		assertCSSVariableDefinition(theme3, "url(/demo/themes/TestTheme1/data/a.txt)", "KEY1");
		assertCSSVariableDefinition(theme3, "url(/demo/themes/TestTheme1/data/a.txt)", "KEY1a");
		assertCSSVariableDefinition(theme3, "ExpectedVar", "KEY2");
		assertCSSVariableDefinition(theme3, "ExpectedVarTheme2", "KEY3");
	}

	private Theme mkDefaultTheme() {
		Theme defaultTheme = new Theme("TestTheme1");
		ThemeConfig configuration = mkConfig("/themes/TestTheme1", "/styles/some.css");
		initialize(defaultTheme, null, configuration, mappings("TABBER_SIZE", "15"));
		return defaultTheme;
	}

	private ThemeSettings mappings(String key, String value) {
		ThemeSettings properties = mappings();
		putValue(properties, key, value);
		return properties;
	}

	private Theme mkDerivedTheme() {
		Theme defaultTheme = new Theme("TestTheme2");
		ThemeConfig configuration = mkConfig("/themes/TestTheme2");
		initialize(defaultTheme, Arrays.asList(mkDefaultTheme()), configuration, mappings("TABBER_SIZE", "25"));
		return defaultTheme;
	}

	private void initialize(Theme theme, List<Theme> parents, ThemeConfig themeConfig, ThemeSettings settings) {
		theme.initializeTheme(log(), Collections.emptyMap(), parents, themeConfig, settings);
	}

	private Log log() {
		return new AssertProtocol();
	}

	private ThemeConfig mkConfig(String path, String... styles) {
		ThemeConfig result = TypedConfiguration.newConfigItem(ThemeConfig.class);
		result.setState(ThemeState.ENABLED);
		result.setPath(path);
		for (String style : styles) {
			result.getStyles().add(mkName(style));
		}
		return result;
	}

	private StyleSheetRef mkName(String name) {
		StyleSheetRef namedConfig = TypedConfiguration.newConfigItem(StyleSheetRef.class);
		namedConfig.setName(name);
		return namedConfig;
	}

    /**
     * The method constructing a test suite for this class.
     *
     * @return    The test to be executed.
     */
    public static Test suite () {
        TestSuite theSuite = new TestSuite(TestTheme.class);

        return TLTestSetup.createTLTestSetup(theSuite);
    }

    /**
     * Start this test.
     * @param args not used
     */      
    public static void main(String[] args) {
        SHOW_TIME = true;
        TestRunner.run (suite ());
    }
}
