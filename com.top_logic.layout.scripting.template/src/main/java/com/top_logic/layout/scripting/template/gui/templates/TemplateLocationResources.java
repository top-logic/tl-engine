/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui.templates;

import com.top_logic.basic.generate.CodeUtil;
import com.top_logic.layout.AbstractResourceProvider;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.scripting.template.gui.templates.node.TemplateLocation;

/**
 * {@link ResourceProvider} for {@link TemplateLocation} objects.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TemplateLocationResources extends AbstractResourceProvider {

	/**
	 * Singleton {@link TemplateLocationResources} instance.
	 */
	public static final TemplateLocationResources INSTANCE = new TemplateLocationResources();

	private TemplateLocationResources() {
		// Singleton constructor.
	}

	@Override
	public String getLabel(Object object) {
		if (object == null) {
			return null;
		}
		TemplateLocation location = (TemplateLocation) object;
		String result = location.getName();

		// Heuristics to derive human readable names from absurd template file names.
		if (result.startsWith("Action-")) {
			result = result.substring("Action-".length());
		}
		result = CodeUtil.toUpperCaseStart(result);
		if (result.endsWith(".xml")) {
			result = result.substring(0, result.length() - ".xml".length());
		}
		result = result.replace('_', ' ');
		result = result.replace('-', ' ');
		return result;
	}

}
