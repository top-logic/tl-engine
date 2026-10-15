/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.handlers;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;

/**
 * Configuration of a {@link Handler} resolving IDs: How an ID is handled that is never resolved.
 */
public interface UnresolvedIdConfig extends ConfigurationItem {

	/**
	 * @see #getIgnoreUnresolved()
	 */
	String IGNORE_UNRESOLVED = "ignore-unresolved";

	/**
	 * Whether an ID that is still not resolved at the end of the import is ignored instead of being
	 * reported as error.
	 * 
	 * <p>
	 * An ignored ID leaves the reference unset. Everything that is applied to the referenced object
	 * (e.g. the nested content of an object reference) is dropped. An ID that is defined later in
	 * the import data is resolved as usual.
	 * </p>
	 * 
	 * <p>
	 * If another reference to the same ID does not ignore it, the unresolved ID is reported
	 * nevertheless.
	 * </p>
	 */
	@Name(IGNORE_UNRESOLVED)
	boolean getIgnoreUnresolved();

}
