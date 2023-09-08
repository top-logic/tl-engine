/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.maven.war.tasks;

import com.top_logic.build.maven.war.WarContext;

/**
 * A task executed while building an application WAR.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface WarTask {

	/**
	 * Performs the build actions.
	 */
	public void run(WarContext context);
	
}
