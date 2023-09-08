/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.xml;

import com.top_logic.template.TemplateFormat;
import com.top_logic.template.tree.AssignStatement;
import com.top_logic.template.tree.DefineStatement;
import com.top_logic.template.tree.ForeachStatement;
import com.top_logic.template.tree.IfStatement;

/**
 * Constants for the XML template schema.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TemplateSchema {

	/**
	 * The namespace of template operation elements.
	 */
	String TEMPLATE_NS = "http://www.top-logic.com/ns/template/1.0";
	
	/**
	 * The namespace of template annotation attributes.
	 */
	String ANNOTATION_NS = "http://www.top-logic.com/ns/template/annotation/1.0";

	/**
	 * Group of operations.
	 */
	String TEMPLATE_ELEMENT = "template";

	/** Invocation of another template. */
	String INVOKE_ELEMENT = "invoke";

	/**
	 * The path to the template file to expand.
	 * <p>
	 * The path can be absolute or relative to the invoking template file.
	 * </p>
	 */
	String TEMPLATE_ATTRIBUTE = "template";

	/**
	 * The {@link TemplateFormat} of the element to be expanded.
	 */
	String TEMPLATE_FORMAT_ATTRIBUTE = "format";

	/**
	 * Iteration operation.
	 * 
	 * @see ForeachStatement
	 */
	String FOREACH_ELEMENT = "foreach";
	
	/**
	 * {@link #FOREACH_ELEMENT} variable name.
	 * 
	 * @see ForeachStatement#getVariable()
	 */
	String FOREACH__VAR_ATTR = "var";
	
	/**
	 * {@link #FOREACH_ELEMENT} values that are iterated.
	 * 
	 * @see ForeachStatement#getExpression()
	 */
	String FOREACH__VALUES_ATTR = "values";
	
	/**
	 * Conditional evaluation.
	 * 
	 * @see IfStatement
	 */
	String IF_ELEMENT = "if";
	
	/**
	 * {@link #IF_ELEMENT} condition to evaluate.
	 * 
	 * @see IfStatement#getCondition()
	 */
	String IF__CONDITION_ATTR = "condition";
	
	/**
	 * {@link #IF_ELEMENT} part containing the template that is evaluated, if condition yields <code>true</code>.
	 * 
	 * @see IfStatement#getThenStm()
	 */
	String THEN_ELEMENT = "then";
	
	/**
	 * {@link #IF_ELEMENT} part containing the template that is evaluated, if condition yields <code>false</code>.
	 * 
	 * @see IfStatement#getElseStm()
	 */
	String ELSE_ELEMENT = "else";
	
	/**
	 * Output of a value.
	 * 
	 * @see AssignStatement
	 */
	String VALUE_ELEMENT = "value";
	
	/**
	 * {@link #VALUE_ELEMENT} expression to output.
	 * 
	 * @see AssignStatement#getExpression()
	 */
	String VALUE__EXPR_ATTR = "expr";
	
	/**
	 * Definition of a variable.
	 * 
	 * @see DefineStatement
	 */
	String DEFINE_ELEMENT = "define";
	
	/**
	 * {@link #DEFINE_ELEMENT} defined variable name.
	 * 
	 * @see DefineStatement#getVariable()
	 */
	String DEFINE__VAR_ATTR = "var";
	
	/**
	 * {@link #DEFINE_ELEMENT} expression assign to the defined variable.
	 * 
	 * @see DefineStatement#getExpression()
	 */
	String DEFINE__EXPR_ATTR = VALUE__EXPR_ATTR;

	String HEAD_ELEMENT = "head";

	String BODY_ELEMENT = "body";

	String SETTINGS_ELEMENT = "settings";

	String PARAMETERS_ELEMENT = "parameters";

	String PARAMETER_ELEMENT = "parameter";

	String TYPES_ELEMENT = "types";

	String TYPE_ELEMENT = "type";

	String ATTRIBUTE_ELEMENT = "attribute";

	String NAME_ATTRIBUTE = "name";

	String TYPE_ATTRIBUTE = "type";

	String VALUE_ATTRIBUTE = "value";

	String DEFAULT_ATTRIBUTE = "default";

	String DEFAULT_ELEMENT = "default";

	String ENTRY_ELEMENT = "entry";

	String OUTPUT_ENCODING_ATTRIBUTE = "output-encoding";

	String OUTPUT_BYTE_ORDER_MARK_ATTRIBUTE = "output-byte-order-mark";

	String OUTPUT_FORMAT_ATTRIBUTE = "output-format";

	String OUTPUT_XML_HEADER_ATTRIBUTE = "output-xml-header";

	String IGNORE_WHITESPACES_ATTRIBUTE = "ignore-whitespaces";

	String MANDATORY_ATTRIBUTE = "mandatory";

	String MULTIPLE_ATTRIBUTE = "multiple";

}
