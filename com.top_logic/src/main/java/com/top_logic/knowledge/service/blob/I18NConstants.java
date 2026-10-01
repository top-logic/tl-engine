/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.blob;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Unreferenced blobs of {0} stores collected: {1} deleted, {2} kept by the grace period.
	 */
	public static ResKey3 BLOB_GC_DONE__STORES_DELETED_KEPT;

	/**
	 * @en The blob garbage collection was aborted without deleting anything for the stores {0}: the
	 *     keys were not delivered in lexicographic order. See the application log for details.
	 */
	public static ResKey1 ERROR_BLOB_GC_ABORTED__STORES;

	static {
		initConstants(I18NConstants.class);
	}
}
