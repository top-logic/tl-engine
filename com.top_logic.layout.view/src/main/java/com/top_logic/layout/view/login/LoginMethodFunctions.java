/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import com.top_logic.base.accesscontrol.loginmethod.LoginMethod;
import com.top_logic.base.accesscontrol.loginmethod.LoginMethods;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;

/**
 * TL-Script functions about the external login methods of the application, see
 * {@link LoginMethod}.
 */
@ScriptPrefix(LoginMethodFunctions.PREFIX)
public class LoginMethodFunctions extends TLScriptFunctions {

	/** Prefix all function names of this class start with. */
	public static final String PREFIX = "loginMethod";

	/**
	 * The label of the login method with the given ID.
	 *
	 * @param id
	 *        The {@link LoginMethod#getId() ID} of a login method.
	 * @return The label, or <code>null</code> if the application has no login method with this ID.
	 */
	@Label("Label of a login method")
	@SideEffectFree
	public static ResKey label(@Mandatory String id) {
		for (LoginMethod method : LoginMethods.all()) {
			if (method.getId().equals(id)) {
				return method.getLabel();
			}
		}
		return null;
	}

}
