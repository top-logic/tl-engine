/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server;

import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.values.edit.AnnotationControlProvider;
import com.top_logic.layout.form.values.edit.EditorFactory;

/**
 * {@link AnnotationControlProvider} displaying an {@link ApplicationRelativeURL} as link to the
 * full URL with the {@link DisplayFullPathTemplate}.
 */
public class ApplicationRelativeURLProvider implements AnnotationControlProvider<ApplicationRelativeURL> {

	private static final ControlProvider LINK = new DisplayFullPathTemplate();

	@Override
	public ControlProvider getControlProvider(EditorFactory editorFactory, PropertyDescriptor property,
			ApplicationRelativeURL annotation) {
		return LINK;
	}

}
