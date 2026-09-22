/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.theme;

import java.io.IOException;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.DesignTokenKind;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Registry of the available UI themes and the source of the active theme's design tokens.
 *
 * <p>
 * A theme is a named set of {@link ThemeToken design tokens} (CSS custom properties) with optional
 * inheritance. This service resolves the inheritance, emits every theme as a CSS block scoped by
 * the {@link #THEME_ATTRIBUTE} of the {@code html} element, and keeps the theme a user selected.
 * </p>
 *
 * <p>
 * A user who has selected no theme sees the one answering the operating system's appearance
 * preference. The theme in effect is always the one named by the {@link #THEME_ATTRIBUTE}.
 * </p>
 *
 * @implNote The operating system's preference is evaluated on the client, by the script written by
 *           {@link #writeThemeScript(TagWriter)}.
 */
@Label("UI themes")
public class UIThemeService extends ConfiguredManagedClass<UIThemeService.Config> {

	/** {@link PersonalConfiguration} key storing the user's selected theme id. */
	public static final String PERSONAL_THEME_KEY = "react.theme";

	/** Attribute of the {@code html} element naming the theme in effect. */
	public static final String THEME_ATTRIBUTE = "data-theme";

	/**
	 * Attribute of the {@code html} element marking the page as following the operating system's
	 * appearance preference.
	 *
	 * @see #SYSTEM_MODE
	 */
	public static final String THEME_MODE_ATTRIBUTE = "data-theme-mode";

	/**
	 * Attribute of the {@code html} element naming the appearance mode of the design system,
	 * {@code light} or {@code dark}. Follows the {@link ColorScheme} of the theme in effect; read by
	 * the design system's {@code tokens.css}.
	 */
	public static final String DS_MODE_ATTRIBUTE = "data-tl-mode";

	/**
	 * Attribute of the {@code html} element naming the density of the design system, {@code normal}
	 * or {@code compact}. Set once to {@link #DS_DENSITY_NORMAL} unless the page carries it already.
	 */
	public static final String DS_DENSITY_ATTRIBUTE = "data-tl-density";

	/** Value of {@link #DS_DENSITY_ATTRIBUTE} while nothing has selected a density. */
	public static final String DS_DENSITY_NORMAL = "normal";

	/** Value of {@link #THEME_MODE_ATTRIBUTE} while the page follows the operating system. */
	public static final String SYSTEM_MODE = "system";

	/** Name of the global object through which the client switches the theme. */
	public static final String CLIENT_API = "tlTheme";

	/**
	 * Name of the {@link #CLIENT_API} function that follows the operating system's appearance
	 * preference.
	 */
	public static final String FOLLOW_SYSTEM_FUNCTION = "followSystem";

	/**
	 * Name of the {@link #CLIENT_API} function that puts the theme with a given id into effect.
	 */
	public static final String SELECT_FUNCTION = "select";

	private static final String CSS_TYPE = "text/css";

	/**
	 * Configuration of {@link UIThemeService}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<UIThemeService> {

		/** Configuration name for {@link #getDefaultTheme()}. */
		String DEFAULT_THEME = "default-theme";

		/**
		 * The registered themes, keyed by id, contributed across modules.
		 */
		@Name("themes")
		@Key(NamedConfiguration.NAME_ATTRIBUTE)
		Map<String, UITheme.Config> getThemes();

		/**
		 * Id of the theme in effect wherever nothing else names one: the appearance of a page whose
		 * script did not run, and the answer to an appearance preference of the operating system no
		 * theme is marked for.
		 */
		@Name(DEFAULT_THEME)
		@StringDefault("default")
		String getDefaultTheme();

	}

	private final Map<String, UITheme> _themes;

	private final String _defaultTheme;

	private final Map<ColorScheme, UITheme> _systemThemes;

	/**
	 * Creates a {@link UIThemeService} from configuration.
	 *
	 * @param context
	 *        The instantiation context for error reporting.
	 * @param config
	 *        The service configuration.
	 */
	@CalledByReflection
	public UIThemeService(InstantiationContext context, Config config) {
		super(context, config);
		_themes = resolve(context, config.getThemes());
		_defaultTheme = config.getDefaultTheme();
		if (!_themes.containsKey(_defaultTheme)) {
			context.error("Default theme '" + _defaultTheme + "' is not defined.");
		}
		_systemThemes = systemThemes(context, _themes.values());
	}

	private static Map<ColorScheme, UITheme> systemThemes(InstantiationContext context, Collection<UITheme> themes) {
		Map<ColorScheme, UITheme> result = new EnumMap<>(ColorScheme.class);
		for (UITheme theme : themes) {
			if (!theme.isSystemDefault()) {
				continue;
			}
			ColorScheme scheme = theme.getColorScheme();
			UITheme clash = result.put(scheme, theme);
			if (clash != null) {
				context.error("Themes '" + clash.getId() + "' and '" + theme.getId()
					+ "' both answer the '" + scheme.cssKeyword() + "' preference of the operating system.");
			}
		}
		return result;
	}

	/**
	 * The registered themes.
	 */
	public Collection<UITheme> getThemes() {
		return _themes.values();
	}

	/**
	 * The id of the default theme.
	 */
	public String getDefaultThemeId() {
		return _defaultTheme;
	}

	/**
	 * The id of the theme the current user has selected, or {@code null} if none is stored.
	 *
	 * <p>
	 * A stored id naming no configured theme counts as none, so a theme dropped from the
	 * configuration leaves the user following the operating system again.
	 * </p>
	 */
	public String getSelectedThemeId() {
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		if (pc != null) {
			Object stored = pc.getJSONValue(PERSONAL_THEME_KEY);
			if (stored instanceof String && _themes.containsKey(stored)) {
				return (String) stored;
			}
		}
		return null;
	}

	/**
	 * Stores the given theme id as the current user's selection.
	 *
	 * @param themeId
	 *        The selected theme id, or {@code null} to drop the selection and follow the operating
	 *        system again. An id naming no configured theme is ignored.
	 */
	public void setSelectedThemeId(String themeId) {
		if (themeId != null && !_themes.containsKey(themeId)) {
			return;
		}
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		if (pc != null) {
			pc.setJSONValue(PERSONAL_THEME_KEY, themeId);
			PersonalConfiguration.storePersonalConfiguration();
		}
	}

	/**
	 * The theme with the given id.
	 *
	 * @param id
	 *        The id of a registered theme.
	 * @return The theme, or {@code null} if no theme with that id is registered.
	 */
	public UITheme getTheme(String id) {
		return _themes.get(id);
	}

	/**
	 * Writes {@link #DS_MODE_ATTRIBUTE} for the given theme.
	 *
	 * @param out
	 *        The writer of the {@code html} start tag.
	 * @param theme
	 *        The theme in effect.
	 */
	public void writeModeAttribute(TagWriter out, UITheme theme) {
		out.writeAttribute(DS_MODE_ATTRIBUTE, theme.getColorScheme().cssKeyword());
	}

	/**
	 * The theme answering the given appearance preference of the operating system.
	 *
	 * @param scheme
	 *        The scheme the operating system asks for.
	 * @return The theme marked as the system default for that scheme, or the default theme if none
	 *         is marked.
	 */
	public UITheme getSystemTheme(ColorScheme scheme) {
		UITheme marked = _systemThemes.get(scheme);
		return marked != null ? marked : _themes.get(_defaultTheme);
	}

	/**
	 * Whether following the operating system makes a difference, i.e. whether the light and the
	 * dark appearance preference are answered by different themes.
	 */
	public boolean offersSystemThemes() {
		return getSystemTheme(ColorScheme.LIGHT) != getSystemTheme(ColorScheme.DARK);
	}

	/**
	 * Writes a {@code <style>} element defining every theme as a block of CSS custom properties
	 * scoped by the {@link #THEME_ATTRIBUTE}, each declaring the {@code color-scheme} of its
	 * appearance. The default theme is additionally bound to {@code :root}, which is the appearance
	 * of a page whose script did not run.
	 *
	 * @param out
	 *        The writer of the HTML {@code <head>}.
	 * @throws IOException
	 *         If writing fails.
	 */
	public void writeThemeStyles(TagWriter out) throws IOException {
		out.beginBeginTag(HTMLConstants.STYLE_ELEMENT);
		out.writeAttribute(HTMLConstants.TYPE_ATTR, CSS_TYPE);
		out.endBeginTag();
		for (UITheme theme : _themes.values()) {
			out.writeContent(selector(theme.getId()));
			out.writeContent("{");
			out.writeContent("color-scheme:");
			out.writeContent(theme.getColorScheme().cssKeyword());
			out.writeContent(";");
			for (Map.Entry<String, String> token : theme.getTokens().entrySet()) {
				out.writeContent("--");
				out.writeContent(token.getKey());
				out.writeContent(":");
				out.writeContent(token.getValue());
				out.writeContent(";");
			}
			out.writeContent("}");
		}
		out.endTag(HTMLConstants.STYLE_ELEMENT);
	}

	private String selector(String themeId) {
		String scoped = "[" + THEME_ATTRIBUTE + "=\"" + themeId + "\"]";
		return themeId.equals(_defaultTheme) ? ":root, " + scoped : scoped;
	}

	/**
	 * Writes the {@code <script>} element defining the {@link #CLIENT_API} object and puts the
	 * theme in effect.
	 *
	 * <p>
	 * The script names the theme in effect in the {@link #THEME_ATTRIBUTE} of the {@code html}
	 * element. Its {@link #FOLLOW_SYSTEM_FUNCTION} evaluates the operating system's appearance
	 * preference, marks the page with the {@link #THEME_MODE_ATTRIBUTE} and re-evaluates the
	 * preference whenever the operating system changes it; its {@link #SELECT_FUNCTION} drops that
	 * marker and puts the theme with the given id into effect. An element carrying no
	 * {@link #THEME_ATTRIBUTE} yet - a page rendered for a user who has selected no theme -
	 * follows the operating system.
	 * </p>
	 *
	 * <p>
	 * Written as the first script of the {@code head}, so that the theme is in effect before the
	 * first paint.
	 * </p>
	 *
	 * @param out
	 *        The writer of the HTML {@code <head>}.
	 * @throws IOException
	 *         If writing fails.
	 */
	public void writeThemeScript(TagWriter out) throws IOException {
		String themeAttr = jsString(THEME_ATTRIBUTE);
		String modeAttr = jsString(THEME_MODE_ATTRIBUTE);
		String systemMode = jsString(SYSTEM_MODE);
		String darkQuery = jsString("(prefers-color-scheme: " + ColorScheme.DARK.cssKeyword() + ")");
		String lightTheme = jsString(getSystemTheme(ColorScheme.LIGHT).getId());
		String darkTheme = jsString(getSystemTheme(ColorScheme.DARK).getId());
		String dsModeAttr = jsString(DS_MODE_ATTRIBUTE);
		String dsDensityAttr = jsString(DS_DENSITY_ATTRIBUTE);
		String dsDensityNormal = jsString(DS_DENSITY_NORMAL);
		String lightMode = jsString(ColorScheme.LIGHT.cssKeyword());
		String darkMode = jsString(ColorScheme.DARK.cssKeyword());

		out.beginScript();
		out.writeScript("(function() {");
		out.writeScript("var html = document.documentElement;");
		out.writeScript("var dark = window.matchMedia(" + darkQuery + ");");
		// The color scheme of each theme, so that selecting a theme also names the design system's mode.
		out.writeScript("var modes = {");
		boolean first = true;
		for (UITheme theme : _themes.values()) {
			out.writeScript((first ? "" : ",") + jsString(theme.getId()) + ": "
				+ jsString(theme.getColorScheme().cssKeyword()));
			first = false;
		}
		out.writeScript("};");
		out.writeScript("var api = {");
		out.writeScript(FOLLOW_SYSTEM_FUNCTION + ": function() {");
		out.writeScript("html.setAttribute(" + modeAttr + ", " + systemMode + ");");
		out.writeScript("html.setAttribute(" + themeAttr + ", dark.matches ? " + darkTheme + " : " + lightTheme
			+ ");");
		out.writeScript("html.setAttribute(" + dsModeAttr + ", dark.matches ? " + darkMode + " : " + lightMode + ");");
		out.writeScript("},");
		out.writeScript(SELECT_FUNCTION + ": function(id) {");
		out.writeScript("html.removeAttribute(" + modeAttr + ");");
		out.writeScript("html.setAttribute(" + themeAttr + ", id);");
		out.writeScript("html.setAttribute(" + dsModeAttr + ", modes[id] || " + lightMode + ");");
		out.writeScript("}");
		out.writeScript("};");
		out.writeScript("dark.addEventListener('change', function() {");
		out.writeScript("if (html.getAttribute(" + modeAttr + ") === " + systemMode + ") {");
		out.writeScript("api." + FOLLOW_SYSTEM_FUNCTION + "();");
		out.writeScript("}");
		out.writeScript("});");
		out.writeScript("window." + CLIENT_API + " = api;");
		out.writeScript("if (!html.hasAttribute(" + dsDensityAttr + ")) {");
		out.writeScript("html.setAttribute(" + dsDensityAttr + ", " + dsDensityNormal + ");");
		out.writeScript("}");
		out.writeScript("if (!html.hasAttribute(" + themeAttr + ")) {");
		out.writeScript("api." + FOLLOW_SYSTEM_FUNCTION + "();");
		out.writeScript("}");
		out.writeScript("})();");
		out.endScript();
	}

	private static String jsString(String value) {
		StringBuilder buffer = new StringBuilder();
		TagUtil.writeJsString(buffer, value);
		return buffer.toString();
	}

	private static Map<String, UITheme> resolve(InstantiationContext context, Map<String, UITheme.Config> configs) {
		Map<String, UITheme> result = new LinkedHashMap<>();
		for (String id : configs.keySet()) {
			resolveTheme(context, id, configs, result, new HashSet<>());
		}
		return result;
	}

	private static UITheme resolveTheme(InstantiationContext context, String id, Map<String, UITheme.Config> configs,
			Map<String, UITheme> result, Set<String> active) {
		UITheme resolved = result.get(id);
		if (resolved != null) {
			return resolved;
		}
		UITheme.Config config = configs.get(id);
		if (config == null) {
			context.error("Unknown theme '" + id + "' referenced by 'extends'.");
			return null;
		}
		if (!active.add(id)) {
			context.error("Cyclic theme inheritance at '" + id + "'.");
			return null;
		}

		Map<String, String> tokens = new LinkedHashMap<>();
		Map<String, DesignTokenKind> kinds = new LinkedHashMap<>();
		ColorScheme inheritedScheme = null;
		String parent = config.getExtends();
		if (!StringServices.isEmpty(parent)) {
			UITheme parentTheme = resolveTheme(context, parent, configs, result, active);
			if (parentTheme != null) {
				tokens.putAll(parentTheme.getTokens());
				kinds.putAll(parentTheme.getTokenKinds());
				inheritedScheme = parentTheme.getColorScheme();
			}
		}
		Map<String, ThemeToken<?>> own = new LinkedHashMap<>();
		for (Map.Entry<String, ThemeToken.Config<?>> entry : config.getTokens().entrySet()) {
			ThemeToken<?> token = context.getInstance(entry.getValue());
			if (token != null) {
				tokens.put(entry.getKey(), token.cssValue());
				own.put(entry.getKey(), token);
			}
		}
		resolveKinds(context, id, own, tokens, kinds);

		active.remove(id);
		ColorScheme scheme = config.getColorScheme();
		if (scheme == null) {
			scheme = inheritedScheme != null ? inheritedScheme : ColorScheme.LIGHT;
		}
		UITheme theme = new UITheme(id, config.getLabel(), config.getIcon(), scheme, config.isSystemDefault(),
			tokens, kinds);
		result.put(id, theme);
		return theme;
	}

	/**
	 * Enters the kind of each of a theme's own tokens into the theme's kind map.
	 *
	 * @param context
	 *        The context reporting a token that names no token, or a cycle of such names.
	 * @param themeId
	 *        The id of the theme whose tokens are resolved, for error reporting.
	 * @param own
	 *        The theme's own tokens, keyed by name.
	 * @param tokens
	 *        The theme's resolved token values, the theme's own ones and the inherited ones.
	 * @param kinds
	 *        The kinds resolved so far, the inherited ones on entry. A token whose kind cannot be
	 *        resolved is dropped, so that it is not taken for the inherited one it overrides.
	 */
	private static void resolveKinds(InstantiationContext context, String themeId, Map<String, ThemeToken<?>> own,
			Map<String, String> tokens, Map<String, DesignTokenKind> kinds) {
		for (String name : own.keySet()) {
			DesignTokenKind kind = kindOf(context, themeId, name, own, tokens, kinds, new HashSet<>());
			if (kind != null) {
				kinds.put(name, kind);
			} else {
				kinds.remove(name);
			}
		}
	}

	/**
	 * The kind of the token with the given name, following the chain of
	 * {@link ThemeToken#aliasedToken() names} a token aliasing another one starts.
	 *
	 * @param visiting
	 *        The names currently being followed, to stop a cycle of them.
	 * @return The kind, or <code>null</code> if the chain ends in a name no token answers, or in a
	 *         cycle. Both are reported to the given context.
	 */
	private static DesignTokenKind kindOf(InstantiationContext context, String themeId, String name,
			Map<String, ThemeToken<?>> own, Map<String, String> tokens, Map<String, DesignTokenKind> kinds,
			Set<String> visiting) {
		ThemeToken<?> token = own.get(name);
		if (token == null) {
			// A token of the extended theme, whose kind is resolved there.
			return kinds.get(name);
		}
		String ref = token.aliasedToken();
		if (ref == null) {
			return token.kind();
		}
		if (!visiting.add(name)) {
			context.error("Token '" + name + "' of theme '" + themeId + "' refers to itself through '" + ref + "'.");
			return null;
		}
		try {
			if (!tokens.containsKey(ref)) {
				context.error(
					"Token '" + name + "' of theme '" + themeId + "' refers to the undefined token '" + ref + "'.");
				return null;
			}
			return kindOf(context, themeId, ref, own, tokens, kinds, visiting);
		} finally {
			visiting.remove(name);
		}
	}

	/**
	 * The singleton {@link UIThemeService}.
	 */
	public static UIThemeService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Module for {@link UIThemeService}.
	 */
	public static final class Module extends TypedRuntimeModule<UIThemeService> {

		/** Singleton instance. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton.
		}

		@Override
		public Class<UIThemeService> getImplementation() {
			return UIThemeService.class;
		}

	}

}
