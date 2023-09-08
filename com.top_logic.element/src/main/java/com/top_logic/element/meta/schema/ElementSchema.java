/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.schema;

import com.top_logic.element.config.ClassConfig;
import com.top_logic.element.config.ModelConfig;
import com.top_logic.element.config.ModuleConfig;
import com.top_logic.model.util.TLModelUtil;

/**
 * Utility methods for accessing {@link ModelConfig} declaration files.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ElementSchema {

	/**
	 * Creates an identifier for an {@link ClassConfig} based on the
	 * {@link ModuleConfig#getName()} and the
	 * {@link ClassConfig#getName()}.
	 * 
	 * @param structureName The {@link ModuleConfig#getName()}.
	 * @param elementName The {@link ClassConfig#getName()}.
	 */
	public static String getElementType(String structureName, String elementName) {
		return TLModelUtil.qualifiedNameDotted(structureName, elementName);
	}

}
