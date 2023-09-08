/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.upload;

import java.util.List;

import com.top_logic.basic.io.binary.BinaryData;

/**
 * Component interfaces that allows processing upload data.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface MultiUploadAware {

	/**
	 * Processes the uploaded file.
	 * 
	 * @param files
	 *        The uploaded files.
	 */
	public void receiveFiles(List<BinaryData> files) throws Exception;

}
