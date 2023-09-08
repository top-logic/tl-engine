/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template.model.internal;

import com.top_logic.layout.form.template.model.Embedd;

/**
 * Thrown if {@link Embedd} could not render.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class TemplateException extends RuntimeException {

	public TemplateException(String message) {
		super(message);
	}

}