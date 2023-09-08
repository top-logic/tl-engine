/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.demo;

import com.top_logic.element.ElementStartStop;

/**
 * Handler for application server to start and stop the <i>TopLogic</i> context.
 * 
 * @author    <a href="mailto:support@top-logic.com"><i>TopLogic</i> Support</a>
 */
public class KafkaDemoStartStopListener extends ElementStartStop {

    /**
     * Creates a {@link KafkaDemoStartStopListener}.
     */
    public KafkaDemoStartStopListener() {
        /* needed for ServletContextListener */
    }

}
