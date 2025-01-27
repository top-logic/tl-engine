/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.instance.importer.schema;

import com.top_logic.basic.config.ConfigurationValueBinding;
import com.top_logic.basic.config.annotation.Binding;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.format.XMLFragmentString;

/**
 * A value for a complex primitive type that is serialized by a {@link ConfigurationValueBinding}
 * for the application type.
 */
@TagName("complex-value")
public interface ComplexValueConf extends CustomValueConf {

	/**
	 * The configuration value as generic XML document fragment.
	 */
	@Binding(XMLFragmentString.class)
	String getContents();

	/**
	 * @see #getContents()
	 */
	void setContents(String contents);

}
