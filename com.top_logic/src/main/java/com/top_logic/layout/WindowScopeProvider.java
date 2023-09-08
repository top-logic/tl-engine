/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

/**
 * The interface {@link WindowScopeProvider} is needed as at construction
 * time it is possible that the corresponding {@link WindowScope} is not
 * already build.
 * 
 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
 */
public interface WindowScopeProvider {

	/**
	 * Returns a {@link WindowScope}. must be constant.
	 */
	WindowScope getWindowScope();
}