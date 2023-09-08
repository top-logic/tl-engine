/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.editor;

import com.top_logic.basic.config.ConfigurationItem;

/**
 * Call-back consuming a new item.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
interface CreateHandler {

	/**
	 * Informs about a created entry.
	 * 
	 * @param entry
	 *        The entry (instance or configuration).
	 * @param elementModel
	 *        The configuration of the given entry. This is the same as the entry, if the entry is
	 *        itself a configuration.
	 */
	void addElement(Object entry, ConfigurationItem elementModel);

}