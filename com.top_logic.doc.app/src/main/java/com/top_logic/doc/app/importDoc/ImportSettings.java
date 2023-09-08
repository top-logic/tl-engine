/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.app.importDoc;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.layout.form.values.edit.annotation.AcceptedTypes;

/**
 * Description of the HTML import dialog model.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@DisplayOrder({
	ImportSettings.IMPORT_DATA,
	ImportSettings.EXTRACT_TITLES,

})
public interface ImportSettings extends ConfigurationItem {

	/**
	 * @see #getExtractTitles()
	 */
	String EXTRACT_TITLES = "extract-titles";

	/**
	 * @see #getImportData()
	 */
	String IMPORT_DATA = "import-data";

	/**
	 * The data to import.
	 */
	@AcceptedTypes({ "application/zip", "text/html" })
	@Name(IMPORT_DATA)
	BinaryData getImportData();

	/**
	 * Whether to extract page titles from from contents.
	 */
	@Name(EXTRACT_TITLES)
	boolean getExtractTitles();

}
