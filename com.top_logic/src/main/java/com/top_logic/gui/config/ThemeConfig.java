/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.gui.config;

import java.util.List;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.func.Function2;
import com.top_logic.gui.Theme;

/**
 * Configuration options for a {@link Theme}.
 */
public interface ThemeConfig extends ConfigurationItem {

	/**
	 * @see #getId()
	 */
	String ID_PROPERTY = "id";

	/**
	 * @see #getState()
	 */
	String STATE_PROPERTY = "state";

	/**
	 * @see #getExtends()
	 */
	String EXTENDS_PROPERTY = "extends";

	/**
	 * @see #getPath()
	 */
	String PATH_PROPERTY = "path";

	/**
	 * @see #getPathEffective()
	 */
	String PATH_EFFECTIVE_PROPERTY = "path-effective";

	/**
	 * @see #getStyles()
	 */
	String STYLES_PROPERTY = "styles";

	/**
	 * @see #isProtected()
	 */
	String PROTECTED_PROPERTY = "protected";

	/**
	 * Identifier for the {@link Theme}.
	 * 
	 * @see #getExtends()
	 */
	@Name(ID_PROPERTY)
	String getId();

	/**
	 * @see #getId()
	 */
	void setId(String value);

	/**
	 * Flag to indicate if it's possible to edit the theme (styles, theme variables, etc.).
	 */
	@Name(PROTECTED_PROPERTY)
	@BooleanDefault(false)
	boolean isProtected();

	/**
	 * @see #isProtected()
	 */
	void setProtected(boolean isProtected);

	/**
	 * Whether this {@link Theme} is enabled for being selected by the user.
	 * 
	 * <p>
	 * Even a disable {@link Theme} may be used in an {@link #getExtends()} clause of a derived
	 * {@link Theme}.
	 * </p>
	 */
	@Name(STATE_PROPERTY)
	ThemeState getState();

	/**
	 * @see #getState()
	 */
	void setState(ThemeState value);

	/**
	 * The {@link #getId()} of the base themes to inherit from.
	 */
	@Name(EXTENDS_PROPERTY)
	@Format(CommaSeparatedStrings.class)
	List<String> getExtends();

	/**
	 * @see #getExtends()
	 */
	void setExtends(List<String> value);

	/**
	 * Configuration option that defines the themes base path.
	 * 
	 * <p>
	 * The default is <code>/themes/{@link #getId() theme-id}</code>
	 * </p>
	 * 
	 * @see #getPathEffective()
	 */
	@Name(PATH_PROPERTY)
	@Nullable
	String getPath();

	/**
	 * The path to use ({@link #getPath()} or fallback to
	 * <code>/themes/{@link #getId() id}</code>).
	 */
	@Name(PATH_EFFECTIVE_PROPERTY)
	@Derived(fun = ThemeConfig.PathEffective.class, args = { @Ref(PATH_PROPERTY), @Ref(ID_PROPERTY) })
	String getPathEffective();

	/**
	 * Implementation of {@link ThemeConfig#getPathEffective()}.
	 */
	class PathEffective extends Function2<String, String, String> {
		@Override
		public String apply(String path, String id) {
			return path != null ? path : "/themes/" + id;
		}
	}

	/**
	 * @see #getPath()
	 */
	void setPath(String value);

	/**
	 * The style sheet file names to include.
	 */
	@Name(STYLES_PROPERTY)
	@Key(StyleSheetRef.NAME_ATTRIBUTE)
	List<ThemeConfig.StyleSheetRef> getStyles();

	/**
	 * Name of a style sheet to load in a certain {@link Theme}.
	 */
	interface StyleSheetRef extends NamedConfigMandatory {
		// Pure marker interface.
	}
}