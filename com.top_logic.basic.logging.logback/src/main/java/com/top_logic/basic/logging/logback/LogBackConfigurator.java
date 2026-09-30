/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.logging.logback;

import java.net.URL;

import com.top_logic.basic.logging.LogConfigurator;

/**
 * {@link LogConfigurator} for use with LogBack logging.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LogBackConfigurator extends LogConfigurator {

	@Override
	public void configure(URL configuration) {
		// Not supported.
	}

	@Override
	public void configureStdout(String aLevel) {
		// Not supported.
	}

	@Override
	public void reset() {
		// Not supported.
	}

	@Override
	public String getLogMark(String key) {
		return null; // Not supported.
	}

	@Override
	public void addLogMark(String key, String value) {
		// Not supported.
	}

	@Override
	public void removeLogMark(String key) {
		// Not supported.
	}

}
