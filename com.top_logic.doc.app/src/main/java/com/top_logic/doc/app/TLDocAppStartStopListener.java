/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.app;

import com.top_logic.element.ElementStartStop;

/**
 * Handler for application server to start and stop the <i>TopLogic</i> context.
 * 
 * @author    <a href="mailto:support@top-logic.com"><i>TopLogic</i> Support</a>
 */
public class TLDocAppStartStopListener extends ElementStartStop {

    /**
     * Creates a {@link TLDocAppStartStopListener}.
     */
    public TLDocAppStartStopListener() {
        /* needed for ServletContextListener */
    }

}
