/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.theme;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.react.theme.ColorScheme;
import com.top_logic.layout.react.theme.UITheme;
import com.top_logic.layout.react.theme.UIThemeService;

/**
 * Tests for {@link UIThemeService}: the color scheme of a theme, the theme answering the operating
 * system's appearance preference, and the CSS and script emitted for them.
 */
public class TestUIThemeService extends TestCase {

	/** A light default theme and a dark theme extending it, marked as the system's dark answer. */
	private static final String LIGHT_AND_DARK = config("light",
		"<theme name='light'>"
			+ "<color name='background' value='#ffffff'/>"
			+ "<color name='text' value='#000000'/>"
			+ "</theme>"
			+ "<theme name='dark' extends='light' color-scheme='dark' system-default='true'>"
			+ "<color name='background' value='#000000'/>"
			+ "</theme>");

	/** A dark root theme and a child declaring no scheme of its own. */
	private static final String DARK_AND_CHILD = config("night",
		"<theme name='night' color-scheme='dark'>"
			+ "<color name='background' value='#000000'/>"
			+ "</theme>"
			+ "<theme name='night-contrast' extends='night'>"
			+ "<color name='background' value='#111111'/>"
			+ "</theme>");

	/** A single theme, leaving nothing to follow the operating system with. */
	private static final String SINGLE = config("light",
		"<theme name='light'>"
			+ "<color name='background' value='#ffffff'/>"
			+ "</theme>");

	/** Two themes claiming the same appearance preference of the operating system. */
	private static final String AMBIGUOUS_SYSTEM_DEFAULT = config("light",
		"<theme name='light'>"
			+ "<color name='background' value='#ffffff'/>"
			+ "</theme>"
			+ "<theme name='dark' color-scheme='dark' system-default='true'>"
			+ "<color name='background' value='#000000'/>"
			+ "</theme>"
			+ "<theme name='midnight' color-scheme='dark' system-default='true'>"
			+ "<color name='background' value='#000005'/>"
			+ "</theme>");

	/** An abstract base theme and two concrete themes extending it. */
	private static final String ABSTRACT_BASE = config("light",
		"<theme name='base' abstract='true'>"
			+ "<color name='background' value='#ffffff'/>"
			+ "<color name='text' value='#000000'/>"
			+ "</theme>"
			+ "<theme name='light' extends='base'>"
			+ "<color name='accent' value='#0000ff'/>"
			+ "</theme>"
			+ "<theme name='dark' extends='base' color-scheme='dark' system-default='true'>"
			+ "<color name='background' value='#000000'/>"
			+ "</theme>");

	/** An abstract theme configured as the default theme. */
	private static final String ABSTRACT_DEFAULT = config("base",
		"<theme name='base' abstract='true'>"
			+ "<color name='background' value='#ffffff'/>"
			+ "</theme>"
			+ "<theme name='light' extends='base'/>");

	/** An abstract dark theme extended by a light theme only. */
	private static final String ABSTRACT_DARK_BASE = config("light",
		"<theme name='base' abstract='true' color-scheme='dark'>"
			+ "<color name='background' value='#000000'/>"
			+ "</theme>"
			+ "<theme name='light' extends='base' color-scheme='light'/>");

	/** An abstract theme marked as answering the operating system's preference. */
	private static final String ABSTRACT_SYSTEM_DEFAULT = config("light",
		"<theme name='base' abstract='true' color-scheme='dark' system-default='true'>"
			+ "<color name='background' value='#000000'/>"
			+ "</theme>"
			+ "<theme name='light' extends='base' color-scheme='light'/>");

	/**
	 * Each theme declares the color scheme of its appearance beside its custom properties, and the
	 * default theme is bound to {@code :root} as well.
	 */
	public void testColorSchemeDeclaredPerTheme() throws ConfigurationException {
		String css = css(service(LIGHT_AND_DARK));

		assertTrue(css, css.contains(":root, " + selector("light") + "{"));
		assertTrue(css, block(css, "light").startsWith("color-scheme:light;"));
		assertTrue(css, block(css, "dark").startsWith("color-scheme:dark;"));
	}

	/**
	 * A theme's block carries its own tokens as well as the ones it inherits.
	 */
	public void testInheritedTokens() throws ConfigurationException {
		String dark = block(css(service(LIGHT_AND_DARK)), "dark");

		assertTrue(dark, dark.contains("--background:#000000;"));
		assertTrue(dark, dark.contains("--text:#000000;"));
	}

	/**
	 * A theme declaring no scheme takes the one of the theme it extends.
	 */
	public void testInheritedColorScheme() throws ConfigurationException {
		UIThemeService service = service(DARK_AND_CHILD);

		assertEquals(ColorScheme.DARK, theme(service, "night-contrast").getColorScheme());
		assertTrue(block(css(service), "night-contrast").startsWith("color-scheme:dark;"));
	}

	/**
	 * The marked theme answers its scheme; a scheme no theme is marked for is answered by the
	 * default theme.
	 */
	public void testSystemThemes() throws ConfigurationException {
		UIThemeService service = service(LIGHT_AND_DARK);

		assertEquals("dark", service.getSystemTheme(ColorScheme.DARK).getId());
		assertEquals("light", service.getSystemTheme(ColorScheme.LIGHT).getId());
		assertTrue(service.offersSystemThemes());
	}

	/**
	 * With a single theme, both appearance preferences are answered by it, so following the
	 * operating system offers nothing.
	 */
	public void testSingleThemeOffersNoSystemThemes() throws ConfigurationException {
		UIThemeService service = service(SINGLE);

		assertEquals("light", service.getSystemTheme(ColorScheme.DARK).getId());
		assertEquals("light", service.getSystemTheme(ColorScheme.LIGHT).getId());
		assertFalse(service.offersSystemThemes());
	}

	/**
	 * A second theme claiming the same appearance preference is a configuration error.
	 */
	public void testAmbiguousSystemDefault() throws ConfigurationException {
		BufferingProtocol log = new BufferingProtocol();
		create(AMBIGUOUS_SYSTEM_DEFAULT, log);

		List<String> errors = log.getErrors();
		assertEquals("Expected a single error, got: " + errors, 1, errors.size());
		String error = errors.get(0);
		assertTrue(error, error.contains("dark") && error.contains("midnight"));
	}

	/**
	 * The script hands the client the ids of the themes answering the two appearance preferences,
	 * through the documented global object.
	 */
	public void testThemeScript() throws ConfigurationException {
		String script = script(service(LIGHT_AND_DARK));

		assertTrue(script, script.contains("window." + UIThemeService.CLIENT_API + " = api;"));
		assertTrue(script, script.contains(UIThemeService.FOLLOW_SYSTEM_FUNCTION + ": function()"));
		assertTrue(script, script.contains(UIThemeService.SELECT_FUNCTION + ": function(id)"));
		assertTrue(script, script.contains("'light'"));
		assertTrue(script, script.contains("'dark'"));
		assertTrue(script, script.contains("'" + UIThemeService.THEME_ATTRIBUTE + "'"));
		assertTrue(script, script.contains("'" + UIThemeService.THEME_MODE_ATTRIBUTE + "'"));
		assertTrue(script, script.contains("'" + UIThemeService.SYSTEM_MODE + "'"));
		assertTrue(script,
			script.contains("'(prefers-color-scheme: " + ColorScheme.DARK.cssKeyword() + ")'"));
	}

	/**
	 * The script names the design system's mode and density on the {@code html} element: the mode
	 * follows the color scheme of the theme in effect, the density is set once to its default.
	 */
	public void testThemeScriptSetsDesignSystemModeAndDensity() throws ConfigurationException {
		String script = script(service(LIGHT_AND_DARK));

		assertTrue(script, script.contains("'" + UIThemeService.DS_MODE_ATTRIBUTE + "'"));
		assertTrue(script, script.contains("'" + UIThemeService.DS_DENSITY_ATTRIBUTE + "'"));
		assertTrue(script, script.contains("'" + UIThemeService.DS_DENSITY_NORMAL + "'"));
		assertTrue(script, script.contains("var modes = {"));
		assertTrue(script, script.contains("'dark': 'dark'"));
		assertTrue(script, script.contains("modes[id] || 'light'"));
	}

	/**
	 * The theme representing a color scheme while the page is held in that mode is the theme
	 * answering the operating system's preference for it, if it has that scheme.
	 */
	public void testModeThemes() throws ConfigurationException {
		UIThemeService service = service(LIGHT_AND_DARK);

		assertEquals("light", service.getModeTheme(ColorScheme.LIGHT).getId());
		assertEquals("dark", service.getModeTheme(ColorScheme.DARK).getId());
	}

	/**
	 * A scheme the system theme does not have is represented by the first theme of that scheme, a
	 * scheme no theme has by none.
	 */
	public void testModeThemeOfUnansweredScheme() throws ConfigurationException {
		UIThemeService service = service(DARK_AND_CHILD);

		// The default theme answers the light preference, but is dark.
		assertEquals("night", service.getSystemTheme(ColorScheme.LIGHT).getId());
		assertNull(service.getModeTheme(ColorScheme.LIGHT));
		assertEquals("night", service.getModeTheme(ColorScheme.DARK).getId());
	}

	/**
	 * The script offers holding the page in one appearance mode, with the theme representing each
	 * scheme meanwhile, and leaves out a scheme no theme has.
	 */
	public void testThemeScriptLockMode() throws ConfigurationException {
		String script = script(service(LIGHT_AND_DARK));

		assertTrue(script, script.contains(UIThemeService.LOCK_MODE_FUNCTION + ": function(mode)"));
		assertTrue(script, script.contains("var modeThemes = {"));
		assertTrue(script, script.contains("'light': 'light','dark': 'dark'"));
		assertTrue(script, script.contains("locked || modes[id] || 'light'"));

		String darkOnly = script(service(DARK_AND_CHILD));
		assertTrue(darkOnly, darkOnly.contains("var modeThemes = {'dark': 'night'};"));
	}

	/**
	 * Nothing is selected without a personal configuration to select it in.
	 */
	public void testNoSelectionWithoutPersonalConfiguration() throws ConfigurationException {
		assertNull(service(LIGHT_AND_DARK).getSelectedThemeId());
	}

	/**
	 * The themes extending an abstract one inherit its tokens, while the abstract theme itself is
	 * not selectable.
	 */
	public void testAbstractThemeIsInheritedButNotSelectable() throws ConfigurationException {
		UIThemeService service = service(ABSTRACT_BASE);

		assertTrue(theme(service, "base").isAbstract());
		assertEquals("#000000", theme(service, "light").getTokens().get("text"));
		assertEquals("#0000ff", theme(service, "light").getTokens().get("accent"));
		assertEquals("#000000", theme(service, "dark").getTokens().get("background"));
		assertEquals("#000000", theme(service, "dark").getTokens().get("text"));

		assertEquals(List.of("light", "dark"), ids(service.getSelectableThemes()));
		assertEquals(List.of("base", "light", "dark"), ids(service.getThemes()));
		assertFalse(service.isSelectable("base"));
		assertTrue(service.isSelectable("light"));
		assertFalse(service.isSelectable("undefined"));
	}

	/**
	 * An abstract theme is put into effect nowhere, so neither the stylesheet nor the script names
	 * it, while its tokens are part of the blocks of the themes extending it.
	 */
	public void testAbstractThemeNotEmitted() throws ConfigurationException {
		UIThemeService service = service(ABSTRACT_BASE);
		String css = css(service);

		assertFalse(css, css.contains(selector("base")));
		assertTrue(css, block(css, "light").contains("--text:#000000;"));
		String script = script(service);
		assertFalse(script, script.contains("'base'"));
	}

	/**
	 * An abstract theme never represents a color scheme while the page is held in that mode, so a
	 * scheme only an abstract theme has is represented by none.
	 */
	public void testAbstractThemeIsNoModeTheme() throws ConfigurationException {
		UIThemeService service = service(ABSTRACT_DARK_BASE);

		assertEquals("light", service.getModeTheme(ColorScheme.LIGHT).getId());
		assertNull(service.getModeTheme(ColorScheme.DARK));
		String script = script(service);
		assertTrue(script, script.contains("var modeThemes = {'light': 'light'};"));
	}

	/**
	 * An abstract theme cannot be the default theme.
	 */
	public void testAbstractDefaultTheme() throws ConfigurationException {
		BufferingProtocol log = new BufferingProtocol();
		create(ABSTRACT_DEFAULT, log);

		List<String> errors = log.getErrors();
		assertEquals("Expected a single error, got: " + errors, 1, errors.size());
		String error = errors.get(0);
		assertTrue(error, error.contains("base") && error.contains("abstract"));
	}

	/**
	 * An abstract theme cannot answer the operating system's preference.
	 */
	public void testAbstractSystemDefault() throws ConfigurationException {
		BufferingProtocol log = new BufferingProtocol();
		UIThemeService service = create(ABSTRACT_SYSTEM_DEFAULT, log);

		List<String> errors = log.getErrors();
		assertEquals("Expected a single error, got: " + errors, 1, errors.size());
		String error = errors.get(0);
		assertTrue(error, error.contains("base") && error.contains("abstract"));
		assertEquals("light", service.getSystemTheme(ColorScheme.DARK).getId());
	}

	private static List<String> ids(Collection<UITheme> themes) {
		List<String> result = new ArrayList<>();
		for (UITheme theme : themes) {
			result.add(theme.getId());
		}
		return result;
	}

	private static String config(String defaultTheme, String themes) {
		return "<config class='" + UIThemeService.class.getName() + "' default-theme='" + defaultTheme + "'>"
			+ "<themes>" + themes + "</themes>"
			+ "</config>";
	}

	private UIThemeService service(String xml) throws ConfigurationException {
		BufferingProtocol log = new BufferingProtocol();
		UIThemeService result = create(xml, log);
		assertEquals("Unexpected configuration errors.", List.of(), log.getErrors());
		return result;
	}

	private UIThemeService create(String xml, Log log) throws ConfigurationException {
		UIThemeService.Config config = TypedConfiguration.parse("config", UIThemeService.Config.class,
			CharacterContents.newContent(xml));
		return new UIThemeService(new DefaultInstantiationContext(log), config);
	}

	private static UITheme theme(UIThemeService service, String id) {
		for (UITheme theme : service.getThemes()) {
			if (id.equals(theme.getId())) {
				return theme;
			}
		}
		fail("No theme '" + id + "' configured.");
		return null;
	}

	private static String css(UIThemeService service) {
		TagWriter out = new TagWriter();
		try {
			service.writeThemeStyles(out);
		} catch (IOException ex) {
			throw new AssertionError(ex);
		}
		return out.toString();
	}

	private static String script(UIThemeService service) {
		TagWriter out = new TagWriter();
		try {
			service.writeThemeScript(out);
		} catch (IOException ex) {
			throw new AssertionError(ex);
		}
		return out.toString();
	}

	private static String selector(String themeId) {
		return "[" + UIThemeService.THEME_ATTRIBUTE + "=\"" + themeId + "\"]";
	}

	private static String block(String css, String themeId) {
		String start = selector(themeId) + "{";
		int begin = css.indexOf(start);
		assertTrue("No block for theme '" + themeId + "' in: " + css, begin >= 0);
		begin += start.length();
		int end = css.indexOf('}', begin);
		assertTrue("Unterminated block for theme '" + themeId + "' in: " + css, end >= 0);
		return css.substring(begin, end);
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module, used to resolve the tag names of the
	 * configured design tokens.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestUIThemeService.class, TypeIndex.Module.INSTANCE);
	}

}
