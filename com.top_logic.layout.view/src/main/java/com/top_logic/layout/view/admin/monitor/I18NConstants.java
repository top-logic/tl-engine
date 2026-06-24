/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.monitor;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for the {@link com.top_logic.layout.view.admin.monitor} package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Section
	 */
	public static ResKey COLUMN_SECTION;

	/**
	 * @en Name
	 */
	public static ResKey COLUMN_NAME;

	/**
	 * @en Value
	 */
	public static ResKey COLUMN_VALUE;

	/**
	 * @en System properties
	 */
	public static ResKey SYSTEM_ENVIRONMENT_SECTION_SYSTEM_PROPERTIES;

	/**
	 * @en VM arguments
	 */
	public static ResKey SYSTEM_ENVIRONMENT_SECTION_VM_ARGUMENTS;

	/**
	 * @en Configuration variables
	 */
	public static ResKey SYSTEM_ENVIRONMENT_SECTION_CONFIGURATION_VARIABLES;

	/**
	 * @en Status
	 */
	public static ResKey APPLICATION_MONITOR_SECTION_STATUS;

	/**
	 * @en Memory
	 */
	public static ResKey APPLICATION_MONITOR_SECTION_MEMORY;

	/**
	 * @en Java VM
	 */
	public static ResKey APPLICATION_MONITOR_SECTION_JAVA;

	/**
	 * @en System
	 */
	public static ResKey APPLICATION_MONITOR_SECTION_SYSTEM;

	/**
	 * @en Name
	 */
	public static ResKey THREAD_COLUMN_NAME;

	/**
	 * @en State
	 */
	public static ResKey THREAD_COLUMN_STATE;

	/**
	 * @en Priority
	 */
	public static ResKey THREAD_COLUMN_PRIORITY;

	/**
	 * @en Kind
	 */
	public static ResKey THREAD_COLUMN_KIND;

	/**
	 * @en Group
	 */
	public static ResKey THREAD_COLUMN_GROUP;

	/**
	 * @en Daemon
	 */
	public static ResKey THREAD_KIND_DAEMON;

	/**
	 * @en Normal
	 */
	public static ResKey THREAD_KIND_NORMAL;

	/**
	 * @en Stack frame
	 */
	public static ResKey THREAD_COLUMN_STACK_FRAME;

	/**
	 * @en Select a thread to show its current stack trace.
	 */
	public static ResKey THREAD_STACK_EMPTY;

	static {
		initConstants(I18NConstants.class);
	}
}
