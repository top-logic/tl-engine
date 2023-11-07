/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.impl;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.format.CommonMark;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.documentation.AbstractScriptDocumentation;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.service.openapi.client.registry.conf.MethodDefinition;
import com.top_logic.service.openapi.client.registry.conf.ParameterDefinition;

/**
 * {@link HTMLFragment} writing the documentation of a {@link MethodDefinition}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MethodDefinitionDocumentation extends AbstractScriptDocumentation<ParameterDefinition> {

	private MethodDefinition _definition;

	/**
	 * Creates a new {@link MethodDefinitionDocumentation}.
	 */
	public MethodDefinitionDocumentation(MethodDefinition definition) {
		super(definition.getParameters());
		_definition = definition;
	}

	@Override
	protected void writeLabel(DisplayContext context, TagWriter out) {
		out.writeText(_definition.getName());
	}

	@Override
	protected String parameterName(ParameterDefinition parameter) {
		return parameter.getName();
	}

	@Override
	protected String parameterType(ParameterDefinition parameter) {
		TLModelPartRef type = parameter.getType();
		if (type == null) {
			return null;
		}
		String typeName = getTypeName(type);
		if (parameter.isMultiple()) {
			return "List<" + typeName + ">";
		} else {
			return typeName;
		}
	}

	@Override
	protected String scriptName(DisplayContext context) {
		return _definition.getName();
	}

	@Override
	protected boolean writeDescription(DisplayContext context, TagWriter out) throws IOException {
		String description = _definition.getDescription();
		if (description != null) {
			CommonMark.writeCommonMark(out, description);
			return true;
		}
		return false;
	}

	@Override
	protected void writeParameterDefaultValue(DisplayContext context, TagWriter out, ParameterDefinition parameter) {
		Expr defaultValue = parameter.getDefaultValue();
		if (defaultValue != null) {
			out.writeText(ExprFormat.INSTANCE.getSpecification(defaultValue));
		}

	}

	@Override
	protected boolean parameterMandatory(ParameterDefinition parameter) {
		return parameter.isRequired();
	}

	@Override
	protected void writeParameterDescription(DisplayContext context, TagWriter out,
			ParameterDefinition parameter) throws IOException {
		String description = parameter.getDescription();
		if (description != null) {
			CommonMark.writeCommonMark(out, description);
		}
	}

}

