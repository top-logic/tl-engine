/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.theme;

import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;

/**
 * A single design token: a named value that is emitted as a CSS custom property.
 *
 * <p>
 * The {@link #getName() name} is the custom-property name without the leading {@code --} (e.g.
 * {@code button-primary}). The {@link #getValue() value} is a CSS value that may itself reference
 * other tokens (e.g. {@code var(--layer-accent-icon)}).
 * </p>
 */
public interface TokenDef extends NamedConfigMandatory {

	/** Configuration name for {@link #getKind()}. */
	String KIND = "kind";

	/** Configuration name for {@link #getValue()}. */
	String VALUE = "value";

	/** Configuration name for {@link #getGroup()}. */
	String GROUP = "group";

	/**
	 * The kind of value, for validation and theme-editor widget selection.
	 */
	@Name(KIND)
	TokenKind getKind();

	/**
	 * The CSS value of the token, possibly referencing other tokens via {@code var(--other)}.
	 */
	@Name(VALUE)
	@Mandatory
	String getValue();

	/**
	 * Optional grouping label for related tokens.
	 */
	@Name(GROUP)
	String getGroup();

}
