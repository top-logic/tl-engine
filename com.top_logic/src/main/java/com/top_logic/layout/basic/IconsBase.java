/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.gui.ThemeFactory;
import com.top_logic.gui.ThemeVar;

/**
 * Base class for defining theme variables.
 * 
 * <p>
 * Sub-classes are expected to be named <code>Icons</code> to declare public static (pseudo-final)
 * fields of type {@link ThemeImage} or {@link ThemeVar} for the package they reside in. During
 * system startup, those fields are automatically filled with {@link ThemeImage}s or
 * {@link ThemeVar}s referencing a theme setting with the fully qualified name of the corresponding
 * field.
 * </p>
 * 
 * <p>
 * The concrete icon that is used in a theme is defined in a <code>theme-settings.xml</code> file
 * for that theme. The property name is the fully qualified name of the icons class appended by '.'
 * and the name of the declared constant (e.g.
 * <code>com.top_logic.layout.form.control.Icons.ALERT</code>).
 * </p>
 * 
 * <p>
 * {@link ThemeImage} and {@link ThemeVar} constants may be annotated with {@link DefaultValue} to
 * provide a theme-independent default image to use, if the theme does not provide an icon, e.g.:
 * </p>
 * 
 * <pre>
 * &#64;DefaultValue("css:fas fa-code")
 * public static ThemeImage POPUP_BUTTON_TEMPLATE;
 * </pre>
 * 
 * <p>
 * The <code>theme-settings.xml</code> files are expected in a theme-folder with the same name as
 * the theme for which the definitions are created. A theme-folder must be located in
 * <code>WEB-INF/themes</code> of the defining module (e.g.
 * <code>WEB-INF/themes/default/theme-settings.xml</code>).
 * </p>
 * 
 * <p>
 * The value of an icon property is either the web-application-local path of the icon to be served,
 * or one of the prefixes defined by {@link ThemeImage} followed by a corresponding definition, see
 * e.g. {@link ThemeImage#CSS_PREFIX}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class IconsBase {

	// Pure marker super-class.
	static {
		assert ThemeFactory.Module.INSTANCE
			.isActive() : "ThemeFactory is not yet started. Variables are not initialized.";
	}

	/**
	 * @deprecated Explicit initialization is no longer required. Remove static initializer that
	 *             calls this method.
	 */
	@Deprecated
	protected static void initConstants(@SuppressWarnings("unused") Class<? extends IconsBase> iconsClass) {
		// Ignore.
	}

}
