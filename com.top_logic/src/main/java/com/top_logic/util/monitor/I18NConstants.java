/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.monitor;

import com.top_logic.basic.i18n.CustomKey;
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

	@CustomKey("admin.sys.monitor.server")
	public static ResKey SYSTEM_SERVER;

	@CustomKey("admin.sys.monitor.time")
	public static ResKey SYSTEM_TIME;

	@CustomKey("admin.sys.monitor.startup")
	public static ResKey SYSTEM_STARTUP;

	@CustomKey("admin.sys.monitor.startup__STARTUP__UPTIME")
	public static ResKey2 SYSTEM_STARTUP__STARTUP__UPTIME;

	@CustomKey("admin.sys.monitor.timeZoneSystem")
	public static ResKey SYSTEM_TIME_ZONE;

	@CustomKey("admin.sys.monitor.timeZoneUser")
	public static ResKey USER_TIME_ZONE;

	@CustomKey("admin.sys.monitor.application")
	public static ResKey SYSTEM_APPLICATION_VERSION;

	@CustomKey("admin.sys.monitor.message.fatal")
	public static ResKey STATE_MESSAGE_FATAL;

	@CustomKey("admin.sys.monitor.message.fatal1")
	public static ResKey STATE_MESSAGE_FATAL1;

	@CustomKey("admin.sys.monitor.down")
	public static ResKey STATE_DOWN;

	@CustomKey("admin.sys.monitor.message.alive")
	public static ResKey STATE_MESSAGE_ALIVE;

	@CustomKey("admin.sys.monitor.message.alive1")
	public static ResKey STATE_MESSAGE_ALIVE1;

	@CustomKey("admin.sys.monitor.message.ok")
	public static ResKey STATE_MESSAGE_OK;

	@CustomKey("admin.sys.monitor.alive")
	public static ResKey STATE_ALIVE;

	@CustomKey("admin.sys.monitor.ok")
	public static ResKey STATE_OK;


	@CustomKey("util.maintenance.vm.free")
	public static ResKey MEMORY_FREE;

	@CustomKey("util.maintenance.vm.used")
	public static ResKey MEMORY_USED;

	@CustomKey("util.maintenance.vm.available")
	public static ResKey MEMORY_AVAILABLE;

	@CustomKey("util.maintenance.vm.size")
	public static ResKey MEMORY_TOTAL;

	@CustomKey("util.maintenance.vm.max")
	public static ResKey MEMORY_MAX;

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
	 * @en Does not expire.
	 */
	public static ResKey NO_EXPIRY;

	@CustomKey("admin.sys.monitor.message.alive1")
	public static ResKey ALIVE1;

	@CustomKey("admin.sys.monitor.application")
	public static ResKey APPLICATION;

	@CustomKey("util.maintenance.vm.available")
	public static ResKey AVAILABLE;

	@CustomKey("admin.sys.monitor.down")
	public static ResKey DOWN;

	@CustomKey("admin.sys.monitor.message.fatal")
	public static ResKey FATAL;

	@CustomKey("admin.sys.monitor.message.fatal1")
	public static ResKey FATAL1;

	@CustomKey("util.maintenance.vm.free")
	public static ResKey FREE;

	@CustomKey("util.maintenance.vm.max")
	public static ResKey MAX;

	@CustomKey("admin.sys.monitor.message.alive")
	public static ResKey MESSAGE_ALIVE;

	@CustomKey("admin.sys.monitor.alive")
	public static ResKey MONITOR_ALIVE;

	@CustomKey("admin.sys.monitor.ok")
	public static ResKey OK;

	@CustomKey("admin.sys.monitor.server")
	public static ResKey SERVER;

	@CustomKey("admin.sys.monitor.time")
	public static ResKey TIME;

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
