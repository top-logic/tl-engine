/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.template;

import java.util.Map;

import com.top_logic.basic.config.template.TemplateExpression.Template;

/**
 * Scope based on a {@link Map} of named {@link Template}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MapScope implements TemplateScope {

	private final Map<String, TemplateExpression> _templates;

	/**
	 * Creates a {@link MapScope}.
	 * 
	 * @param templates
	 *        The {@link Template}s indexed by name.
	 */
	public MapScope(Map<String, TemplateExpression> templates) {
		_templates = templates;
	}

	@Override
	public TemplateExpression getTemplate(String name, boolean optional) {
		return _templates.get(name);
	}

}
