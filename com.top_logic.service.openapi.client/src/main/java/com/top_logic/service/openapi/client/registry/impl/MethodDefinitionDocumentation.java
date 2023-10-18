/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.impl;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;
import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.format.CommonMark;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.service.openapi.client.registry.conf.MethodDefinition;
import com.top_logic.service.openapi.client.registry.conf.ParameterDefinition;
import com.top_logic.util.Resources;

/**
 * {@link HTMLFragment} writing the documentation of a {@link MethodDefinition}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MethodDefinitionDocumentation implements HTMLFragment {

	/**
	 * CSS class for a {@link HTMLConstants#TABLE table} that describes the parameters of a
	 * function.
	 */
	public static String PARAMETER_TABLE_CSS_CLASS = "tlDocTable";

	private MethodDefinition _definition;

	/**
	 * Creates a new {@link MethodDefinitionDocumentation}.
	 */
	public MethodDefinitionDocumentation(MethodDefinition definition) {
		_definition = definition;
	}

	@Override
	public void write(DisplayContext context, TagWriter out) throws IOException {
		out.beginTag(DIV, CLASS_ATTR, SearchBuilder.DOCUMENTATION_CSS_CLASS);

		writeName(out);
		writeSyntax(context, out);
		writeDescription(context, out);
		writeParameters(context, out);

		out.endTag(DIV);
	}

	private void writeName(TagWriter out) {
		out.beginTag(H2);
		out.writeText(_definition.getName());
		out.endTag(H2);
	}

	private void writeSyntax(DisplayContext context, TagWriter out) {
		out.beginTag(H2);
		out.writeText(context.getResources().getString(I18NConstants.MESSAGE_DOC_SYNTAX_HEADER));
		out.endTag(H2);
		out.beginTag(PRE);
		out.beginTag(CODE);
		out.writeText(_definition.getName());
		out.writeText('(');
		List<String> parameterNames = _definition.getParameterNames();
		switch (parameterNames.size()) {
			case 0: {
				break;
			}
			case 1: {
				String parameter = parameterNames.get(0);
				out.writeText(parameter);
				out.writeText(": $");
				out.writeText(parameter);
				break;
			}
			default: {
				boolean firstParam = true;
				for (String parameter : parameterNames) {
					if (!firstParam) {
						out.writeText(",");
					}
					out.writeText("\n    ");
					out.writeText(parameter);
					out.writeText(": $");
					out.writeText(parameter);
					firstParam = false;
				}
				out.writeText("\n");
			}
		}
		out.writeText(')');
		out.endTag(CODE);
		out.endTag(PRE);
	}

	private void writeDescription(DisplayContext context, TagWriter out) throws IOException {
		out.beginTag(H2);
		out.writeText(context.getResources().getString(I18NConstants.MESSAGE_DOC_DESCRIPTION_HEADER));
		out.endTag(H2);
		String description = _definition.getDescription();
		if (description != null) {
			CommonMark.writeCommonMark(out, description);
		}
	}

	private void writeParameters(DisplayContext context, TagWriter out)
			throws IOException {
		List<ParameterDefinition> parameters = _definition.getParameters();
		if (parameters.isEmpty()) {
			return;
		}
		Resources res = context.getResources();
		out.beginTag(H2);
		out.writeText(res.getString(I18NConstants.MESSAGE_DOC_PARAMETERS_HEADER));
		out.endTag(H2);

		HTMLUtil.beginTable(out, null, PARAMETER_TABLE_CSS_CLASS);
		out.beginTag(THEAD);
		writeTH(out, res, I18NConstants.MESSAGE_DOC_PARAMETERS_COLUMN_NAME);
		writeTH(out, res, I18NConstants.MESSAGE_DOC_PARAMETERS_COLUMN_TYPE);
		writeTH(out, res, I18NConstants.MESSAGE_DOC_PARAMETERS_COLUMN_DESCRIPTION);
		writeTH(out, res, I18NConstants.MESSAGE_DOC_PARAMETERS_COLUMN_MANDATORY);
		writeTH(out, res, I18NConstants.MESSAGE_DOC_PARAMETERS_COLUMN_DEFAULT);
		out.endTag(THEAD);
		out.beginTag(TBODY);
		for (ParameterDefinition parameter : parameters) {
			out.beginTag(TR);
			writeParameter(out, parameter);
			out.endTag(TR);
		}
		out.endTag(TBODY);
		HTMLUtil.endTable(out);
	}

	private void writeParameter(TagWriter out, ParameterDefinition parameter) throws IOException {
		writeParameterName(out, parameter);
		writeParameterType(out, parameter);
		writeParameterDescription(out, parameter);
		writeParameterMandatory(out, parameter);
		writeParameterDefaultValue(out, parameter);
	}

	private void writeParameterDefaultValue(TagWriter out, ParameterDefinition parameter) {
		out.beginTag(TD);
		if (!parameter.isRequired()) {
			Expr defaultValue = parameter.getDefaultValue();
			if (defaultValue != null) {
				out.writeText(ExprFormat.INSTANCE.getSpecification(defaultValue));
			}
		}
		out.endTag(TD);
	}

	private void writeParameterMandatory(TagWriter out, ParameterDefinition parameter) {
		out.beginTag(TD);
		out.writeText(MetaLabelProvider.INSTANCE.getLabel(parameter.isRequired()));
		out.endTag(TD);
	}

	private void writeParameterDescription(TagWriter out, ParameterDefinition parameter) throws IOException {
		out.beginTag(TD);
		String description = parameter.getDescription();
		if (description != null) {
			CommonMark.writeCommonMark(out, description);
		}
		out.endTag(TD);
	}

	private void writeParameterType(TagWriter out, ParameterDefinition parameter) {
		out.beginTag(TD);
		TLModelPartRef type = parameter.getType();
		if (type != null) {
			out.writeText(getTypeName(type));
		}
		out.endTag(TD);
	}

	private void writeParameterName(TagWriter out, ParameterDefinition parameter) {
		out.beginTag(TD);
		out.writeText(parameter.getName());
		out.endTag(TD);
	}

	private String getTypeName(TLModelPartRef type) {
		try {
			TLModelPart resolved = type.resolve();
			return MetaLabelProvider.INSTANCE.getLabel(resolved);
		} catch (ConfigurationException ex) {
			return type.qualifiedName();
		}
	}

	private void writeTH(TagWriter out, Resources res, ResKey label) {
		out.beginTag(TH);
		out.writeText(res.getString(label));
		out.endTag(TH);
	}

}

