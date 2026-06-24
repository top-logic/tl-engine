/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.theme;

import java.util.Map;

import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.util.ResKey;

/**
 * A resolved UI theme: an identified, labeled set of design-token values.
 *
 * <p>
 * The token map is fully resolved, i.e. the values inherited from the {@link Config#getExtends()
 * parent} theme are already merged with this theme's own overrides.
 * </p>
 */
public final class UITheme {

	/**
	 * Configuration of a single theme.
	 */
	public interface Config extends NamedConfigMandatory {

		/** Configuration name for {@link #getExtends()}. */
		String EXTENDS = "extends";

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Configuration name for {@link #getTokens()}. */
		String TOKENS = "tokens";

		/**
		 * Id of the parent theme whose tokens this theme inherits, or empty for a root theme.
		 */
		@Name(EXTENDS)
		String getExtends();

		/**
		 * Display label of the theme, e.g. for a theme picker.
		 */
		@Name(LABEL)
		ResKey getLabel();

		/**
		 * This theme's token values, overriding the inherited ones, keyed by token name.
		 */
		@Name(TOKENS)
		@Key(NamedConfiguration.NAME_ATTRIBUTE)
		Map<String, TokenDef> getTokens();

	}

	private final String _id;

	private final ResKey _label;

	private final Map<String, String> _tokens;

	/**
	 * Creates a {@link UITheme}.
	 *
	 * @param id
	 *        The theme id.
	 * @param label
	 *        The display label.
	 * @param tokens
	 *        The fully resolved token values (name without {@code --} to CSS value).
	 */
	public UITheme(String id, ResKey label, Map<String, String> tokens) {
		_id = id;
		_label = label;
		_tokens = tokens;
	}

	/**
	 * The theme id (the value of the {@code data-theme} attribute).
	 */
	public String getId() {
		return _id;
	}

	/**
	 * The display label.
	 */
	public ResKey getLabel() {
		return _label;
	}

	/**
	 * The fully resolved token values, keyed by token name (without {@code --}).
	 */
	public Map<String, String> getTokens() {
		return _tokens;
	}

}
