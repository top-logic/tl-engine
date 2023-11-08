/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.configured;

import java.io.IOException;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.html.i18n.HtmlResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.documentation.AbstractScriptDocumentation;
import com.top_logic.model.util.TLModelPartRef;

/**
 * Documentation for a script that bases on a {@link ScriptConfiguration}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConfiguredScriptDocumentation extends AbstractScriptDocumentation<ScriptParameter> {

	private ScriptConfiguration _script;

	/**
	 * Creates a {@link ConfiguredScriptDocumentation}.
	 *
	 */
	public ConfiguredScriptDocumentation(ScriptConfiguration script) {
		super(script.getParameters());
		_script = script;
	}

	@Override
	protected String parameterName(ScriptParameter parameter) {
		return parameter.getName();
	}

	@Override
	protected String parameterType(ScriptParameter parameter) {
		TLModelPartRef type = parameter.getType();
		if (type == null) {
			return null;
		}
		String typeName = getTypeName(type);
		if (parameter.isMultiple()) {
			if (parameter.isBag()) {
				if (parameter.isOrdered()) {
					return "List<" + typeName + ">";
				} else {
					return "MultiSet<" + typeName + ">";
				}
			} else {
				if (parameter.isOrdered()) {
					return "OrderedSet<" + typeName + ">";
				} else {
					return "Set<" + typeName + ">";
				}
			}
		} else {
			return typeName;
		}
	}

	@Override
	protected void writeLabel(DisplayContext context, TagWriter out) {
		String label;
		if (_script.getLabel() != null) {
			label = context.getResources().getString(_script.getLabel(), _script.getName());
		} else {
			label = _script.getName();
		}
		out.writeText(label);
	}

	@Override
	protected String scriptName(DisplayContext context) {
		return _script.getName();
	}

	@Override
	protected boolean writeDescription(DisplayContext context, TagWriter out) throws IOException {
		HtmlResKey description = _script.getDescription();
		if (description != null) {
			description.getHtml(context.getResources()).write(context, out);
			return true;
		}
		return false;
	}

	@Override
	protected void writeParameterDescription(DisplayContext context, TagWriter out, ScriptParameter parameter)
			throws IOException {
		ResKey description = parameter.getDescription();
		if (description != null) {
			out.writeText(context.getResources().getString(description));
		}
	}

	@Override
	protected boolean parameterMandatory(ScriptParameter parameter) {
		return parameter.isMandatory();
	}

	@Override
	protected void writeParameterDefaultValue(DisplayContext context, TagWriter out, ScriptParameter parameter) {
		Expr defaultValue = parameter.getDefaultValue();
		if (defaultValue != null) {
			out.writeText(ExprFormat.INSTANCE.getSpecification(defaultValue));
		}
	}

}
