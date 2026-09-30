/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.instances.importer;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.layout.form.values.edit.annotation.AcceptedTypes;

/**
 * Upload form for importing object data in XML format.
 */
public interface UploadForm extends ConfigurationItem {

	/**
	 * Data to upload.
	 * 
	 * <p>
	 * The data must be XML-encoded objects in a format formerly exported from this or another
	 * system. If the imported objects contain references to other object not in the data set, it is
	 * expected that these objects already exist in the system before import.
	 * </p>
	 */
	@Mandatory
	@AcceptedTypes(value = { "text/xml", "*.xml" })
	BinaryData getData();

}
