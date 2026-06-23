/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.resource;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.Name;

/**
 * A client resource that is a CSS stylesheet, emitted as a {@code <link rel="stylesheet">}.
 *
 * <p>
 * A {@link #isThemeScoped() theme-scoped} stylesheet is resolved through the active theme's overlay
 * search path, so that a theme may override it by providing a file of the same name.
 * </p>
 */
public interface StyleSheetConfig extends ResourceConfig {

	/** Element tag of a stylesheet resource declaration. */
	String TAG_NAME = "stylesheet";

	/** Configuration name for {@link #getResource()}. */
	String RESOURCE = "resource";

	/** Configuration name for {@link #isThemeScoped()}. */
	String THEME_SCOPED = "theme-scoped";

	/**
	 * The logical context-relative path of the stylesheet, e.g. {@code "/style/tlReactControls.css"}.
	 */
	@Name(RESOURCE)
	@Mandatory
	String getResource();

	/**
	 * Whether {@link #getResource()} is resolved through the active theme's overlay (so a theme can
	 * override the file), as opposed to being used verbatim.
	 */
	@Name(THEME_SCOPED)
	@BooleanDefault(true)
	boolean isThemeScoped();

}
