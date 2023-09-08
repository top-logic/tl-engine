/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template.model;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.FormTemplateConstants;

/**
 * Aspect of a {@link FormMember} to render from within a template.
 * 
 * @see Templates#member(String, MemberStyle)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum MemberStyle implements ExternallyNamed {

	/**
	 * Renders the {@link FormMember} with the annotated template or using its default view provided
	 * by the context {@link ControlProvider}.
	 */
	NONE("none", null),

	/**
	 * Renders only the label aspect of a {@link FormMember}.
	 * 
	 * @see #LABEL_WITH_COLON
	 */
	LABEL(FormTemplateConstants.STYLE_LABEL_VALUE),

	/**
	 * Renders the label aspect of a {@link FormMember} appended with a ':' character.
	 * 
	 * @see #LABEL
	 */
	LABEL_WITH_COLON(FormTemplateConstants.STYLE_LABEL_WITH_COLON_VALUE),

	/**
	 * Renders only the error display of a {@link FormMember}.
	 * 
	 * @see #LABEL_WITH_COLON
	 */
	ERROR(FormTemplateConstants.STYLE_ERROR_VALUE),

	/**
	 * Renders the form member directly ignoring an annotated {@link ControlProvider}.
	 * 
	 * @see FormMember#getControlProvider()
	 */
	DIRECT(FormTemplateConstants.STYLE_DIRECT_VALUE),

	;

	private final String _externalName;

	private final String _externalStyle;

	MemberStyle(String name) {
		this(name, name);
	}

	MemberStyle(String name, String style) {
		_externalName = name;
		_externalStyle = style;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

	@Override
	public String toString() {
		return getExternalName();
	}

	/**
	 * The legacy style to use in {@link ControlProvider#createControl(Object, String)}.
	 */
	public String getControlProviderStyle() {
		return _externalStyle;
	}

	private static final Map<String, MemberStyle> _byExternalName = index();

	private static Map<String, MemberStyle> index() {
		HashMap<String, MemberStyle> result = new HashMap<>();
		for (MemberStyle style : values()) {
			result.put(style.getExternalName(), style);
		}
		result.put(null, NONE);
		return result;
	}

	/**
	 * Resolves the {@link MemberStyle} constant with the given external name.
	 * 
	 * @param name
	 *        The external name, or <code>null</code> for the default {@link #NONE} style.
	 * @return The corresponding constant or <code>null</code>, if no such constant exists.
	 */
	public static MemberStyle byExternalName(String name) {
		return _byExternalName.get(name);
	}

}
