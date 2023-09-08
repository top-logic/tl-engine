/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.generate;

import com.top_logic.model.TLType;

/**
 * Super class for {@link TLTypeGenerator} generating templates to be modified by application
 * developer.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class TemplateGenerator extends TLTypeGenerator {

	/**
	 * Creates a new {@link TemplateGenerator}.
	 */
	public TemplateGenerator(String packageName, TLType type) {
		super(packageName, type);
	}

}

