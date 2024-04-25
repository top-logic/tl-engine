/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.persistency;

import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;

/**
 * All table that can be used to store link objects.
 */
public class LinkTables extends AllTables {
	@Override
	protected String getBaseTable() {
		return ApplicationObjectUtil.WRAPPER_ATTRIBUTE_ASSOCIATION_BASE;
	}
}