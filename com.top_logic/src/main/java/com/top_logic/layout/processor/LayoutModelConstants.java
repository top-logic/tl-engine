/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.processor;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.layout.editor.DynamicComponentDefinition;
import com.top_logic.mig.html.layout.ComponentName;


/**
 * Constants used during layout processing.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface LayoutModelConstants {

	/** Attribute specifying the implementation class of a component. */
	public static final String CLASS_ATTRIBUTE = "class";

	/** Start of a variable reference in a layout. */
	static final String VARIABLE_PREFIX = "${";

	/** End of a variable reference in a layout. */
	static final String VARIABLE_SUFFIX = "}";
	
	/** Start of a template parameter reference. */
	static final String PARAMETER_PREFIX = "#{";

	/** End of a template parameter reference. */
	static final String PARAMETER_SUFFIX = "}";

	/** File name suffix of XML files. */
	static final String XML_SUFFIX = ".xml";

	/** Start of a CVS <code>Id</code> keyword. */
	static final String CVS_ID_PREFIX = "$Id:";

	/** Start of a CVS <code>Revision</code> keyword. */
	static final String CVS_REVISION_PREFIX = "$Revision:";

	/** Annotation attribute holding the version of a layout definition. */
	static final String DEFINITION_VERSION_ANNOTATION_ATTR = "version";

	/** Annotation attribute marking an inlined layout. */
	static final String INLINED_ANNOTATION_ATTR = "inlined";

	/** Value of {@link #INLINED_ANNOTATION_ATTR} marking an inlined layout. */
	static final String INLINED_ANNOTATION_VALUE = "true";

	/** Element defining a window. */
	static final String WINDOW_ELEMENT = "window";

	/** Element including another layout or template. */
	static final String INCLUDE_ELEMENT = "include";
	
	/** Attribute of {@link #INCLUDE_ELEMENT} naming the included layout. */
	static final String INCLUDE_NAME = "name";

	/** Attribute of {@link #INCLUDE_ELEMENT} holding the ID of the inclusion. */
	static final String INCLUDE_ID = "id";

	/** Element injecting the value of a template parameter. */
	static final String INJECT_ELEMENT = "inject";

	/** Root element of a layout template. */
	static final String TEMPLATE_ELEMENT = "template";

	/** Element declaring the parameters of a template. */
	static final String PARAMS_ELEMENT = "params";
	
	/** Element declaring a single template parameter. */
	static final String PARAM_ELEMENT = "param";
	
	/** Attribute of {@link #PARAM_ELEMENT} holding the parameter name. */
	static final String PARAM_NAME = "name";

	/** Attribute of {@link #PARAM_ELEMENT} holding the parameter value. */
	static final String PARAM_VALUE = "value";

	/** Attribute of {@link #PARAM_ELEMENT} marking an optional parameter. */
	static final String PARAM_OPTIONAL = "optional";
	
	/** Attribute holding the name of a component. */
	public static final String COMPONENT_NAME = "name";

	/**
	 * Common suffix of files containing the layout template definition.
	 */
	String DYNAMIC_LAYOUT_TEMPLATE_FILE_SUFFIX = ".template.xml";

	/** Common suffix of files containing (complete) layout component definitions. */
	String LAYOUT_XML_FILE_SUFFIX = ".layout.xml";

	/**
	 * Common suffix of files containing an overlay for a
	 * {@link com.top_logic.mig.html.layout.LayoutComponent.Config layout config}.
	 */
	String LAYOUT_XML_OVERLAY_FILE_SUFFIX = ".layout.overlay.xml";

	/**
	 * Element defining the call of a {@link DynamicComponentDefinition dynamic template}.
	 */
	String TEMPLATE_CALL_ELEMENT = "template-call";

	/**
	 * Element holding the argument {@link ConfigurationItem} within a
	 * {@link #TEMPLATE_CALL_ELEMENT}
	 */
	String TEMPLATE_CALL_ARGUMENTS_ELEMENT = "arguments";

	/**
	 * Attribute of the element {@link #TEMPLATE_CALL_ELEMENT} that holds the name of the layout
	 * file containing the template.
	 */
	String TEMPLATE_CALL_TEMPLATE_ATTR = "template";

	/**
	 * Attribute of the element {@link #TEMPLATE_CALL_ELEMENT} that holds the the scope for the name
	 * of the new layout.
	 * 
	 * <p>
	 * The {@value LayoutModelConstants#TEMPLATE_CALL_LAYOUT_SCOPE_ATTR} attribute must be unique
	 * within the template file and can be used in the template file to reference the later
	 * instantiated component.
	 * </p>
	 * <p>
	 * The value is replaced by a generated unique name during template instantiation.
	 * </p>
	 */
	String TEMPLATE_CALL_LAYOUT_SCOPE_ATTR = "layout-scope";

	/**
	 * Value that can be used within a template file to reference the top-level component defined in
	 * that template file. Occurrences in {@link ComponentName#scope() component names} in the
	 * template file are later replaced by the name scope of the new component.
	 */
	String TEMPLATE_CALL_ENCLOSING_LAYOUT_SCOPE_VALUE = "__enclosingLayoutScope__";

	/**
	 * Attribute of the element {@link #TEMPLATE_CALL_ELEMENT}.
	 * 
	 * <p>
	 * A value "true" means, that the template call is final, i.e. no additional overlays are
	 * applied.
	 * </p>
	 * 
	 * <p>
	 * The value of the attribute is expected to be "true" or "false".
	 * </p>
	 */
	String TEMPLATE_CALL_FINAL_ATTR = "final";

}
