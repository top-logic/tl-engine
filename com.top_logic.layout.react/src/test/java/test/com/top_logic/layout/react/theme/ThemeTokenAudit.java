/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.theme;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.layout.react.theme.UITheme;
import com.top_logic.layout.react.theme.UIThemeService;

/**
 * Audit of a stylesheet against the design tokens of a {@link UITheme}.
 *
 * <p>
 * A stylesheet of the React UI takes every colour, spacing, rounding and shadow from a theme token,
 * written as a plain {@code var(--name)} without a fallback. This audit reports the two ways that
 * contract is broken: a {@code var()} reference no theme token and no declaration of the sheet
 * itself answers, and a corner rounding written as a literal length instead of one of the two
 * radius tokens.
 * </p>
 *
 * <p>
 * A rounding that names a shape rather than a corner radius - a circle, a pill, a squared-off
 * corner - stays literal and is accepted, see {@link #SHAPE_RADII}. A selector that rounds for a
 * reason of its own (an icon glyph drawn out of a box, say) is passed in the allow-list of the
 * audit.
 * </p>
 *
 * <p>
 * The audit is written against a theme configuration and a stylesheet handed to it, so an
 * application module runs it over its own sheets and its own theme, through this module's test-jar.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ThemeTokenAudit {

	/**
	 * Literal rounding values naming a shape: no rounding at all, a circle, and the two spellings
	 * of a pill.
	 *
	 * <p>
	 * A shape must survive a theme that squares every corner off, so it is written literally
	 * instead of reading a radius token.
	 * </p>
	 */
	public static final Set<String> SHAPE_RADII = Set.of("0", "0px", "50%", "999px", "9999px");

	/**
	 * Attribute naming the service a {@code <config>} element of an application configuration
	 * configures.
	 */
	private static final String SERVICE_CLASS_ATTRIBUTE = TypedRuntimeModule.ModuleConfiguration.SERVICE_CLASS;

	/** A {@code var(--name)} reference, capturing the name without the leading dashes. */
	private static final Pattern VAR_REFERENCE = Pattern.compile("var\\(\\s*--([A-Za-z0-9_-]+)");

	/** A custom property declaration, capturing the name without the leading dashes. */
	private static final Pattern PROPERTY_DECLARATION = Pattern.compile("--([A-Za-z0-9_-]+)\\s*:");

	/** A {@code border-radius} declaration or one of its corner long-hands. */
	private static final Pattern RADIUS_DECLARATION =
		Pattern.compile("(?<![-A-Za-z0-9_])border(?:-[a-z]+)*-radius\\s*:\\s*([^;}]*)");

	/** A CSS comment, spanning lines. */
	private static final Pattern COMMENT = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);

	/** Separator of the components of a multi-value rounding, whitespace or the radii slash. */
	private static final Pattern RADIUS_COMPONENT_SEPARATOR = Pattern.compile("[\\s/]+");

	/**
	 * Reports every violation of the token contract in the given stylesheet.
	 *
	 * @param tokens
	 *        The names (without the leading dashes) of the design tokens of the theme the sheet is
	 *        written against, e.g. the keys of {@link UITheme#getTokens()}.
	 * @param css
	 *        The text of the stylesheet to audit.
	 * @return One message per problem, empty if the sheet keeps the contract.
	 */
	public static List<String> audit(Set<String> tokens, String css) {
		return audit(tokens, css, Collections.emptySet());
	}

	/**
	 * Reports every violation of the token contract in the given stylesheet.
	 *
	 * @param tokens
	 *        The names (without the leading dashes) of the design tokens of the theme the sheet is
	 *        written against, e.g. the keys of {@link UITheme#getTokens()}.
	 * @param css
	 *        The text of the stylesheet to audit.
	 * @param allowedLiteralSelectors
	 *        Selector fragments whose rounding may be a literal length. A literal rounding is
	 *        accepted if the selector of its rule contains one of these.
	 * @return One message per problem, empty if the sheet keeps the contract.
	 */
	public static List<String> audit(Set<String> tokens, String css, Set<String> allowedLiteralSelectors) {
		String stripped = stripComments(css);
		List<String> problems = new ArrayList<>();
		reportUndefinedReferences(tokens, stripped, problems);
		reportLiteralRadii(stripped, allowedLiteralSelectors, problems);
		return problems;
	}

	/**
	 * The resolved design tokens of one theme of a {@link UIThemeService} configuration.
	 *
	 * @param resource
	 *        Web application resource path of the application configuration declaring the themes,
	 *        e.g. {@code /WEB-INF/conf/tl-react-theme.config.xml}.
	 * @param themeId
	 *        The id of the theme to resolve.
	 * @return The token values, keyed by token name without the leading dashes.
	 * @throws IOException
	 *         If the resource cannot be read.
	 * @throws ConfigurationException
	 *         If the configuration cannot be parsed, or declares no such theme.
	 */
	public static Map<String, String> themeTokens(String resource, String themeId)
			throws IOException, ConfigurationException {
		UIThemeService service = themeService(resource);
		for (UITheme theme : service.getThemes()) {
			if (themeId.equals(theme.getId())) {
				return theme.getTokens();
			}
		}
		throw new ConfigurationException("No theme '" + themeId + "' configured in '" + resource + "'.");
	}

	/**
	 * The text of a stylesheet of the web application.
	 *
	 * @param resource
	 *        Web application resource path of the stylesheet, e.g. {@code /style/tlReactBase.css}.
	 * @return The stylesheet's text, read as UTF-8.
	 * @throws IOException
	 *         If the resource cannot be read.
	 */
	public static String stylesheet(String resource) throws IOException {
		try (InputStream in = FileManager.getInstance().getStream(resource)) {
			return StreamUtilities.readAllFromStream(in, StandardCharsets.UTF_8);
		}
	}

	private static UIThemeService themeService(String resource) throws IOException, ConfigurationException {
		Element instance = serviceConfig(resource);
		UIThemeService.Config config = TypedConfiguration.parse(instance.getTagName(), UIThemeService.Config.class,
			CharacterContents.newContent(DOMUtil.toString(instance), resource));

		BufferingProtocol log = new BufferingProtocol();
		UIThemeService result = new UIThemeService(new DefaultInstantiationContext(log), config);
		if (log.hasErrors()) {
			throw new ConfigurationException("Invalid theme configuration in '" + resource + "': " + log.getErrors());
		}
		return result;
	}

	/**
	 * The element configuring the {@link UIThemeService} instance in the given application
	 * configuration.
	 */
	private static Element serviceConfig(String resource) throws IOException, ConfigurationException {
		Document document;
		try {
			document = DOMUtil.parseResource(resource);
		} catch (SAXException ex) {
			throw new IOException("Cannot parse '" + resource + "'.", ex);
		}
		String serviceClass = UIThemeService.class.getName();
		for (Element config : configElements(document)) {
			if (serviceClass.equals(config.getAttribute(SERVICE_CLASS_ATTRIBUTE))) {
				Element instance = DOMUtil.getFirstElementChild(config);
				if (instance == null) {
					throw new ConfigurationException(
						"No service instance configured for '" + serviceClass + "' in '" + resource + "'.");
				}
				return instance;
			}
		}
		throw new ConfigurationException("No configuration of '" + serviceClass + "' in '" + resource + "'.");
	}

	private static List<Element> configElements(Document document) {
		List<Element> result = new ArrayList<>();
		for (Element services : DOMUtil.elements(document.getDocumentElement())) {
			for (Element config : DOMUtil.elements(services)) {
				if (config.hasAttribute(SERVICE_CLASS_ATTRIBUTE)) {
					result.add(config);
				}
			}
		}
		return result;
	}

	private static void reportUndefinedReferences(Set<String> tokens, String css, List<String> problems) {
		Set<String> defined = new HashSet<>(tokens);
		Matcher declaration = PROPERTY_DECLARATION.matcher(css);
		while (declaration.find()) {
			defined.add(declaration.group(1));
		}

		Matcher reference = VAR_REFERENCE.matcher(css);
		while (reference.find()) {
			String name = reference.group(1);
			if (!defined.contains(name)) {
				problems.add("Line " + lineOf(css, reference.start()) + ": reference to '--" + name
					+ "', which is neither a theme token nor declared in the stylesheet.");
			}
		}
	}

	private static void reportLiteralRadii(String css, Set<String> allowedLiteralSelectors, List<String> problems) {
		Matcher declaration = RADIUS_DECLARATION.matcher(css);
		while (declaration.find()) {
			String value = declaration.group(1).trim();
			if (value.contains("var(") || isShape(value)) {
				continue;
			}
			String selector = selectorAt(css, declaration.start());
			if (allowed(selector, allowedLiteralSelectors)) {
				continue;
			}
			problems.add("Line " + lineOf(css, declaration.start()) + ": literal rounding '" + value + "' in '"
				+ selector + "'; use one of the theme's radius tokens.");
		}
	}

	private static boolean isShape(String value) {
		for (String component : RADIUS_COMPONENT_SEPARATOR.split(value)) {
			if (!component.isEmpty() && !SHAPE_RADII.contains(component)) {
				return false;
			}
		}
		return true;
	}

	private static boolean allowed(String selector, Set<String> allowedLiteralSelectors) {
		for (String allowed : allowedLiteralSelectors) {
			if (selector.contains(allowed)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * The selector of the rule the given offset lies in: the text between the last structural
	 * character before the enclosing brace and that brace.
	 */
	private static String selectorAt(String css, int offset) {
		int brace = css.lastIndexOf('{', offset);
		if (brace < 0) {
			return "";
		}
		int start = brace;
		while (start > 0) {
			char before = css.charAt(start - 1);
			if (before == '{' || before == '}' || before == ';') {
				break;
			}
			start--;
		}
		return css.substring(start, brace).trim().replaceAll("\\s+", " ");
	}

	private static int lineOf(String css, int offset) {
		int line = 1;
		for (int n = 0; n < offset; n++) {
			if (css.charAt(n) == '\n') {
				line++;
			}
		}
		return line;
	}

	/**
	 * Replaces every comment with as many newlines as it spanned, so the reported line numbers stay
	 * those of the original sheet.
	 */
	private static String stripComments(String css) {
		StringBuilder result = new StringBuilder();
		Matcher comment = COMMENT.matcher(css);
		int done = 0;
		while (comment.find()) {
			result.append(css, done, comment.start());
			String skipped = comment.group();
			for (int n = 0; n < skipped.length(); n++) {
				if (skipped.charAt(n) == '\n') {
					result.append('\n');
				}
			}
			done = comment.end();
		}
		result.append(css, done, css.length());
		return result.toString();
	}

	/**
	 * Only static access.
	 */
	private ThemeTokenAudit() {
		// Utility class.
	}

}
