/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.monitor;

import com.top_logic.basic.config.annotation.Label;

/**
 * Plug-in for the {@link ApplicationMonitor} service.
 * 
 * <p>
 * A {@link MonitorComponent} retrieves status information for a certain part of the platform. The
 * status reports in form of a {@link MonitorResult} are displayed in the
 * {@link ApplicationMonitorComponent}.
 * </p>
 * 
 * @see #checkState(MonitorResult)
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
@Label("Application health monitor")
public interface MonitorComponent {

    /**
     * Returns a user understandable name of the implementing class.
     *
     * If the returned value is empty, the value will not be displayed in the
     * user interface.
     *
     * @return    The name of the class, must not be <code>null</code> but
     *            may be an empty string as described above.
     */
    public String getName();

    /**
     * Returns the description of the functionality of the implementing class.
     *
     * @return    The description of the function of this class, must not be 
     *            <code>null</code>, but may be an empty string ass described
     *            above.
     */
    public String getDescription();

	/**
	 * Check the current state of this component.
	 * 
	 * <p>
	 * The result of the internal test must be stored into the given result.
	 * </p>
	 * 
	 * <p>
	 * Checking of the state must not take more than few milliseoncds and must not require may
	 * resources, as this function may be called at regular intervals (e.g all ten minutes).
	 * </p>
	 * 
	 * @see MonitorResult#addMessage(MonitorMessage)
	 */
	void checkState(MonitorResult result);
}
