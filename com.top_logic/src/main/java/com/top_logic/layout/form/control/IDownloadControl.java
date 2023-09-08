/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.WindowScope;

/**
 * Control that renders a download. Typically it uses the command {@link DownloadCommand}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface IDownloadControl extends Control {

	/**
	 * Returns the actual item for download.
	 */
	BinaryDataSource dataItem();

	/**
	 * Resource key of the message to render when no download is available.
	 */
	ResKey noValueKey();

	/**
	 * Whether the download should be offered inline.
	 * 
	 * @see WindowScope#deliverContent(BinaryDataSource, boolean)
	 */
	boolean downloadInline();

}