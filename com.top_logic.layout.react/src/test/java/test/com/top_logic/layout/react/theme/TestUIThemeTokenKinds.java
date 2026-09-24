/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.theme;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.gui.DesignTokenKind;
import com.top_logic.layout.react.theme.RefToken;
import com.top_logic.layout.react.theme.UITheme;
import com.top_logic.layout.react.theme.UIThemeService;

/**
 * Tests the {@link DesignTokenKind kinds} of the design tokens of the shipped themes.
 *
 * <p>
 * The kinds are read from the theme configuration the application ships, so a token that changes
 * its kind - or a {@link RefToken} that ends up naming no token at all - is reported here.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestUIThemeTokenKinds extends TestCase {

	/** The shipped application configuration declaring the themes of the React UI. */
	private static final String THEME_CONFIG = AbstractStylesheetTokenTest.THEME_CONFIG;

	/** The theme every other theme extends. */
	private static final String DEFAULT_THEME = AbstractStylesheetTokenTest.DEFAULT_THEME;

	/** The theme overriding the {@link #DEFAULT_THEME} for a dark appearance. */
	private static final String DARK_THEME = "dark";

	/** A theme whose only token names a token nothing declares. */
	private static final String DANGLING_REFERENCE = config(
		"<theme name='light'>"
			+ "<color name='text-primary' value='#000000'/>"
			+ "<ref name='text-muted' ref='text-nowhere'/>"
			+ "</theme>");

	/** A theme with two tokens naming each other. */
	private static final String CYCLIC_REFERENCE = config(
		"<theme name='light'>"
			+ "<ref name='here' ref='there'/>"
			+ "<ref name='there' ref='here'/>"
			+ "</theme>");

	/**
	 * A color token holds a {@link DesignTokenKind#COLOR}, for the tokens marking the supported
	 * states as well.
	 */
	public void testColorTokens() throws Exception {
		Map<String, DesignTokenKind> kinds = kinds(DEFAULT_THEME);

		assertEquals(DesignTokenKind.COLOR, kinds.get("support-success"));
		assertEquals(DesignTokenKind.COLOR, kinds.get("support-warning"));
		assertEquals(DesignTokenKind.COLOR, kinds.get("text-primary"));
	}

	/**
	 * A spacing is a {@link DesignTokenKind#LENGTH}, and therefore no name a color annotation may
	 * choose.
	 */
	public void testLengthTokenIsNoColor() throws Exception {
		Map<String, DesignTokenKind> kinds = kinds(DEFAULT_THEME);

		assertEquals(DesignTokenKind.LENGTH, kinds.get("spacing-01"));
		assertFalse("A spacing is no name a color annotation may choose.",
			names(kinds, DesignTokenKind.COLOR).contains("spacing-01"));
	}

	/**
	 * A shadow is a raw CSS value, i.e. a {@link DesignTokenKind#TEXT} token.
	 */
	public void testTextToken() throws Exception {
		assertEquals(DesignTokenKind.TEXT, kinds(DEFAULT_THEME).get("shadow-menu"));
	}

	/**
	 * A token naming a color token is a color token itself, in the theme declaring it as well as in
	 * a theme inheriting it.
	 */
	public void testReferencedKind() throws Exception {
		Map<String, DesignTokenKind> kinds = kinds(DEFAULT_THEME);

		assertEquals(DesignTokenKind.COLOR, kinds.get("border-color"));
		assertEquals(DesignTokenKind.COLOR, kinds.get("text-muted"));
		assertEquals(DesignTokenKind.COLOR, kinds(DARK_THEME).get("border-color"));
	}

	/**
	 * Every token has a kind, i.e. no reference of a shipped theme is left unresolved.
	 */
	public void testEveryTokenHasAKind() throws Exception {
		for (String themeId : List.of(DEFAULT_THEME, DARK_THEME)) {
			UITheme theme = ThemeTokenAudit.theme(THEME_CONFIG, themeId);
			Set<String> missing = new HashSet<>(theme.getTokens().keySet());
			missing.removeAll(theme.getTokenKinds().keySet());

			assertEquals("Tokens without a kind in theme '" + themeId + "'.", Set.of(), missing);
		}
	}

	/**
	 * The dark theme overrides tokens of the theme it extends and declares none of its own, so it
	 * contributes no name to the vocabulary.
	 */
	public void testDarkThemeOnlyOverrides() throws Exception {
		Set<String> names = ThemeTokenAudit.themeTokens(THEME_CONFIG, DEFAULT_THEME).keySet();
		Set<String> darkNames = new HashSet<>(ThemeTokenAudit.themeTokens(THEME_CONFIG, DARK_THEME).keySet());
		darkNames.removeAll(names);

		assertEquals("Tokens declared by the dark theme alone.", Set.of(), darkNames);
	}

	/**
	 * A token naming a token that is not declared is reported, instead of quietly resolving to
	 * nothing in the browser.
	 */
	public void testDanglingReference() throws ConfigurationException {
		List<String> errors = errors(DANGLING_REFERENCE);

		assertEquals("Expected a single error, got: " + errors, 1, errors.size());
		String error = errors.get(0);
		assertTrue(error, error.contains("text-muted") && error.contains("text-nowhere"));
	}

	/**
	 * Tokens naming each other are reported rather than followed.
	 */
	public void testCyclicReference() throws ConfigurationException {
		List<String> errors = errors(CYCLIC_REFERENCE);

		assertFalse("A cycle of references must be reported.", errors.isEmpty());
		for (String error : errors) {
			assertTrue(error, error.contains("here") && error.contains("there"));
		}
	}

	private static Map<String, DesignTokenKind> kinds(String themeId) throws Exception {
		return ThemeTokenAudit.theme(THEME_CONFIG, themeId).getTokenKinds();
	}

	private static Set<String> names(Map<String, DesignTokenKind> kinds, DesignTokenKind kind) {
		Set<String> result = new HashSet<>();
		for (Map.Entry<String, DesignTokenKind> token : kinds.entrySet()) {
			if (token.getValue() == kind) {
				result.add(token.getKey());
			}
		}
		return result;
	}

	private static String config(String themes) {
		return "<config class='" + UIThemeService.class.getName() + "' default-theme='light'>"
			+ "<themes>" + themes + "</themes>"
			+ "</config>";
	}

	private static List<String> errors(String xml) throws ConfigurationException {
		UIThemeService.Config config = TypedConfiguration.parse("config", UIThemeService.Config.class,
			CharacterContents.newContent(xml));
		BufferingProtocol log = new BufferingProtocol();
		new UIThemeService(new DefaultInstantiationContext(log), config);
		return log.getErrors();
	}

	/**
	 * Test suite reading the shipped theme configuration from the web application resources, with
	 * the {@link TypeIndex} resolving the tag names of the design tokens.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(ServiceTestSetup.createSetup(TestUIThemeTokenKinds.class,
			TypeIndex.Module.INSTANCE));
	}

}
