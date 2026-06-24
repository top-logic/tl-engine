/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react;

import com.top_logic.element.ElementStartStop;

/**
 * Servlet context listener that starts and stops the <i>TopLogic</i> context of the React-only demo
 * application.
 *
 * @author <a href="mailto:support@top-logic.com"><i>TopLogic</i> Support</a>
 */
public class DemoReactStartStopListener extends ElementStartStop {

	/**
	 * Creates a {@link DemoReactStartStopListener}.
	 */
	public DemoReactStartStopListener() {
		/* needed for ServletContextListener */
	}

}
