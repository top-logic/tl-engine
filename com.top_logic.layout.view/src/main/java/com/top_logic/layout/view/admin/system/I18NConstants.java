/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.system;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
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

	/**
	 * @en Status
	 */
	public static ResKey SERVICE_COLUMN_STATUS;

	/**
	 * @en Service
	 */
	public static ResKey SERVICE_COLUMN_NAME;

	/**
	 * @en Implementation
	 */
	public static ResKey SERVICE_COLUMN_TECHNICAL_NAME;

	/**
	 * @en Running
	 */
	public static ResKey SERVICE_STATE_ACTIVE;

	/**
	 * @en Stopped
	 */
	public static ResKey SERVICE_STATE_INACTIVE;

	/**
	 * @en Lifecycle operation on service ''{0}'' failed.
	 */
	public static ResKey1 ERROR_SERVICE_LIFECYCLE__NAME;

	/**
	 * @en The system is operating normally.
	 */
	public static ResKey MAINTENANCE_STATUS_NORMAL;

	/**
	 * @en Maintenance mode will be activated at {0}.
	 */
	public static ResKey1 MAINTENANCE_STATUS_PENDING__TIME;

	/**
	 * @en The system is in maintenance mode.
	 */
	public static ResKey MAINTENANCE_STATUS_ACTIVE;

	/**
	 * @en File
	 */
	public static ResKey LOG_COLUMN_NAME;

	/**
	 * @en Size
	 */
	public static ResKey LOG_COLUMN_SIZE;

	/**
	 * @en Last modified
	 */
	public static ResKey LOG_COLUMN_MODIFIED;

	/**
	 * @en Time
	 */
	public static ResKey LOG_COLUMN_TIME;

	/**
	 * @en Severity
	 */
	public static ResKey LOG_COLUMN_SEVERITY;

	/**
	 * @en Category
	 */
	public static ResKey LOG_COLUMN_CATEGORY;

	/**
	 * @en Thread
	 */
	public static ResKey LOG_COLUMN_THREAD;

	/**
	 * @en Message
	 */
	public static ResKey LOG_COLUMN_MESSAGE;

	/**
	 * @en Details
	 */
	public static ResKey LOG_COLUMN_DETAILS;

	/**
	 * @en Logger
	 */
	public static ResKey LOGGER_COLUMN_NAME;

	/**
	 * @en Level
	 */
	public static ResKey LOGGER_COLUMN_LEVEL;

	static {
		initConstants(I18NConstants.class);
	}
}
