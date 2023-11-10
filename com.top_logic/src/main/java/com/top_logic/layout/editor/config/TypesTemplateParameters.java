/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.config;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelPartRefsFormat;

/**
 * Base interface for layout template parameter definitions to provide a types parameter.
 *
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public interface TypesTemplateParameters extends ConfigurationItem {

	/**
	 * @see #getTypes()
	 */
	String TYPE = TypeTemplateParameters.TYPE;

	/**
	 * Types for all displayed elements.
	 */
	@Name(TYPE)
	@Mandatory
	@Format(TLModelPartRefsFormat.class)
	List<TLModelPartRef> getTypes();

	/**
	 * @see #getTypes()
	 */
	void setTypes(List<TLModelPartRef> value);

}
