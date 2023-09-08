/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.processor;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.layout.editor.DynamicComponentDefinition;
import com.top_logic.mig.html.layout.ComponentName;


/**
 * Constants used during layout processing.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface LayoutModelConstants {

	public static final String CLASS_ATTRIBUTE = "class";

	static final String VARIABLE_PREFIX = "${";

	static final String VARIABLE_SUFFIX = "}";
	
	static final String PARAMETER_PREFIX = "#{";

	static final String PARAMETER_SUFFIX = "}";

	static final String XML_SUFFIX = ".xml";

	static final String CVS_ID_PREFIX = "$Id:";

	static final String CVS_REVISION_PREFIX = "$Revision:";

	/**
	 * Namespace for layout annotations created by this processor.
	 */
	static final String ANNOTATION_NS = ConfigurationReader.ANNOTATION_NS;

	static final String DEFINITION_FILE_ANNOTATION_ATTR = ConfigurationReader.DEFINITION_FILE_ANNOTATION_ATTR;

	static final String DEFINITION_VERSION_ANNOTATION_ATTR = "version";

	static final String INLINED_ANNOTATION_ATTR = "inlined";

	static final String INLINED_ANNOTATION_VALUE = "true";

	static final String WINDOW_ELEMENT = "window";

	static final String INCLUDE_ELEMENT = "include";
	
	static final String INCLUDE_NAME = "name";

	static final String INCLUDE_ID = "id";

	static final String INJECT_ELEMENT = "inject";

	static final String TEMPLATE_ELEMENT = "template";

	static final String PARAMS_ELEMENT = "params";
	
	static final String PARAM_ELEMENT = "param";
	
	static final String PARAM_NAME = "name";

	static final String PARAM_VALUE = "value";

	static final String PARAM_OPTIONAL = "optional";
	
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
