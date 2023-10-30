/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template.expr;

import com.top_logic.html.template.RawTemplateFragment;
import com.top_logic.html.template.TemplateExpression;
import com.top_logic.html.template.TemplateNode;
import com.top_logic.html.template.VariableTemplate;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.template.NoSuchPropertyException;
import com.top_logic.layout.template.WithProperties;

/**
 * Access to a model variable to be either directly rendered or used in a
 * {@link TemplateExpression}.
 */
public class VariableExpression extends TemplateNode implements TemplateExpression {

	private final String _name;

	/**
	 * Creates a {@link VariableExpression}.
	 *
	 * @param name
	 *        The name of the model property to access.
	 */
	public VariableExpression(int line, int column, String name) {
		super(line, column);
		_name = name;
	}

	@Override
	public Object eval(DisplayContext context, WithProperties properties) {
		try {
			return properties.getPropertyValue(_name);
		} catch (NoSuchPropertyException ex) {
			throw WithProperties.reportError(properties, _name);
		}
	}

	@Override
	public RawTemplateFragment toFragment() {
		return new VariableTemplate(_name);
	}

}
