/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.modeler.upload;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.io.binary.BinaryData;

/**
 * Form type with a single upload field.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface UploadForm extends ConfigurationItem {

	/**
	 * The uploaded data.
	 */
	BinaryData getData();

	/**
	 * @see #getData()
	 */
	void setData(BinaryData value);

}
