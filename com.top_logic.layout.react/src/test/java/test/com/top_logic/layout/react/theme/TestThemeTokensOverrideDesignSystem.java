/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.theme;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.react.resource.ClientResources;
import com.top_logic.layout.react.theme.UIThemeService;

/**
 * Tests that a token of a {@link UIThemeService} theme wins over the declaration of the same
 * custom property in the sheets of the design system, in the light and in the dark mode.
 *
 * <p>
 * The theme blocks and the sheets of the design system are in the same cascade layer
 * {@value ClientResources#ENGINE_LAYER}, and the theme blocks come first in the page. So the
 * specificity of the selectors and their order decide. The test resolves this part of the cascade
 * for the {@code html} element: it reads the shipped sheets, matches their selectors and the
 * selectors of the theme blocks against the attributes of the {@code html} element, and takes the
 * declaration with the highest specificity, the later one of two with the same specificity.
 * </p>
 */
public class TestThemeTokensOverrideDesignSystem extends TestCase {

	/** The sheets of the design system that declare custom properties, in the order of the page. */
	private static final List<String> DESIGN_SYSTEM_SHEETS =
		List.of("/style/tl-design-system/primitives.css", "/style/tl-design-system/tokens.css");

	/** A color role that the design system declares for the light and for the dark mode. */
	private static final String COLOR_ROLE = "tl-text-primary";

	/** A type role that the design system declares once, for both modes. */
	private static final String TYPE_ROLE = "tl-font-size-body";

	private static final String COLOR_VALUE = "#123456";

	private static final String TYPE_VALUE = "17px";

	/** A light default theme and a dark theme extending it, both naming roles of the design system. */
	private static final String THEMES = "<config class='" + UIThemeService.class.getName()
		+ "' default-theme='light'>"
		+ "<themes>"
		+ "<theme name='light'>"
		+ "<color name='background' value='#ffffff'/>"
		+ "<color name='" + COLOR_ROLE + "' value='" + COLOR_VALUE + "'/>"
		+ "<length name='" + TYPE_ROLE + "' value='" + TYPE_VALUE + "'/>"
		+ "</theme>"
		+ "<theme name='dark' extends='light' color-scheme='dark'>"
		+ "<color name='background' value='#000000'/>"
		+ "</theme>"
		+ "</themes>"
		+ "</config>";

	private static final Pattern COMMENT = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);

	private static final Pattern RULE = Pattern.compile("([^{}]+)\\{([^{}]*)\\}");

	private static final Pattern DECLARATION = Pattern.compile("--([\\w-]+)\\s*:\\s*([^;]+);");

	/**
	 * One part of a compound selector that can match the {@code html} element. Each of them counts
	 * as one class in the specificity.
	 */
	private static final Pattern SIMPLE_SELECTOR =
		Pattern.compile(":root|\\[([\\w-]+)(?:=\"([^\"]*)\")?\\]");

	/**
	 * A role that a theme names wins over the role of the design system, for each theme and in the
	 * light and in the dark mode of the design system.
	 */
	public void testThemeRoleWins() throws Exception {
		List<Rule> rules = new ArrayList<>(themeRules(service(THEMES)));
		for (String sheet : DESIGN_SYSTEM_SHEETS) {
			rules.addAll(rules(ThemeTokenAudit.stylesheet(sheet)));
		}

		for (String theme : List.of("light", "dark")) {
			for (String mode : List.of("light", "dark")) {
				Map<String, String> html = Map.of(
					UIThemeService.THEME_ATTRIBUTE, theme,
					UIThemeService.DS_MODE_ATTRIBUTE, mode);
				String state = "Theme '" + theme + "', mode '" + mode + "'";

				assertEquals(state, COLOR_VALUE, value(rules, html, COLOR_ROLE));
				assertEquals(state, TYPE_VALUE, value(rules, html, TYPE_ROLE));
			}
		}
	}

	/**
	 * The value of the custom property with the given name that the {@code html} element with the
	 * given attributes gets from the given rules of one cascade layer, in the order of the page.
	 */
	private static String value(List<Rule> rules, Map<String, String> html, String property) {
		String result = null;
		int bestSpecificity = -1;
		for (Rule rule : rules) {
			String value = rule._declarations.get(property);
			if (value == null) {
				continue;
			}
			for (String selector : rule._selectors) {
				int specificity = specificity(selector, html);
				if (specificity >= bestSpecificity) {
					bestSpecificity = specificity;
					result = value;
				}
			}
		}
		return result;
	}

	/**
	 * The specificity of the given selector, or {@code -1} if it does not match the {@code html}
	 * element with the given attributes.
	 */
	private static int specificity(String selector, Map<String, String> html) {
		Matcher part = SIMPLE_SELECTOR.matcher(selector);
		int end = 0;
		int result = 0;
		boolean matches = true;
		while (part.lookingAt()) {
			String attribute = part.group(1);
			if (attribute != null) {
				String actual = html.get(attribute);
				String expected = part.group(2);
				matches &= actual != null && (expected == null || expected.equals(actual));
			}
			result++;
			end = part.end();
			part.region(end, selector.length());
		}
		assertEquals("Selector not supported by the test: " + selector, selector.length(), end);
		return matches ? result : -1;
	}

	private static List<Rule> themeRules(UIThemeService service) throws IOException {
		TagWriter out = new TagWriter();
		service.writeThemeStyles(out);
		String css = out.toString();
		String layerStart = "@layer " + ClientResources.ENGINE_LAYER + "{";
		int begin = css.indexOf(layerStart);
		int end = css.lastIndexOf("}</style>");
		assertTrue(css, begin >= 0 && end > begin);
		return rules(css.substring(begin + layerStart.length(), end));
	}

	private static List<Rule> rules(String css) {
		List<Rule> result = new ArrayList<>();
		Matcher rule = RULE.matcher(COMMENT.matcher(css).replaceAll(""));
		while (rule.find()) {
			List<String> selectors = new ArrayList<>();
			for (String selector : rule.group(1).split(",")) {
				selectors.add(selector.trim());
			}
			Map<String, String> declarations = new HashMap<>();
			Matcher declaration = DECLARATION.matcher(rule.group(2));
			while (declaration.find()) {
				declarations.put(declaration.group(1), declaration.group(2).trim());
			}
			result.add(new Rule(selectors, declarations));
		}
		return result;
	}

	private static UIThemeService service(String xml) throws ConfigurationException {
		BufferingProtocol log = new BufferingProtocol();
		UIThemeService.Config config = TypedConfiguration.parse("config", UIThemeService.Config.class,
			CharacterContents.newContent(xml));
		UIThemeService result = new UIThemeService(new DefaultInstantiationContext(log), config);
		assertEquals("Unexpected configuration errors.", List.of(), log.getErrors());
		return result;
	}

	/** A style rule: its selector list and its custom property declarations. */
	private static class Rule {

		final List<String> _selectors;

		final Map<String, String> _declarations;

		Rule(List<String> selectors, Map<String, String> declarations) {
			_selectors = selectors;
			_declarations = declarations;
		}

	}

	/**
	 * Test suite reading the shipped sheets of the design system from the web application
	 * resources, with the {@link TypeIndex} resolving the tag names of the design tokens.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestThemeTokensOverrideDesignSystem.class, TypeIndex.Module.INSTANCE));
	}

}
