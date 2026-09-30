/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.upload;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.format.MemorySizeFormat;

/**
 * Settings limiting the size of a file upload.
 *
 * @implNote Read through {@link UploadSupport#maxUploadSize()}, which falls back to the defaults
 *           declared here if the application does not configure the section.
 */
public interface UploadConfig extends ConfigurationItem {

	/** Configuration name of {@link #getMaxUploadSize()}. */
	String MAX_UPLOAD_SIZE = "max-upload-size";

	/**
	 * Maximum size of an upload.
	 *
	 * <p>
	 * The limit applies to each single uploaded file and to the whole multipart request. A value of
	 * <code>0</code> means that there is no limit.
	 * </p>
	 */
	@Name(MAX_UPLOAD_SIZE)
	@Format(MemorySizeFormat.class)
	@LongDefault(52428800)
	long getMaxUploadSize();

}
