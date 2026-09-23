/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.binary.scan;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;

/**
 * I18N of this package.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The uploaded file ''{0}'' was rejected because it is infected: {1}.
	 */
	public static ResKey2 ERROR_UPLOAD_INFECTED__NAME_SIGNATURE;

	/**
	 * @en The uploaded file ''{0}'' could not be checked for viruses, because the virus scanner is
	 *     not available.
	 */
	public static ResKey1 ERROR_UPLOAD_SCAN_UNAVAILABLE__NAME;

	static {
		initConstants(I18NConstants.class);
	}
}
