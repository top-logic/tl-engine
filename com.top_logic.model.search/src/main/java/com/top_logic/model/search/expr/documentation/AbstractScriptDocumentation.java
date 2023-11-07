/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.documentation;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;
import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.util.Resources;

/**
 * {@link HTMLFragment} creating the documentation for an TL-Script.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractScriptDocumentation<T> implements HTMLFragment {

	private final List<T> _parameters;

	/**
	 * Creates a {@link AbstractScriptDocumentation}.
	 *
	 * @param parameters
	 *        List of parameters of the script to create the parameters table and syntax section.
	 */
	public AbstractScriptDocumentation(List<T> parameters) {
		_parameters = parameters;
	}

	@Override
	public void write(DisplayContext context, TagWriter out) throws IOException {
		out.beginTag(DIV, CLASS_ATTR, DocumentationConstants.DOCUMENTATION_CSS_CLASS);

		writeLabelSection(context, out);
		writeSyntaxDescription(context, out);
		writeDescriptionSection(context, out);
		writeParametersTable(context, out);


		out.endTag(DIV);
	}

	private void writeLabelSection(DisplayContext context, TagWriter out) throws IOException {
		out.beginTag(H2);
		writeLabel(context, out);
		out.endTag(H2);

	}

	/**
	 * Write the label of the script.
	 * 
	 * @implNote The label is part of an {@value #H2} section.
	 *
	 * @param context
	 *        The context in which rendering occurs.
	 * @param out
	 *        {@link TagWriter} to write content to.
	 */
	protected abstract void writeLabel(DisplayContext context, TagWriter out) throws IOException;

	private void writeSyntaxDescription(DisplayContext context, TagWriter out) {
		out.beginTag(H2);
		out.writeText(context.getResources().getString(I18NConstants.MESSAGE_DOC_SYNTAX_HEADER));
		out.endTag(H2);
		out.beginTag(PRE);
		out.beginTag(CODE);
		out.writeText(scriptName(context));
		out.writeText('(');
		switch (_parameters.size()) {
			case 0: {
				break;
			}
			case 1: {
				String parameterName = parameterName(_parameters.get(0));
				out.writeText(parameterName);
				out.writeText(": $");
				out.writeText(parameterName);
				break;
			}
			default: {
				boolean firstParam = true;
				for (T parameter : _parameters) {
					String parameterName = parameterName(parameter);

					if (!firstParam) {
						out.writeText(",");
					}
					out.writeText("\n    ");
					out.writeText(parameterName);
					out.writeText(": $");
					out.writeText(parameterName);
					firstParam = false;
				}
				out.writeText("\n");
			}
		}
		out.writeText(')');
		out.endTag(CODE);
		out.endTag(PRE);
	}

	/**
	 * The name of the parameter.
	 *
	 * @param parameter
	 *        The parameter currently processed.
	 */
	protected abstract String parameterName(T parameter);

	/**
	 * The type of the parameter.
	 *
	 * @param parameter
	 *        The parameter currently processed.
	 * @return Type of the parameter. May be <code>null</code>.
	 */
	protected abstract String parameterType(T parameter);

	/**
	 * The name of the script.
	 *
	 * @param context
	 *        {@link DisplayContext} in which the documentation is rendered.
	 */
	protected abstract String scriptName(DisplayContext context);

	private void writeDescriptionSection(DisplayContext context, TagWriter out) throws IOException {
		out.beginTag(H2);
		out.writeText(context.getResources().getString(I18NConstants.MESSAGE_DOC_DESCRIPTION_HEADER));
		out.endTag(H2);
		boolean hasDescription = writeDescription(context, out);
		if (!hasDescription) {
			out.writeText(context.getResources().getString(I18NConstants.MESSAGE_DOC_NO_DESCRIPTION));
		}
	}

	/**
	 * Write the description of the script, if there is one. If no description is available a
	 * replacement text is written.
	 * 
	 * @param context
	 *        The context in which rendering occurs.
	 * @param out
	 *        {@link TagWriter} to write content to.
	 * 
	 * @return Whether a description was written. If <code>false</code>, a replacement text is
	 *         written.
	 */
	protected abstract boolean writeDescription(DisplayContext context, TagWriter out) throws IOException;

	private void writeParametersTable(DisplayContext context, TagWriter out)
			throws IOException {
		if (_parameters.isEmpty()) {
			return;
		}
		Resources res = context.getResources();
		out.beginTag(H2);
		out.writeText(res.getString(I18NConstants.MESSAGE_DOC_PARAMETERS_HEADER));
		out.endTag(H2);

		HTMLUtil.beginTable(out, null, DocumentationConstants.PARAMETER_TABLE_CSS_CLASS);
		out.beginTag(THEAD);
		writeTH(out, res, I18NConstants.MESSAGE_DOC_PARAMETERS_COLUMN_NAME);
		writeTH(out, res, I18NConstants.MESSAGE_DOC_PARAMETERS_COLUMN_TYPE);
		writeTH(out, res, I18NConstants.MESSAGE_DOC_PARAMETERS_COLUMN_DESCRIPTION);
		writeTH(out, res, I18NConstants.MESSAGE_DOC_PARAMETERS_COLUMN_MANDATORY);
		writeTH(out, res, I18NConstants.MESSAGE_DOC_PARAMETERS_COLUMN_DEFAULT);
		out.endTag(THEAD);
		out.beginTag(TBODY);
		for (T parameter : _parameters) {
			out.beginTag(TR);
			writeParameter(context, out, parameter);
			out.endTag(TR);
		}
		out.endTag(TBODY);
		HTMLUtil.endTable(out);
	}

	private void writeTH(TagWriter out, Resources res, ResKey label) {
		out.beginTag(TH);
		out.writeText(res.getString(label));
		out.endTag(TH);
	}

	private void writeParameter(DisplayContext context, TagWriter out, T parameter) throws IOException {
		writeParameterNameCell(out, parameter);
		writeParameterTypeCell(out, parameter);
		writeParameterDescriptionCell(context, out, parameter);
		writeParameterMandatoryCell(out, parameter);
		writeParameterDefaultValueCell(context, out, parameter);
	}

	private void writeParameterDefaultValueCell(DisplayContext context, TagWriter out, T parameter) {
		out.beginTag(TD);
		if (!parameterMandatory(parameter)) {
			writeParameterDefaultValue(context, out, parameter);
		}
		out.endTag(TD);
	}

	/**
	 * Writes a potential default for the parameter if there is one.
	 * 
	 * @implNote The parameter default is part of a {@link #TD}.
	 *
	 * @param context
	 *        The context in which rendering occurs.
	 * @param out
	 *        {@link TagWriter} to write content to.
	 * @param parameter
	 *        The parameter that is currently processed.
	 */
	protected abstract void writeParameterDefaultValue(DisplayContext context, TagWriter out, T parameter);

	private void writeParameterMandatoryCell(TagWriter out, T parameter) {
		out.beginTag(TD);
		out.writeText(MetaLabelProvider.INSTANCE.getLabel(parameterMandatory(parameter)));
		out.endTag(TD);
	}

	/**
	 * Whether the parameter is mandatory.
	 *
	 * @param parameter
	 *        The parameter currently processed.
	 */
	protected abstract boolean parameterMandatory(T parameter);

	private void writeParameterDescriptionCell(DisplayContext context, TagWriter out, T parameter) throws IOException {
		out.beginTag(TD);
		writeParameterDescription(context, out, parameter);
		out.endTag(TD);
	}

	/**
	 * Writes a potential description for the parameter if there is one.
	 * 
	 * @implNote The parameter default is part of a {@link #TD}.
	 *
	 * @param context
	 *        The context in which rendering occurs.
	 * @param out
	 *        {@link TagWriter} to write content to.
	 * @param parameter
	 *        The parameter that is currently processed.
	 */
	protected abstract void writeParameterDescription(DisplayContext context, TagWriter out, T parameter)
			throws IOException;

	private void writeParameterTypeCell(TagWriter out, T parameter) {
		out.beginTag(TD);
		out.writeText(parameterType(parameter));
		out.endTag(TD);
	}

	private void writeParameterNameCell(TagWriter out, T parameter) {
		out.beginTag(TD);
		out.writeText(parameterName(parameter));
		out.endTag(TD);
	}

	/**
	 * Determines a string representation for the given {@link TLModelPartRef}.
	 */
	protected static String getTypeName(TLModelPartRef type) {
		TLModelPart resolved = type.resolve();
		return MetaLabelProvider.INSTANCE.getLabel(resolved);
	}

}
