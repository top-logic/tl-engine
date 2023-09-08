/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.format.MillisFormat;

/**
 * Configuration for the start stop listener.
 * 
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface StartStopListenerConfig extends ConfigurationItem {
	String STARTUP_SLEEP = "startupSleep";
	String LOCK_TIMEOUT = "lockTimeout";
	String DEFERRED_BOOT = "deferredBoot";
	String ENTER_MAINTENANCE_MODE = "enterMaintenanceMode";
	String DO_INIT_LAYOUTS = "doInitLayouts";

	@Name(DO_INIT_LAYOUTS)
	@BooleanDefault(true)
	boolean getDoInitLayouts();
	
	@Name(ENTER_MAINTENANCE_MODE)
	boolean getEnterMaintenanceMode();
	
	@Name(DEFERRED_BOOT)
	@BooleanDefault(false)
	boolean getDeferredBoot();

	/**
	 * Maximum time waiting for a startup lock while booting the application.
	 * 
	 * <p>
	 * The timeout configured here must at least be as long as the token timeout of the
	 * <code>startup</code> token (see
	 * {@link com.top_logic.base.locking.service.ConfiguredLockService.Config.TypeConfig.OperationConfig#getLockTimeout()}.
	 * </p>
	 */
	@Name(LOCK_TIMEOUT)
	@LongDefault(10 * 60 * 1000L)
	@Format(MillisFormat.class)
	long getLockTimeout();

	/**
	 * Polling interval when waiting for a startup token.
	 * 
	 * @see #getLockTimeout()
	 */
	@Name(STARTUP_SLEEP)
	@LongDefault(1000 * 10L)
	@Format(MillisFormat.class)
	long getStartupSleep();
}
