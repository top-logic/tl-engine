/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.migration.data;

import com.top_logic.basic.TLID;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLReference;

/**
 * {@link TypePart} representing a {@link TLReference}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface Reference extends TypePart {

	/**
	 * The ID of the {@link TLAssociationEnd} which is implemented by this reference.
	 */
	TLID getEndID();

	/**
	 * Setter for {@link #getEndID()}.
	 */
	void setEndID(TLID end);

}

