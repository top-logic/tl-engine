/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import com.top_logic.base.office.excel.ExcelAccess;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.DefaultValueProvider;
import com.top_logic.basic.config.annotation.DefaultValueProviderShared;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ComplexDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;

/**
 * Configuration of an Excel export.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ExportConfig extends ConfigurationItem {

	/** Configuration name of {@link #getDownloadNameKey()}. */
	String DOWNLOAD_NAME_KEY = "downloadNameKey";

	/** Configuration name of {@link #getTemplateName()}. */
	String TEMPLATE_NAME = "templateName";

	/** Configuration name of {@link #getAutofitColumns()}. */
	String AUTOFIT_COLUMNS = "autofitColumns";

	/**
	 * {@link DefaultValueProvider} defining the default {@link ExportConfig#getDownloadNameKey()
	 * download name key}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	class DefaultDownloadKey extends DefaultValueProviderShared {
		@Override
		public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
			return I18NConstants.DOWNLOAD_FILE_KEY;
		}
	}

	/**
	 * Resource key for the name of the download file.
	 */
	@Name(DOWNLOAD_NAME_KEY)
	@ComplexDefault(DefaultDownloadKey.class)
	@InstanceFormat
	ResKey getDownloadNameKey();

	/**
	 * Setter for {@link #getDownloadNameKey()}.
	 */
	void setDownloadNameKey(ResKey value);

	/**
	 * Name of the Excel template that is used to export table.
	 */
	@Name(TEMPLATE_NAME)
	@StringDefault("defaultTemplate.xlsx")
	String getTemplateName();

	/**
	 * Setter for {@link #getTemplateName()}.
	 */
	void setTemplateName(String value);

	/**
	 * Flag, if columns have to be auto fitted to matching width.
	 * 
	 * @see ExcelAccess#setValuesDirect(java.io.InputStream, java.io.File,
	 *      com.top_logic.base.office.excel.ExcelValue[], boolean)
	 */
	@Name(AUTOFIT_COLUMNS)
	@BooleanDefault(true)
	boolean getAutofitColumns();

	/**
	 * Setter for {@link #getAutofitColumns()}.
	 */
	void setAutofitColumns(boolean value);

}

