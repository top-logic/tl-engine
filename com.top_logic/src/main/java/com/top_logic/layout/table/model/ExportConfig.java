/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.layout.table.export.ComponentDownloadName;
import com.top_logic.layout.table.export.DownloadNameProvider;

/**
 * Base configuration for an export.
 */
@Abstract
public interface ExportConfig extends ConfigurationItem {

	/** @see #getDownloadNameProvider() */
	String DOWNLOAD_NAME_PROVIDER = "downloadNameProvider";

	/**
	 * Provider for the download-name.
	 */
	@Name(DOWNLOAD_NAME_PROVIDER)
	@ItemDefault(ComponentDownloadName.class)
	@NonNullable
	PolymorphicConfiguration<? extends DownloadNameProvider> getDownloadNameProvider();

	/**
	 * @see #getDownloadNameProvider()
	 */
	void setDownloadNameProvider(PolymorphicConfiguration<? extends DownloadNameProvider> value);

}
