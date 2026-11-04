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

	/**
	 * Header of a template, containing the {@link #SETTINGS_ELEMENT settings}, the
	 * {@link #TYPES_ELEMENT type} and {@link #PARAMETERS_ELEMENT parameter} definitions.
	 */
	String HEAD_ELEMENT = "head";

	/**
	 * Body of a template, containing the template text.
	 */
	String BODY_ELEMENT = "body";

	/**
	 * Output settings of a template.
	 */
	String SETTINGS_ELEMENT = "settings";

	/**
	 * Definition of the parameters of a template.
	 */
	String PARAMETERS_ELEMENT = "parameters";

	/**
	 * Definition of a single template parameter.
	 */
	String PARAMETER_ELEMENT = "parameter";

	/**
	 * Container of the types defined in a template.
	 */
	String TYPES_ELEMENT = "types";

	/**
	 * Definition of a structured type.
	 */
	String TYPE_ELEMENT = "type";

	/**
	 * Definition of an attribute of a type, or an attribute value in a structured default value.
	 */
	String ATTRIBUTE_ELEMENT = "attribute";

	/**
	 * Name of a type, parameter or attribute.
	 */
	String NAME_ATTRIBUTE = "name";

	/**
	 * Type of a parameter or attribute.
	 */
	String TYPE_ATTRIBUTE = "type";

	/**
	 * Primitive value of an attribute or list entry in a default value.
	 */
	String VALUE_ATTRIBUTE = "value";

	/**
	 * Primitive default value of a parameter or attribute.
	 */
	String DEFAULT_ATTRIBUTE = "default";

	/**
	 * Structured or list-valued default value of a parameter or attribute.
	 */
	String DEFAULT_ELEMENT = "default";

	/**
	 * Entry of a list-valued default value.
	 */
	String ENTRY_ELEMENT = "entry";

	/**
	 * {@link #SETTINGS_ELEMENT} character encoding of the expanded template.
	 */
	String OUTPUT_ENCODING_ATTRIBUTE = "output-encoding";

	/**
	 * {@link #SETTINGS_ELEMENT} whether the expanded template starts with a byte order mark.
	 */
	String OUTPUT_BYTE_ORDER_MARK_ATTRIBUTE = "output-byte-order-mark";

	/**
	 * {@link #SETTINGS_ELEMENT} format of the expanded template.
	 */
	String OUTPUT_FORMAT_ATTRIBUTE = "output-format";

	/**
	 * {@link #SETTINGS_ELEMENT} whether the expanded template starts with an XML header.
	 */
	String OUTPUT_XML_HEADER_ATTRIBUTE = "output-xml-header";

	/**
	 * {@link #SETTINGS_ELEMENT} whether whitespace in the template body is ignored.
	 */
	String IGNORE_WHITESPACES_ATTRIBUTE = "ignore-whitespaces";

	/**
	 * Whether a parameter or attribute must have a value.
	 */
	String MANDATORY_ATTRIBUTE = "mandatory";

	/**
	 * Whether a parameter or attribute holds a list of values.
	 */
	String MULTIPLE_ATTRIBUTE = "multiple";

}
