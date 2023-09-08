/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager;

import java.util.Map;
import java.util.Set;

/**
 * {@link LogHandler} that logs nothing.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class NullLogHandler implements LogHandler {

	/** The instance of the {@link NullLogHandler}. */
	public static final NullLogHandler INSTANCE = new NullLogHandler();

	@Override
	public void logSecurityUpdate(Map someNew, Map someRemoved, Map rulesToObjectsMap, Set invalidObjects) {
		// Log nothing
	}

}
