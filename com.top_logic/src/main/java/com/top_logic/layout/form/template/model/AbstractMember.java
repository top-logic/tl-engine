/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template.model;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.template.ControlProvider;

/**
 * Base class for template implementations referencing {@link FormMember}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractMember extends AbstractView {

	/**
	 * Property for {@link FormMember}s.
	 */
	public static final Property<Boolean> RENDER_WHOLE_LINE =
		TypedAnnotatable.property(Boolean.class, "renderWholeLine");

	/**
	 * Property for {@link FormMember}s.
	 */
	public static final Property<String> FIRST_COLUMN_WIDTH =
		TypedAnnotatable.property(String.class, "firstColumnWidth");

	private final MemberStyle _style;

	private ControlProvider _cp;

	AbstractMember(MemberStyle style, HTMLTemplateFragment template, ControlProvider cp) {
		super(template);

		assert style != null : "No style given.";
		_style = style;
		_cp = cp;
	}

	/**
	 * How to render the field (aspect).
	 */
	public MemberStyle getStyle() {
		return _style;
	}

	/**
	 * The optional specialized {@link ControlProvider} to render this member.
	 */
	public ControlProvider getControlProvider() {
		return _cp;
	}

	/**
	 * The field name relative to the context group.
	 */
	public abstract String getName();

}
