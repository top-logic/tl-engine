/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template;

import com.top_logic.html.template.TagTemplate;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.template.model.internal.TemplateControl;

/**
 * Constants for the form pattern language.
 * 
 * @deprecated Use {@link TagTemplate} with {@link TemplateControl}
 */
@Deprecated
public interface FormPatternConstants {

	/**
	 * The namespace URI for the pattern language.
	 */
	public static final String PATTERN_NS = "http://www.top-logic.com/ns/form/pattern/1.0";
	
	/**
	 * Element that references a {@link FormMember} by {@link #NAME_FIELD_ATTRIBUTE name}.
	 */
	public static final String FIELD_PATTERN_ELEMENT = "field";

	/**
	 * Element that references a {@link FormMember} by itself.
	 */
	public static final String SELF_PATTERN_ELEMENT = "self";

	/**
	 * Name attribute of a {@link #FIELD_PATTERN_ELEMENT}.
	 */
	public static final String NAME_FIELD_ATTRIBUTE = "name";

	/**
	 * Attribute that references a {@link FormMember} within a
	 * {@link FormTemplateConstants#TEMPLATE_NS} element.
	 */
	public static final String FIELD_PATTERN_ATTRIBUTE = "field";
	
	/**
	 * Attribute that adds a match expression to
	 * {@link FormTemplateConstants#TEMPLATE_NS} element.
	 */
	public static final String MATCH_PATTERN_ATTRIBUTE = "match";
}
