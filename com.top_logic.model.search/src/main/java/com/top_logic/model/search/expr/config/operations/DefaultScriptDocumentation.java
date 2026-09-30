/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.search.expr.config.operations;

import java.io.IOException;
import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.model.search.expr.documentation.AbstractScriptDocumentation;

/**
 * Default {@link AbstractScriptDocumentation} implementation.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultScriptDocumentation extends AbstractScriptDocumentation<DefaultScriptDocumentation.DocumentationParameter> {

	/**
	 * Definition of a TL-Script parameter.
	 */
	public static class DocumentationParameter {

		String _name;

		boolean _mandatory;

		HTMLFragment _description;

		String _defaultValue;

		String _type;

		/**
		 * Creates a new {@link DocumentationParameter}.
		 */
		public DocumentationParameter(String name) {
			_name = name;
		}

		/**
		 * Marks the parameter as mandatory.
		 * 
		 * @see #setOptional(String)
		 */
		public DocumentationParameter setMandatory() {
			_mandatory = true;
			_defaultValue = null;
			return this;
		}

		/**
		 * Marks the parameter as optional.
		 * 
		 * @see #setMandatory()
		 */
		public DocumentationParameter setOptional(String defaultValue) {
			_mandatory = false;
			_defaultValue = defaultValue;
			return this;
		}

		/**
		 * Sets the description for the parameter.
		 */
		public DocumentationParameter setDescription(HTMLFragment description) {
			_description = description;
			return this;
		}

		/**
		 * Sets the type for the parameter.
		 */
		public DocumentationParameter setType(String type) {
			_type = type;
			return this;
		}

	}

	private final String _scriptName;

	private ResKey _label;

	private HTMLFragment _description;

	private String _returnType;

	private HTMLFragment _returnDescription;

	/**
	 * Creates a new {@link DefaultScriptDocumentation}.
	 */
	public DefaultScriptDocumentation(String scriptName, List<DocumentationParameter> docuParams) {
		super(docuParams);
		_scriptName = scriptName;
	}

	/**
	 * Sets the label for the script. If not set, the script name is used.
	 */
	public DefaultScriptDocumentation setLabel(ResKey label) {
		_label = label;
		return this;
	}

	/**
	 * Sets the description for the script.
	 */
	public DefaultScriptDocumentation setDescription(HTMLFragment description) {
		_description = description;
		return this;
	}

	/**
	 * Sets the return type of the script.
	 */
	public DefaultScriptDocumentation setReturnType(String returnType) {
		_returnType = StringServices.nonEmpty(returnType);
		return this;
	}

	/**
	 * Sets the description of the return value of the script.
	 */
	public DefaultScriptDocumentation setReturnDescription(HTMLFragment returnDescription) {
		_returnDescription = returnDescription;
		return this;
	}

	@Override
	protected void writeLabel(DisplayContext context, TagWriter out) throws IOException {
		if (_label != null) {
			out.writeText(context.getResources().getString(_label, _scriptName));
		} else {
			out.writeText(_scriptName);
		}
	}

	@Override
	protected String parameterName(DocumentationParameter parameter) {
		return parameter._name;
	}

	@Override
	protected String parameterType(DocumentationParameter parameter) {
		return parameter._type;
	}

	@Override
	protected String scriptName(DisplayContext context) {
		return _scriptName;
	}

	@Override
	protected boolean writeDescription(DisplayContext context, TagWriter out) throws IOException {
		if (_description != null) {
			_description.write(context, out);
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected String getReturnType() {
		return _returnType;
	}

	@Override
	protected HTMLFragment getReturnDescription() {
		if (_returnDescription != null) {
			return _returnDescription;
		} else {
			return super.getReturnDescription();
		}
	}

	@Override
	protected void writeParameterDefaultValue(DisplayContext context, TagWriter out, DocumentationParameter parameter) {
		out.writeText(parameter._defaultValue);
	}

	@Override
	protected boolean parameterMandatory(DocumentationParameter parameter) {
		return parameter._mandatory;
	}

	@Override
	protected void writeParameterDescription(DisplayContext context, TagWriter out,
			DocumentationParameter parameter) throws IOException {
		if (parameter._description != null) {
			parameter._description.write(context, out);
		}
	}

}

