/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.model;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Resource keys of the test model {@code test.objectedit} that {@link TestObjectEditTool} works on;
 * their texts are in the test bundle {@code testTlAiCoreMessages}.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey ARCHIVE_IS_KEPT;

	static {
		initConstants(I18NConstants.class);
	}
}
