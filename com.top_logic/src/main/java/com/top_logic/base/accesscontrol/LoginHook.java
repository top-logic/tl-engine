/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.accesscontrol;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.top_logic.basic.util.ResKey;

/**
 * Additional checks during a login process.
 * 
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public interface LoginHook {

	/**
	 * Invoked after successful authentication for additional checks and processing.
	 * 
	 * @return A reason to deny the login, or <code>null</code> to allow login.
	 */
	ResKey check(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException;

}
