/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.admin.component;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * {@link I18NConstantsBase} for this package.
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The object {0} is not a group.
	 */
	public static ResKey1 ERROR_NOT_A_GROUP__ELEMENT;

	/**
	 * Info message when a service configuration is stored into the filesystem.
	 */
	public static ResKey CONFIGURATION_SAVED_INFO_MESSAGE;

	/**
	 * Info message when a service configuration is reseted i.e. the filesystem config file from the
	 * autoconf directory is removed.
	 */
	public static ResKey CONFIGURATION_RESET_INFO_MESSAGE;

	public static ResPrefix LIST_TYPES = legacyPrefix("main.admin.list.table.select.");

	/** @see I18NConstantsBase */
	public static ResKey DUPLICATE_LIST_NAME;
	
	public static ResKey ERROR_INVALID_GROUP_NAME = legacyKey("group.name.invalid");

	public static ResKey ERROR_NOT_IN_MAINTENANCE_MODE = legacyKey("tl.executable.inMaintenanceModeOnly");

	public static ResKey ERROR_SYSTEM_GROUP_CANNOT_BE_DELETED = legacyKey("admin.group.edit.deleteGroup.disabled.isSystem");

	/**
	 * Error message if the restart of the top-logic service failed.
	 */
	public static ResKey1 SERVICE_RESTART_ERROR;

	/**
	 * Error message if the creation of the missing meatConf.txt failed.
	 */
	public static ResKey META_CONF_FILE_CREATION_ERROR;

	/**
	 * Error message if the creation of the missing custom service config file
	 * (inAppService.conf.xml) failed.
	 */
	public static ResKey CUSTOM_SERVICE_CONFIG_FILE_CREATION_ERROR;

	/**
	 * Error message if the start of the top-logic service failed.
	 */
	public static ResKey1 SERVICE_START_ERROR;

	/**
	 * Error message if the shut down of the top-logic service failed.
	 */
	public static ResKey1 SERVICE_STOP_ERROR;

	/**
	 * Error message if the service configuration could not be saved.
	 */
	public static ResKey SERVICE_CONFIG_SAVE_ERROR;

	/**
	 * Message introduction if a service with dependents should be stopped.
	 */
	public static ResKey1 SERVICE_DEPENDENT_MODULES_MESSAGE__MODULE;

	/**
	 * Label for the command to restart a service.
	 */
	public static ResKey SERVICE_RESTART_COMMAND_LABEL;

	/**
	 * Label for the command to stop a service.
	 */
	public static ResKey SERVICE_STOP_COMMAND_LABEL;

	/**
	 * Message to ask for confirmation.
	 */
	public static ResKey CONFIRMATION_MESSAGE;

	/** Message that service is currently not started. */
	public static ResKey1 SERVICE_NOT_STARTED__SERVICE;

	/** Message that service is already started. */
	public static ResKey1 SERVICE_ALREADY_STARTED__SERVICE;

	/**
	 * Message to inform about successful service restart.
	 */
	public static ResKey1 SERVICE_RESTARTED_MESSAGE__SERVICE;

	/** Message that storing is not possible for constraint violation reason. */
	public static ResKey1 SERVICE_CONFIGURATION_INVALID__ERRORS;

	/** Message that fetching configuration for a service was not possible. */
	public static ResKey1 ERROR_GETTING_SERVICE_CONFIGURATION__SERVICE;

	static {
		initConstants(I18NConstants.class);
	}
}
