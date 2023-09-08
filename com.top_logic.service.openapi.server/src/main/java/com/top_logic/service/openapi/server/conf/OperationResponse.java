/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.conf;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.codeedit.control.CodeEditorControl;
import com.top_logic.layout.codeedit.control.EditorControlConfig;
import com.top_logic.layout.codeedit.editor.DefaultCodeEditor;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.layout.form.values.edit.annotation.PropertyEditor;
import com.top_logic.service.openapi.common.document.Described;
import com.top_logic.service.openapi.server.parameter.ConcreteRequestParameter.Config.VisibleOnObjectParam;
import com.top_logic.service.openapi.server.parameter.ParameterFormat;

/**
 * Configuration of a potential response of an {@link Operation}.
 * 
 * @see Operation
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@DisplayOrder({
	OperationResponse.RESPONSE_CODE,
	OperationResponse.FORMAT,
	OperationResponse.SCHEMA,
	OperationResponse.EXAMPLE,
	OperationResponse.DESCRIPTION,
})
public interface OperationResponse extends Described {

	/** Configuration name of {@link #getResponseCode()}.*/
	String RESPONSE_CODE = "response-code";

	/** Configuration name of {@link #getFormat()}.*/
	String FORMAT = "format";
	/** Configuration name of {@link #getExample()}.*/
	String EXAMPLE = "example";

	/** Configuration name of {@link #getSchema()}.*/
	String SCHEMA = "schema";

	/**
	 * The code of this response, e.g. 200,400, or 501.
	 */
	@Name(RESPONSE_CODE)
	String getResponseCode();

	/**
	 * Setter for {@link #getResponseCode()}.
	 */
	void setResponseCode(String value);

	/**
	 * The schema defining the type used for the parameter.
	 */
	@Name(FORMAT)
	ParameterFormat getFormat();

	/**
	 * Setter for {@link #getFormat()}.
	 */
	void setFormat(ParameterFormat value);

	/**
	 * The schema of the response object.
	 */
	@DynamicMode(fun = VisibleOnObjectParam.class, args = @Ref(FORMAT))
	@EditorControlConfig(language = CodeEditorControl.MODE_JSON, prettyPrinting = true)
	@PropertyEditor(DefaultCodeEditor.class)
	@Name(SCHEMA)
	@Nullable
	String getSchema();

	/**
	 * Setter for {@link #getSchema()}.
	 */
	void setSchema(String value);

	/**
	 * An example for a potential response object.
	 */
	@DynamicMode(fun = VisibleOnObjectParam.class, args = @Ref(FORMAT))
	@EditorControlConfig(language = CodeEditorControl.MODE_JSON, prettyPrinting = true)
	@PropertyEditor(DefaultCodeEditor.class)
	@Name(EXAMPLE)
	@Nullable
	String getExample();

	/**
	 * Setter for {@link #getExample()}.
	 */
	void setExample(String value);
}

