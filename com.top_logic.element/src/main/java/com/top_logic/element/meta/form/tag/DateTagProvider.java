/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import java.util.Date;

import com.top_logic.element.meta.form.EditContext;
import com.top_logic.layout.form.tag.DateTag;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.model.annotate.DisplayAnnotations;

/**
 * {@link DisplayProvider} for {@link Date} attributes.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DateTagProvider extends IndirectDisplayProvider {

	/**
	 * Singleton {@link DateTagProvider} instance.
	 */
	public static final DateTagProvider INSTANCE = new DateTagProvider();

	private DateTagProvider() {
		// Singleton constructor.
	}

	@Override
	public ControlProvider getControlProvider(EditContext editContext) {
		DateTag dateTag = new DateTag();
		dateTag.setColumns(DisplayAnnotations.inputSize(editContext, 10));
		return dateTag;
	}

}
