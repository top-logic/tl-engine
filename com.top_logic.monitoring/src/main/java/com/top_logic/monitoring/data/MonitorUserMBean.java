/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.data;

/**
 * Interface for the MBean to monitor data of the users.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public interface MonitorUserMBean {

	/**
	 * Calculates the number of actual logged in users with respect to the last system start.
	 * 
	 * @return Number of users which are logged at this moment.
	 */
	public int getAmountLoggedInUsers();

}
