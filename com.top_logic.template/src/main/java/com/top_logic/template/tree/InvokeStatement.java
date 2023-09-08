/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.tree;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.template.TemplateFormat;
import com.top_logic.template.tree.parameter.ParameterValue;
import com.top_logic.template.xml.source.TemplateSource;

/**
 * A {@link TemplateNode} representing the invocation of another template.
 * 
 * It stores the {@link #getTemplateSource()} to the invoked template and the
 * {@link #getParameterValues()} for the invocation.
 * 
 * @author <a href=mailto:Jan Stolzenburg@top-logic.com>Jan Stolzenburg</a>
 */
public final class InvokeStatement extends TemplateNode {

	private final TemplateSource _templateSource;
	private final TemplateFormat _templateFormat;

	private final Map<String, ParameterValue<TemplateNode>> _parameterValues;

	/**
	 * Creates a new {@link InvokeStatement}.
	 */
	public InvokeStatement(TemplateSource templateSource, TemplateFormat templateFormat) {
		_templateSource = templateSource;
		_templateFormat = templateFormat;
		_parameterValues = new HashMap<>();
	}

	/**
	 * The {@link TemplateSource} of the invoked template.
	 */
	public TemplateSource getTemplateSource() {
		return _templateSource;
	}

	/**
	 * The {@link TemplateFormat} of the invoked template.
	 */
	public TemplateFormat getTemplateFormat() {
		return _templateFormat;
	}

	/**
	 * The parameters for the template invocation.
	 * <p>
	 * Returns the internal used map. <br/>
	 * The values are either {@link TemplateNode}s for non-structured parameter types or {@link Map
	 * maps} like this for structured parameter types.
	 * </p>
	 */
	public Map<String, ParameterValue<TemplateNode>> getParameterValues() {
		return _parameterValues;
	}

	@Override
	public <R, A> R visit(TemplateVisitor<R, A> v, A arg) {
		return v.visitInvokeStatement(this, arg);
	}

}