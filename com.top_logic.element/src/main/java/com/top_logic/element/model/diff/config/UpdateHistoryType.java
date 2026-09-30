/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.diff.config;

import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.model.TLReference;

/**
 * Update of the <code>history type</code> of a {@link TLReference}.
 */
@TagName("update-history-type")
public interface UpdateHistoryType extends PartUpdate {

	/**
	 * The new {@link TLReference#getHistoryType() history type}.
	 */
	HistoryType getHistoryType();

	/** @see #getHistoryType() */
	void setHistoryType(HistoryType value);

}
