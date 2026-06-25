/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.system;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for the {@link com.top_logic.layout.view.admin.system} package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Owner
	 */
	public static ResKey LOCK_COLUMN_OWNER;

	/**
	 * @en Operation
	 */
	public static ResKey LOCK_COLUMN_OPERATION;

	/**
	 * @en Locked objects
	 */
	public static ResKey LOCK_COLUMN_OBJECTS;

	/**
	 * @en Aspects
	 */
	public static ResKey LOCK_COLUMN_ASPECTS;

	/**
	 * @en Timeout
	 */
	public static ResKey LOCK_COLUMN_TIMEOUT;

	/**
	 * @en Cluster node
	 */
	public static ResKey LOCK_COLUMN_NODE;

	static {
		initConstants(I18NConstants.class);
	}
}
