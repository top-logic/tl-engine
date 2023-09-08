/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataSource;

/**
 * Plugin to modify a {@link BinaryData} before delivering it via
 * {@link WindowScope#deliverContent(BinaryDataSource, boolean)} to the client.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface DownloadModification {

	/**
	 * Modifies the given {@link BinaryData}.
	 * 
	 * @param data
	 *        The data that should be delivered to the client.
	 * @param showInline
	 *        Whether the content is displayed inline.
	 * 
	 * @return A modified version of the given {@link BinaryData} or the {@link BinaryData} itself.
	 * 
	 * @see WindowScope#deliverContent(BinaryDataSource, boolean)
	 */
	BinaryData modifyDownload(BinaryData data, boolean showInline);

}

