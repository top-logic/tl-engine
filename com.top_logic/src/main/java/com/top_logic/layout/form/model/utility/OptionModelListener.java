/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model.utility;

/**
 * Observer of a {@link OptionModel}.
 * 
 * @see OptionModel#addOptionListener(OptionModelListener)
 */
public interface OptionModelListener {

	/**
	 * Notifies of a change in the given {@link OptionModel}.
	 *
	 * @param sender
	 *        The {@link OptionModel} whose options changed.
	 */
	void notifyOptionsChanged(OptionModel<?> sender);

}
