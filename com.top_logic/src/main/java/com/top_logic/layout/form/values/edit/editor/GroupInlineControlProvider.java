/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.editor;

import static com.top_logic.layout.form.template.model.Templates.*;

import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.template.model.Embedd;
import com.top_logic.layout.form.template.model.internal.TemplateAnnotation;

/**
 * Inlines a {@link FormGroup}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class GroupInlineControlProvider extends TemplateAnnotation {

	private static final Embedd TEMPLATE = embedd(
		member("outerContainer",
			embedd(
				member(
					ItemEditor.CONTENT_CONTAINER_NAME))));

	/**
	 * Creates a {@link GroupInlineControlProvider}.
	 */
	public GroupInlineControlProvider() {
		super(TEMPLATE);
	}

}
