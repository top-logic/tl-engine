/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr.config.operations;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;

/**
 * TL-Script functions with optional enum parameters declaring the different kinds of defaults.
 *
 * @see TestEnumParameterDefaults
 */
@ScriptPrefix(EnumDefaultFunctions.PREFIX)
public class EnumDefaultFunctions extends TLScriptFunctions {

	/**
	 * The {@link ScriptPrefix} of the functions in this class.
	 */
	public static final String PREFIX = "testEnumDefault";

	/**
	 * Result of the functions in this class for a <code>null</code> argument.
	 */
	public static final String NONE = "none";

	/**
	 * Enum type of the parameters of the functions in this class.
	 */
	public enum Shade implements ExternallyNamed {

		/** First constant, the default of an unannotated parameter. */
		LIGHT_GRAY("light-gray"),

		/** Constant referenced by its Java name. */
		MEDIUM_GRAY("medium-gray"),

		/** Constant referenced by its external name. */
		DARK_GRAY("dark-gray");

		private final String _externalName;

		private Shade(String externalName) {
			_externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}
	}

	/**
	 * Function with an enum parameter without default annotation.
	 *
	 * @param shade
	 *        The shade to describe.
	 * @return The Java name of the given shade, or {@link #NONE}.
	 */
	public static String plain(Shade shade) {
		return describe(shade);
	}

	/**
	 * Function with an enum parameter defaulting to a constant given by its Java name.
	 *
	 * @param shade
	 *        The shade to describe.
	 * @return The Java name of the given shade, or {@link #NONE}.
	 */
	public static String byConstantName(@FormattedDefault("MEDIUM_GRAY") Shade shade) {
		return describe(shade);
	}

	/**
	 * Function with an enum parameter defaulting to a constant given by its external name.
	 *
	 * @param shade
	 *        The shade to describe.
	 * @return The Java name of the given shade, or {@link #NONE}.
	 */
	public static String byExternalName(@FormattedDefault("dark-gray") Shade shade) {
		return describe(shade);
	}

	/**
	 * Function with an enum parameter defaulting to <code>null</code>.
	 *
	 * @param shade
	 *        The shade to describe.
	 * @return The Java name of the given shade, or {@link #NONE}.
	 */
	public static String unset(@NullDefault Shade shade) {
		return describe(shade);
	}

	private static String describe(Shade shade) {
		return shade == null ? NONE : shade.name();
	}

}
