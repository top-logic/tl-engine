/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider.label;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * {@link I18NConstants} for this package.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Current revision
	 * @tooltip The current revision represents the current state of the application's data
	 *          inventory that can be modified. All other revisions represent a stable unmodifyable
	 *          view of historic data.
	 */
	public static ResKey CURRENT_REVISION_LABEL;

	/**
	 * @en Initial revision
	 * @tooltip The initial revision represents the state before the initial system setup. At
	 *          initial revision, the data inventory of the application is considered empty.
	 */
	public static ResKey INITIAL_REVISION_LABEL;

	/**
	 * @en Revision {0} ({1})
	 * @tooltip The historic system state at {1}, represented by the technical revision number {0}.
	 */
	public static ResKey2 REVISION_LABEL__REV_DATE;

	static {
		initConstants(I18NConstants.class);
	}
}
