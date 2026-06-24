/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.theme;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Registry of the new UI's themes and the source of its design tokens.
 *
 * <p>
 * A theme is a named set of {@link TokenDef design tokens} (CSS custom properties) with optional
 * inheritance. This service resolves the inheritance, emits every theme as a {@code data-theme}
 * scoped CSS block, and tracks the per-user active theme. It has no dependency on the legacy theme
 * system.
 * </p>
 */
public class UIThemeService extends ConfiguredManagedClass<UIThemeService.Config> {

	/** {@link PersonalConfiguration} key storing the user's selected theme id. */
	public static final String PERSONAL_THEME_KEY = "react.theme";

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
		 * Id of the theme used when the user has no stored preference.
		 */
		@Name(DEFAULT_THEME)
		@StringDefault("default")
		String getDefaultTheme();

	}

	private final Map<String, UITheme> _themes;

	private final String _defaultTheme;

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
	 * The id of the active theme for the current user (stored preference, or the default).
	 */
	public String getActiveThemeId() {
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		if (pc != null) {
			Object stored = pc.getJSONValue(PERSONAL_THEME_KEY);
			if (stored instanceof String && _themes.containsKey(stored)) {
				return (String) stored;
			}
		}
		return _defaultTheme;
	}

	/**
	 * Stores the given theme id as the current user's preference.
	 *
	 * @param themeId
	 *        The selected theme id; ignored if unknown.
	 */
	public void setActiveThemeId(String themeId) {
		if (!_themes.containsKey(themeId)) {
			return;
		}
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		if (pc != null) {
			pc.setJSONValue(PERSONAL_THEME_KEY, themeId);
			PersonalConfiguration.storePersonalConfiguration();
		}
	}

	/**
	 * Writes a {@code <style>} element defining every theme as a {@code data-theme} scoped block of
	 * CSS custom properties. The default theme is additionally bound to {@code :root}.
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
		String scoped = "[data-theme=\"" + themeId + "\"]";
		return themeId.equals(_defaultTheme) ? ":root, " + scoped : scoped;
	}

	private static Map<String, UITheme> resolve(Log log, Map<String, UITheme.Config> configs) {
		Map<String, UITheme> result = new LinkedHashMap<>();
		for (String id : configs.keySet()) {
			resolveTheme(log, id, configs, result, new HashSet<>());
		}
		return result;
	}

	private static UITheme resolveTheme(Log log, String id, Map<String, UITheme.Config> configs,
			Map<String, UITheme> result, Set<String> active) {
		UITheme resolved = result.get(id);
		if (resolved != null) {
			return resolved;
		}
		UITheme.Config config = configs.get(id);
		if (config == null) {
			log.error("Unknown theme '" + id + "' referenced by 'extends'.");
			return null;
		}
		if (!active.add(id)) {
			log.error("Cyclic theme inheritance at '" + id + "'.");
			return null;
		}

		Map<String, String> tokens = new LinkedHashMap<>();
		String parent = config.getExtends();
		if (!StringServices.isEmpty(parent)) {
			UITheme parentTheme = resolveTheme(log, parent, configs, result, active);
			if (parentTheme != null) {
				tokens.putAll(parentTheme.getTokens());
			}
		}
		for (TokenDef token : config.getTokens().values()) {
			tokens.put(token.getName(), token.getValue());
		}

		active.remove(id);
		UITheme theme = new UITheme(id, config.getLabel(), tokens);
		result.put(id, theme);
		return theme;
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
