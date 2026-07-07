/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.diff.config;

import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.dob.meta.MOReference.DeletionPolicy;
import com.top_logic.model.TLReference;

/**
 * Update of the <code>deletion policy</code> of a {@link TLReference}.
 */
@TagName("update-deletion-policy")
public interface UpdateDeletionPolicy extends PartUpdate {

	/**
	 * The new {@link TLReference#getDeletionPolicy() deletion policy}.
	 */
	DeletionPolicy getDeletionPolicy();

	/** @see #getDeletionPolicy() */
	void setDeletionPolicy(DeletionPolicy value);

}
