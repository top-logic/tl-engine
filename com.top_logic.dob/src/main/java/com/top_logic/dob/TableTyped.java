/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob;

import com.top_logic.dob.meta.MOStructure;

/**
 * {@link StaticTyped}, where the type is a table.
 */
public interface TableTyped extends StaticTyped {

	@Override
	MOStructure tTable();

}
