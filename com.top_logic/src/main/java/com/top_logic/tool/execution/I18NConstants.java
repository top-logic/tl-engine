/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 *
 * @see ResPrefix
 * @see ResKey
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey NOT_INSIDE_TEMPLATE_LAYOUT_ERROR;

	public static ResKey NO_TEMPLATE_LAYOUT_ERROR;

	public static ResKey ERROR_NO_PERMISSION = legacyKey("tl.executable.not.allowed");

	public static ResKey ERROR_DISABLED = legacyKey("tl.executable.disabled");

	public static ResKey ERROR_HIDDEN = legacyKey("tl.executable.hidden");

	public static ResKey ERROR_HISTORIC_DATA = legacyKey("tl.executable.disabled.historicData");

	public static ResKey ERROR_CURRENT_DATA = legacyKey("tl.executable.disabled.currentData");

	public static ResKey ERROR_NO_CURRENT_DATA = legacyKey("tl.executable.noExecNoCurrentRevision");

	public static ResKey ERROR_INVALID_MODEL = legacyKey("tl.executable.noExecInvalid");

	public static ResKey ERROR_LICENCE_EXPIRED = legacyKey("tl.executable.noExecLicenceExpired");

	public static ResKey ERROR_NO_MODEL = legacyKey("tl.executable.noExecNoModel");

	public static ResKey ERROR_RESTRICTED_USER = legacyKey("tl.executable.noExecRestrictedUser");

	public static ResKey ERROR_NO_MODEL_2 = legacyKey("tl.executable.disabled.noModel");

	public static ResKey ERROR_TRANSIENT_OBJECT;

	public static ResKey ERROR_LICENSE_INVALID;

	public static ResKey ERROR_NO_SELECTION;

	public static ResKey ERROR_MODEL_NOT_SUPPORTED;

	public static ResKey ERROR_NO_CONTENT_AVAILABLE;

	public static ResKey ERROR_FUNCTIONALITY_DISABLED;

	public static ResKey1 ERROR_OCCURRED__MESSAGE;

	public static ResKey ERROR_INVALID_ID;

	public static ResKey2 ERROR_NOT_A_CHECKER__COMPONENT_LOCATION;

	public static ResKey ERROR_EDIT_MULTI_SELECTION;

	static {
		initConstants(I18NConstants.class);
	}
}
