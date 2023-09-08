/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.i18n;

import com.top_logic.basic.util.ResourcesModule;

/**
 * An {@link I18NChecker} checks some I18N.
 * 
 * <p>
 * Typically an {@link I18NChecker} is responsible for a part of the resources (e.g. I18N in the
 * model of the application or the I18N of the configured commands), and triggers logging of missing
 * I18N during the check.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface I18NChecker {

	/**
	 * Checks the I18N by fetching the required resources. Missing resources are logged by the
	 * default I18N mechanism.
	 * 
	 * @see ResourcesModule#handleUnknownKey(com.top_logic.basic.util.I18NBundleSPI,
	 *      com.top_logic.basic.util.ResKey)
	 */
	void checkI18N();

}

