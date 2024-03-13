/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.migration.data;

import com.top_logic.basic.TLID;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;

/**
 * {@link BranchIdType} representing a {@link TLType} generalization.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TypeGeneralization extends BranchIdType {

	/**
	 * {@link TLObject#tIdLocal() Local ID} of the specialization.
	 */
	@Mandatory
	TLID getSource();

	/**
	 * Setter for {@link #getSource()}.
	 */
	void setSource(TLID value);

	/**
	 * {@link TLObject#tIdLocal() Local ID} of the generalization.
	 */
	@Mandatory
	TLID getDestination();

	/**
	 * Setter for {@link #getDestination()}.
	 */
	void setDestination(TLID value);

	/**
	 * The order of the generalization relation.
	 */
	@Mandatory
	int getOrder();

	/**
	 * Setter for {@link #getOrder()}.
	 */
	void setOrder(int value);

}