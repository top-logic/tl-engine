/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.logger;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.util.ResKey;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public interface ImportLogger {	
	
	public static String INFO="INFO";
	public static String WARN="WARN";
	public static String ERROR="ERROR";
	public static String DATA_ALERT="DATA_ALERT";
	public static String DATA_MESSAGE="DATA_MESSAGE";
	
	
	public interface Config extends PolymorphicConfiguration<ImportLogger> {
		String getResourcePrefix();
	}
	
	void error(Object caller, ResKey key, Object... params);	
	void error(Object caller, ResKey key, Throwable e, Object... params);	
	void warn(Object caller, ResKey key, Object... params);
	void info(Object caller, ResKey key, Object... params);
}
