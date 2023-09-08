/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.document;

import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.codeedit.control.CodeEditorControl;
import com.top_logic.layout.codeedit.control.EditorControlConfig;
import com.top_logic.layout.codeedit.editor.DefaultCodeEditor;
import com.top_logic.layout.form.values.edit.annotation.PropertyEditor;
import com.top_logic.layout.form.values.edit.annotation.RenderWholeLine;

/**
 * A named <i>OpenAPI</i> schema.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@DisplayOrder({
	SchemaObject.NAME_ATTRIBUTE,
	SchemaObject.SCHEMA,
})
public interface SchemaObject extends NamedConfigMandatory {

	/** Configuration name for {@link #getSchema()}. */
	String SCHEMA = "schema";

	/**
	 * The schema in JSON form.
	 */
	@Name(SCHEMA)
	@EditorControlConfig(language = CodeEditorControl.MODE_JSON, prettyPrinting = true)
	@PropertyEditor(DefaultCodeEditor.class)
	@RenderWholeLine
	@Mandatory
	String getSchema();

	/**
	 * Setter for {@link #getSchema()}.
	 */
	void setSchema(String value);

}
