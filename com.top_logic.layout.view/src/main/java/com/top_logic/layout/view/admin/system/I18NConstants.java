/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.system;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.basic.util.ResKeyN;
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
	 * @en Running
	 */
	public static ResKey SERVICE_STATE_ACTIVE;

	/**
	 * @en Stopped
	 */
	public static ResKey SERVICE_STATE_INACTIVE;

	/**
	 * @en Running – restart required
	 */
	public static ResKey SERVICE_STATE_RESTART_REQUIRED;

	/**
	 * @en Configuration saved. Restart the service to apply it.
	 */
	public static ResKey SERVICE_CONFIGURATION_SAVED;

	/**
	 * @en Configuration saved. It matches the configuration the service is running with, no
	 *     restart is necessary.
	 */
	public static ResKey SERVICE_CONFIGURATION_SAVED_UNCHANGED;

	/**
	 * @en In-app configuration removed. Restart the service to apply the configuration of the
	 *     application's configuration files.
	 */
	public static ResKey SERVICE_CONFIGURATION_RESET;

	/**
	 * @en In-app configuration removed. The configuration of the application's configuration
	 *     files matches the one the service is running with, no restart is necessary.
	 */
	public static ResKey SERVICE_CONFIGURATION_RESET_UNCHANGED;

	/**
	 * @en Changes discarded. The configuration the service is running with is in effect again.
	 */
	public static ResKey SERVICE_CONFIGURATION_DISCARDED;

	/**
	 * @en No service is selected.
	 */
	public static ResKey ERROR_NO_SERVICE;

	/**
	 * @en There is no service configuration to store.
	 */
	public static ResKey ERROR_NO_SERVICE_CONFIGURATION;

	/**
	 * @en Lifecycle operation on service ''{0}'' failed.
	 */
	public static ResKey1 ERROR_SERVICE_LIFECYCLE__NAME;

	/**
	 * @en Operation on task ''{0}'' failed.
	 */
	public static ResKey1 ERROR_SCHEDULER_TASK__NAME;

	/**
	 * @en Task ''{0}'' has been told to stop.
	 */
	public static ResKey1 SCHEDULER_TASK_STOP_SIGNALED__NAME;

	/**
	 * @en Task ''{0}'' has not accepted the request to stop.
	 */
	public static ResKey1 ERROR_SCHEDULER_TASK_NOT_STOPPED__NAME;

	/**
	 * @en No full-text indexing service is running.
	 */
	public static ResKey ERROR_NO_INDEXING_SERVICE;

	/**
	 * @en The running access manager has no security storage.
	 */
	public static ResKey ERROR_NO_SECURITY_STORAGE;

	/**
	 * @en The security storage was not rebuilt: a rebuild is already running, or it failed. See the
	 *     log of the application for details.
	 */
	public static ResKey ERROR_SECURITY_STORAGE_NOT_REBUILT;

	/**
	 * @en Enter a value.
	 */
	public static ResKey ERROR_NO_VALUE;

	/**
	 * @en Choose the format of the encrypted value.
	 */
	public static ResKey ERROR_NO_ENCRYPTION_FORMAT;

	/**
	 * @en The value is not encrypted in the format "{0}". Check that the value was copied
	 *     completely and that the format it was encrypted in is chosen.
	 */
	public static ResKey1 ERROR_NOT_ENCRYPTED__FORMAT;

	/**
	 * @en The encryption support service is not running.
	 */
	public static ResKey ERROR_NO_CRYPT_SUPPORT;

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
	 * @en Download
	 */
	public static ResKey LOG_COLUMN_DOWNLOAD;

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

	/**
	 * @en Database
	 */
	public static ResKey DATABASE_SECTION_DATABASE;

	/**
	 * @en History
	 */
	public static ResKey DATABASE_SECTION_HISTORY;

	/**
	 * @en Product
	 */
	public static ResKey DATABASE_PRODUCT;

	/**
	 * @en Product version
	 */
	public static ResKey DATABASE_PRODUCT_VERSION;

	/**
	 * @en JDBC driver
	 */
	public static ResKey DATABASE_DRIVER;

	/**
	 * @en SQL dialect
	 */
	public static ResKey DATABASE_SQL_DIALECT;

	/**
	 * @en The application keeps no history.
	 */
	public static ResKey DATABASE_NO_HISTORY;

	/**
	 * @en Current revision
	 */
	public static ResKey DATABASE_LAST_REVISION;

	/**
	 * @en Current revision committed at
	 */
	public static ResKey DATABASE_LAST_REVISION_DATE;

	/**
	 * @en Oldest revision kept
	 */
	public static ResKey DATABASE_FIRST_REVISION;

	/**
	 * @en Oldest revision committed at
	 */
	public static ResKey DATABASE_FIRST_REVISION_DATE;

	/**
	 * @en A compaction would collapse the history into revision {0}: {1} rows deleted, {2} rows
	 *     rewritten, {3} references re-pinned, {4} references cleared, {5} rows without a
	 *     mandatory target deleted.
	 */
	public static ResKeyN DATABASE_ANALYSIS__REVISION_DELETED_REWRITTEN_REPINNED_CLEARED_DROPPED;

	/**
	 * @en The history is collapsed into revision {0} of {1}; {2} older revisions are discarded.
	 */
	public static ResKey3 DATABASE_COMPACTION_TARGET__REVISION_DATE_DISCARDED;

	/**
	 * @en There is no older revision to discard.
	 */
	public static ResKey DATABASE_COMPACTION_NOTHING;

	/**
	 * @en The history is to be compacted either before a point in time or before a revision.
	 */
	public static ResKey ERROR_COMPACTION_BOUND;

	/**
	 * @en No statement given.
	 */
	public static ResKey ERROR_SQL_NO_STATEMENT;

	/**
	 * @en The maximum number of rows must be at least 1.
	 */
	public static ResKey ERROR_SQL_MAX_ROWS;

	/**
	 * @en The data of the application is not stored in a database.
	 */
	public static ResKey ERROR_SQL_NO_DATABASE;

	/**
	 * @en The database rejected the statement: {0}
	 */
	public static ResKey1 ERROR_SQL_REJECTED__MESSAGE;

	/**
	 * @en {0} rows read in {1} ms.
	 */
	public static ResKey2 SQL_QUERY__ROWS_DURATION;

	/**
	 * @en The first {0} rows are shown, the query delivers more. Read in {1} ms.
	 */
	public static ResKey2 SQL_QUERY_TRUNCATED__ROWS_DURATION;

	/**
	 * @en The update affected {0} rows and was committed in {1} ms.
	 */
	public static ResKey2 SQL_UPDATE__ROWS_DURATION;

	/**
	 * @en The update would affect {0} rows; it was rolled back. Run in {1} ms.
	 */
	public static ResKey2 SQL_SIMULATION__ROWS_DURATION;

	/**
	 * @en (binary, {0} bytes)
	 */
	public static ResKey1 SQL_BINARY_VALUE__SIZE;

	/**
	 * @en Reloaded: {0}
	 */
	public static ResKey1 RELOAD_DONE__NAMES;

	/**
	 * @en Could not be reloaded:
	 */
	public static ResKey RELOAD_FAILED;

	/**
	 * @en - {0}: {1}
	 */
	public static ResKey2 RELOAD_FAILURE__NAME_REASON;

	static {
		initConstants(I18NConstants.class);
	}
}
