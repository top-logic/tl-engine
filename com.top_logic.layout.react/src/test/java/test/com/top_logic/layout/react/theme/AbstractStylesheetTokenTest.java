/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.theme;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.react.theme.UIThemeService;

/**
 * Audits the stylesheets of a module against the design tokens of the shipped
 * {@link UIThemeService} themes.
 *
 * <p>
 * A subclass supplies the sheets it is responsible for in {@link #stylesheets()}, and a selector
 * that rounds for a reason of its own in {@link #allowedLiteralSelectors()}. Its own
 * {@code suite()} is a single call to {@link #suite(Class)}:
 * </p>
 *
 * <pre>
 * public static Test suite() {
 * 	return AbstractStylesheetTokenTest.suite(TestMyStylesheetTokens.class);
 * }
 * </pre>
 *
 * <p>
 * The audit itself, and what it reports, is {@link ThemeTokenAudit}. The sheets and the theme
 * configuration ({@link #THEME_CONFIG}) are read from the web application resources, so a module
 * audits the sheets as the application serves them.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractStylesheetTokenTest extends TestCase {

	/** The application configuration declaring the themes of the React UI. */
	protected static final String THEME_CONFIG = "/WEB-INF/conf/tl-react-theme.config.xml";

	/** The theme the audited stylesheets are written against. */
	protected static final String DEFAULT_THEME = "default";

	/**
	 * The stylesheets to audit, under the resource paths they are served from.
	 */
	protected abstract List<String> stylesheets();

	/**
	 * Selector fragments whose corner rounding may be a literal length.
	 *
	 * @see ThemeTokenAudit#audit(Set, String, Set)
	 */
	protected Set<String> allowedLiteralSelectors() {
		return Collections.emptySet();
	}

	/**
	 * Every {@code var()} of the {@link #stylesheets()} answers a token of the
	 * {@link #DEFAULT_THEME} theme or a declaration of the sheet itself, and every corner rounding
	 * reads a radius token.
	 */
	public void testStylesheetsKeepTheTokenContract() throws Exception {
		Set<String> tokens = ThemeTokenAudit.themeTokens(THEME_CONFIG, DEFAULT_THEME).keySet();
		Set<String> allowed = allowedLiteralSelectors();

		for (String stylesheet : stylesheets()) {
			List<String> problems = ThemeTokenAudit.audit(tokens, ThemeTokenAudit.stylesheet(stylesheet), allowed);

			assertEquals(stylesheet + ":\n" + String.join("\n", problems), List.of(), problems);
		}
	}

	/**
	 * Test suite reading the shipped theme configuration and the shipped stylesheets from the web
	 * application resources, with the {@link TypeIndex} resolving the tag names of the design
	 * tokens.
	 *
	 * @param test
	 *        The concrete test to run.
	 */
	protected static Test suite(Class<? extends AbstractStylesheetTokenTest> test) {
		return ModuleTestSetup.setupModule(ServiceTestSetup.createSetup(test, TypeIndex.Module.INSTANCE));
	}

}
