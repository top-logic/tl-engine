/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.layout;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;

/**
 * Configuration of the way how the the data will be transferred.
 * 
 * @see MultiPartBodyTransferType
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Label("With transfer type for multiple body")
public interface WithMultiPartBodyTransferType extends ConfigurationItem {

	/** Name of the configuration option {@link #getTransferType()}. */
	String TRANSFER_TYPE = "transfer-type";

	/**
	 * The way how the data of the body are transfered.
	 */
	@Name(TRANSFER_TYPE)
	@Mandatory
	MultiPartBodyTransferType getTransferType();

	/**
	 * Setter for {@link #getTransferType()}.
	 */
	void setTransferType(MultiPartBodyTransferType value);

}
