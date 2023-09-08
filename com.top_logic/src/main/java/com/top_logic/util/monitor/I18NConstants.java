/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.monitor;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResPrefix PERSON = legacyPrefix("tl.admin.person.");

	public static ResKey SYSTEM_SERVER = legacyKey("admin.sys.monitor.server");

	public static ResKey SYSTEM_TIME = legacyKey("admin.sys.monitor.time");

	public static ResKey SYSTEM_STARTUP = legacyKey("admin.sys.monitor.startup");

	public static ResKey2 SYSTEM_STARTUP__STARTUP__UPTIME = legacyKey2("admin.sys.monitor.startup__STARTUP__UPTIME");

	public static ResKey SYSTEM_TIME_ZONE = legacyKey("admin.sys.monitor.timeZoneSystem");

	public static ResKey USER_TIME_ZONE = legacyKey("admin.sys.monitor.timeZoneUser");

	public static ResKey SYSTEM_APPLICATION_VERSION = legacyKey("admin.sys.monitor.application");

	public static ResKey STATE_MESSAGE_FATAL = legacyKey("admin.sys.monitor.message.fatal");

	public static ResKey STATE_MESSAGE_FATAL1 = legacyKey("admin.sys.monitor.message.fatal1");

	public static ResKey STATE_DOWN = legacyKey("admin.sys.monitor.down");

	public static ResKey STATE_MESSAGE_ALIVE = legacyKey("admin.sys.monitor.message.alive");

	public static ResKey STATE_MESSAGE_ALIVE1 = legacyKey("admin.sys.monitor.message.alive1");

	public static ResKey STATE_MESSAGE_OK = legacyKey("admin.sys.monitor.message.ok");

	public static ResKey STATE_ALIVE = legacyKey("admin.sys.monitor.alive");

	public static ResKey STATE_OK = legacyKey("admin.sys.monitor.ok");


	public static ResKey MEMORY_FREE = legacyKey("util.maintenance.vm.free");

	public static ResKey MEMORY_USED = legacyKey("util.maintenance.vm.used");

	public static ResKey MEMORY_AVAILABLE = legacyKey("util.maintenance.vm.available");

	public static ResKey MEMORY_TOTAL = legacyKey("util.maintenance.vm.size");

	public static ResKey MEMORY_MAX = legacyKey("util.maintenance.vm.max");

	/**
	 * @en Vendor
	 */
	public static ResKey JAVA_VENDOR;

	/**
	 * @en Java
	 */
	public static ResKey JAVA_INFO;

	/**
	 * @en Runtime
	 */
	public static ResKey JAVA_RUNTIME;

	/**
	 * @en Version
	 */
	public static ResKey JAVA_VERSION;

	/**
	 * @en Delete license
	 */
	public static ResPrefix DELETE_LICENSE;

	/**
	 * @en Do you want to remove the current license?
	 */
	public static ResKey DELETE_LICENSE_INFO;

	/**
	 * @en User account usage
	 */
	public static ResKey LICENSE_USERS_FULL;

	/**
	 * @en Restricted user account usage
	 */
	public static ResKey LICENSE_USERS_RESTRICTED;

	/**
	 * @en {0} of max {1}
	 */
	public static ResKey LICENSE_USERS_VALUE;

	/**
	 * @en Product type
	 */
	public static ResKey PRODUCT_TYPE;

	/**
	 * @en License state
	 */
	public static ResKey LICENCE_STATE;

	/**
	 * @en License key
	 */
	public static ResKey LICENSE_KEY;

	/**
	 * @en Expiry date of license
	 */
	public static ResKey LICENSE_EXPIRE_DATE;

	/**
	 * @en Next license verification
	 */
	public static ResKey LICENSE_VALIDITY;

	/**
	 * @en Unlimited
	 */
	public static ResKey UNLIMITED;

	/**
	 * @en Does not expire.
	 */
	public static ResKey NO_EXPIRY;

	/**
	 * @en Number of cluster nodes
	 */
	public static ResKey LICENSE_CLUSTER_SIZE;

	public static ResKey ALIVE1 = legacyKey("admin.sys.monitor.message.alive1");

	public static ResKey APPLICATION = legacyKey("admin.sys.monitor.application");

	public static ResKey AVAILABLE = legacyKey("util.maintenance.vm.available");

	public static ResKey DOWN = legacyKey("admin.sys.monitor.down");

	public static ResKey FATAL = legacyKey("admin.sys.monitor.message.fatal");

	public static ResKey FATAL1 = legacyKey("admin.sys.monitor.message.fatal1");

	public static ResKey FREE = legacyKey("util.maintenance.vm.free");

	public static ResKey MAX = legacyKey("util.maintenance.vm.max");

	public static ResKey MESSAGE_ALIVE = legacyKey("admin.sys.monitor.message.alive");

	public static ResKey MONITOR_ALIVE = legacyKey("admin.sys.monitor.alive");

	public static ResKey OK = legacyKey("admin.sys.monitor.ok");

	public static ResKey SERVER = legacyKey("admin.sys.monitor.server");

	public static ResKey TIME = legacyKey("admin.sys.monitor.time");

	/**
	 * @en Classpath
	 */
	public static ResKey SYSTEM_ENVIRONMENT_CLASS_PATH;

	/**
	 * @en System properties
	 */
	public static ResKey SYSTEM_ENVIRONMENT_SYSTEM_PROPERTIES;

	/**
	 * @en JNDI Properties
	 */
	public static ResKey SYSTEM_ENVIRONMENT_JNDI_PROPERTIES;

	/**
	 * @en Error accessing property ''{0}'': {1}
	 */
	public static ResKey2 SYSTEM_ENVIRONMENT_ERROR_ACCESSING_PROPERTY__PROPERTY_MESSAGE;

	/**
	 * @en Application properties
	 */
	public static ResKey SYSTEM_ENVIRONMENT_APPLICATION_PROPERTIES;

	/**
	 * @en VM arguments
	 */
	public static ResKey SYSTEM_ENVIRONMENT_VM_ARGUMENTS;

	/**
	 * @en Classloader URLs
	 */
	public static ResKey SYSTEM_ENVIRONMENT_CLASS_LOADER;

	static {
		initConstants(I18NConstants.class);
	}
}
