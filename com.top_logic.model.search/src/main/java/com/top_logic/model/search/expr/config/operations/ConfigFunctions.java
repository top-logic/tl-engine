/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import com.top_logic.basic.AliasManager;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;

/**
 * TL-Script functions reading the configuration of the application.
 *
 * <p>
 * {@link #variable(String)} reads the value of a single configuration variable (alias), e.g.
 * <code>configVariable('EHOME_DISCOVERY') != 'false'</code>. To replace all configuration
 * variables (aliases) occurring in a text, use the TL-Script function <code>resolveAlias(text)</code>
 * ({@link AliasResolver}) instead.
 * </p>
 *
 * @implNote Every public static method here is a TL-Script function named by the
 *           {@link ScriptPrefix} of this class followed by the capitalized method name, so
 *           {@link #variable(String)} is called <code>configVariable</code>.
 */
@ScriptPrefix(ConfigFunctions.PREFIX)
public class ConfigFunctions extends TLScriptFunctions {

	/** Prefix all function names of this class start with. */
	public static final String PREFIX = "config";

	/**
	 * The effective value of the configuration variable (alias) with the given name.
	 *
	 * <p>
	 * The value is the one the application uses when it replaces the variable in its
	 * configuration: A reference to another configuration variable in the value is already
	 * replaced. Example: <code>configVariable('EHOME_DISCOVERY') != 'false'</code>.
	 * </p>
	 *
	 * <p>
	 * This function reads a single variable. To replace all configuration variables (aliases)
	 * occurring in a text, e.g. <code>'%HOST%:%PORT%'</code>, use the function
	 * <code>resolveAlias(text)</code>.
	 * </p>
	 *
	 * @param name
	 *        The name of the configuration variable, with or without the enclosing <code>%</code>
	 *        characters, e.g. <code>EHOME_DISCOVERY</code> or <code>%EHOME_DISCOVERY%</code>.
	 * @return The value of the variable, <code>null</code> if no variable with the given name is
	 *         defined.
	 *
	 * @see AliasManager#getAlias(String)
	 */
	@Label("Value of a configuration variable (alias)")
	@SideEffectFree
	public static String variable(@Mandatory String name) {
		if (name == null) {
			return null;
		}
		try {
			return AliasManager.getInstance().getAlias(name);
		} catch (IllegalArgumentException ex) {
			// A name containing a '%' inside never names a defined variable.
			return null;
		}
	}

}
