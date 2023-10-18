/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template.config;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.html.template.ExpressionTemplate;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.html.template.HTMLTemplateUtils;
import com.top_logic.html.template.StartTagTemplate;
import com.top_logic.html.template.TagAttributeTemplate;
import com.top_logic.html.template.TagTemplate;
import com.top_logic.html.template.expr.VariableExpression;
import com.top_logic.layout.Control;
import com.top_logic.mig.html.HTMLConstants;

/**
 * {@link HTMLTemplateFragmentFormat} that ensures that the template is a {@link TagTemplate} with
 * an {@link HTMLConstants#ID_ATTR id} attribute.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class HTMLTagFormat extends HTMLTemplateFragmentFormat {

	private static final TagAttributeTemplate ID_ATTR = new TagAttributeTemplate(0, 0, HTMLConstants.ID_ATTR,
		new ExpressionTemplate(new VariableExpression(0, 0, Control.ID)));

	@Override
	protected HTMLTemplateFragment getValueNonEmpty(String propertyName, CharSequence propertyValue)
			throws ConfigurationException {
		String html = propertyValue.toString();
		HTMLTemplateFragment template = HTMLTemplateUtils.parse(propertyName, html);

		if (template instanceof TagTemplate) {
			StartTagTemplate start = ((TagTemplate) template).getStart();
			start.getAttributes().removeIf(a -> a.getName().equals(HTMLConstants.ID_ATTR));
			start.getAttributes().add(ID_ATTR);
		} else {
			StartTagTemplate start = new StartTagTemplate(1, 1, HTMLConstants.DIV);
			start.addAttribute(ID_ATTR);
			template = new TagTemplate(start, template);
		}

		return new ConfiguredTemplate(template, html);
	}

}
