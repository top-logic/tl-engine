/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.binary;

/**
 * {@link BinaryData} that is implemented by proxying a {@link BinaryDataSource}.
 */
public abstract class BinaryDataSourceUpgrade extends BinaryDataSourceProxy {

	/**
	 * Creates a {@link BinaryDataSourceUpgrade}.
	 */
	public BinaryDataSourceUpgrade(BinaryDataSource impl) {
		super(impl);
	}

}
