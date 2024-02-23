/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.gui;

import com.top_logic.basic.config.ResourceDeclaration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Resource which uses the <code>script</code> tag to embed a javascript file.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public interface ScriptResourceDeclaration extends ResourceDeclaration {

	/**
	 * Name of the "type" property.
	 */
	String TYPE_ATTRIBUTE = "type";

	/**
	 * Specifies the type of the script resource.
	 * 
	 * <p>
	 * When using native javascript modules with export and import the type have to be
	 * <code>module</code>.
	 * </p>
	 */
	@Name(TYPE_ATTRIBUTE)
	@StringDefault(HTMLConstants.JAVASCRIPT_TYPE_VALUE)
	String getType();

	/**
	 * Sets the value of {@link #getType()}.
	 */
	void setType(String type);

	/**
	 * The module specifier to map the imported <em>ES6</em> module to.
	 * 
	 * <p>
	 * Use this only when {@link #getType() type} is <code>module</code>.
	 * </p>
	 * 
	 * <p>
	 * From a module referenced with specifier <code>service</code>, contents can be imported with
	 * the following statement:
	 * </p>
	 * 
	 * <pre>
	 * <code>
	 * import {...} from 'service';
	 * </code>
	 * </pre>
	 */
	@Nullable
	@Label("Specifier")
	String getAs();

}
