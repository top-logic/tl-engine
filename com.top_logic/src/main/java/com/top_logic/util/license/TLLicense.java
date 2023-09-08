/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.license;

import java.util.Date;

import com.top_logic.basic.config.ConfigurationItem;

/**
 * Information contained in a TopLogic license.
 */
public interface TLLicense extends ConfigurationItem {

	/**
	 * TODO
	 */
	Date getValidity();

	/**
	 * TODO
	 */
	Date getExpireDate();

	/**
	 * Allowed number of cluster nodes running the application.
	 */
	int getClusterSize();

	/**
	 * Number of users accounts permitted by the license.
	 */
	int getUsers();

	/**
	 * Number of restricted user accounts (read-only) permitted by the license.
	 */
	int getRestrictedUsers();

	/**
	 * Whether this license is for development only.
	 */
	boolean getDevelopment();

	/**
	 * The license key.
	 */
	String getLicenseKey();

}
