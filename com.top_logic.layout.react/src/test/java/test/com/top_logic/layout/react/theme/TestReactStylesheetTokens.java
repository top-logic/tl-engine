/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.theme;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;

import com.top_logic.layout.react.theme.UITheme;
import com.top_logic.layout.react.theme.UIThemeService;

/**
 * Tests that the stylesheets of the React controls take every custom property and every corner
 * rounding from the design tokens of the shipped {@link UIThemeService} themes.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestReactStylesheetTokens extends AbstractStylesheetTokenTest {

	/** The stylesheets audited. */
	private static final List<String> STYLESHEETS =
		List.of("/style/tlReactBase.css", "/style/tlReactControls.css");

	/**
	 * Selectors rounding for a reason of their own: an icon is drawn out of a box whose corners
	 * belong to the glyph, not to a control.
	 */
	private static final Set<String> ICON_GLYPHS =
		Set.of(".tlAudioRecorder__icon--stop", ".tlPhotoCapture__cameraIcon");

	@Override
	protected List<String> stylesheets() {
		return STYLESHEETS;
	}

	@Override
	protected Set<String> allowedLiteralSelectors() {
		return ICON_GLYPHS;
	}

	/**
	 * The theme configuration is read through the typed configuration, so a token declared in the
	 * {@link #DEFAULT_THEME} theme reaches the audit with its value.
	 */
	public void testThemeTokensAreResolved() throws Exception {
		assertEquals("0.25rem", ThemeTokenAudit.themeTokens(THEME_CONFIG, DEFAULT_THEME).get("corner-radius"));
	}

	/**
	 * A theme inherits the tokens of the theme it extends, so a sheet audited against the dark
	 * theme sees the same token names.
	 *
	 * @see UITheme#getTokens()
	 */
	public void testInheritedThemeIsComplete() throws Exception {
		Set<String> light = ThemeTokenAudit.themeTokens(THEME_CONFIG, DEFAULT_THEME).keySet();
		Set<String> dark = ThemeTokenAudit.themeTokens(THEME_CONFIG, "dark").keySet();

		assertTrue("Tokens missing from the dark theme: " + difference(light, dark), dark.containsAll(light));
	}

	/**
	 * The audit reports an undefined reference and a literal rounding, and nothing else.
	 */
	public void testAuditReportsBothProblemKinds() {
		String css = ""
			+ ".defined {\n"
			+ "\tcolor: var(--text-primary);\n"
			+ "\tborder-radius: var(--corner-radius);\n"
			+ "}\n"
			+ ".local {\n"
			+ "\t--own: 1px;\n"
			+ "\tborder-width: var(--own);\n"
			+ "\tborder-radius: 50%;\n"
			+ "}\n"
			+ ".broken {\n"
			+ "\tcolor: var(--no-such-token);\n"
			+ "\tborder-radius: 3px;\n"
			+ "}\n";

		List<String> problems = ThemeTokenAudit.audit(Set.of("text-primary", "corner-radius"), css);

		assertEquals(problems.toString(), 2, problems.size());
		assertTrue(problems.toString(), problems.get(0).contains("--no-such-token"));
		assertTrue(problems.toString(), problems.get(1).contains("3px") && problems.get(1).contains(".broken"));
	}

	/**
	 * A literal rounding of an allowed selector is accepted, and a comment holding one is not read
	 * as a declaration.
	 */
	public void testAuditAllowList() {
		String css = ""
			+ "/* border-radius: 4px; a comment is no declaration. */\n"
			+ ".glyph {\n"
			+ "\tborder-radius: 2px;\n"
			+ "}\n";

		assertEquals(List.of(), ThemeTokenAudit.audit(Set.of(), css, Set.of(".glyph")));
		assertEquals(1, ThemeTokenAudit.audit(Set.of(), css).size());
	}

	private static Set<String> difference(Set<String> expected, Set<String> actual) {
		Set<String> result = new HashSet<>(expected);
		result.removeAll(actual);
		return result;
	}

	/**
	 * @see AbstractStylesheetTokenTest#suite(Class)
	 */
	public static Test suite() {
		return AbstractStylesheetTokenTest.suite(TestReactStylesheetTokens.class);
	}

}
