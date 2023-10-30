/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.gui.ThemeVar;
import com.top_logic.layout.DisplayDimension;

/**
 * Annotation to set a default value for {@link ThemeImage} and {@link ThemeVar} constants in
 * {@link IconsBase} sub-classes.
 * 
 * @see ThemeImage
 * @see ThemeVar
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultValue {

	/**
	 * The default value, e.g.
	 * 
	 * <ul>
	 * <li><code>css:fas fa-code</code> on a {@link ThemeImage}.</li>
	 * <li><code>300px</code> on a {@link ThemeVar} of {@link DisplayDimension}.</li>
	 * </ul>
	 */
	String value();

}
