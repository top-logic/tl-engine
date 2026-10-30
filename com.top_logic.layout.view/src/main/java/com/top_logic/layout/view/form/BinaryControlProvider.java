/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactBinaryFieldControl;
import com.top_logic.layout.react.field.FieldControlRegistry;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.field.ReactFieldControlProvider;
import com.top_logic.layout.react.field.UploadValueFactory;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;

/**
 * {@link ReactFieldControlProvider} for binary ({@code tl.core:Binary}) attributes editing one file
 * per field.
 *
 * <p>
 * Renders a file upload (drag-and-drop plus button) in edit mode and a download link in view mode
 * via {@link ReactBinaryFieldControl}. The file picker offers the
 * {@link FieldSpec#getAcceptedTypes() accepted types} of the field, each file up to the
 * {@link FieldSpec#getMaxUploadSize() upload limit} of the field. A multi-valued field is a list
 * with one such control per file, to which several files are added at once.
 * </p>
 *
 * @see FieldControlRegistry#BINARY
 */
@Label("File upload")
@InApp
@TargetType(TLTypeKind.BINARY)
public class BinaryControlProvider implements ReactFieldControlProvider {

	@Override
	public ReactControl createControl(ReactContext context, FieldSpec field, FieldModel model) {
		return FieldControlRegistry.BINARY.createControl(context, field, model);
	}

	@Override
	public UploadValueFactory uploadValues(FieldSpec field) {
		return FieldControlRegistry.BINARY.uploadValues(field);
	}

	@Override
	public String previewText(FieldSpec field, Object value) {
		return FieldControlRegistry.BINARY.previewText(field, value);
	}

}
