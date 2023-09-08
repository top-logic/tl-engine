/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.element.layout.formeditor.definition.TLFormDefinition;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResPrefix;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.definition.FormDefinition;

/**
 * A control which creates templates for elements of a {@link TLFormDefinition} for a form editor.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public abstract class FormEditorDisplayControl extends DynamicFormDisplayControl {

	/**
	 * Whether the component is in edit mode.
	 */
	protected boolean _isInEditMode = false;

	private ResPrefix _resPrefix;

	/**
	 * @see #getType()
	 */
	private final TLStructuredType _type;

	/**
	 * Create a new {@link FormEditorDisplayControl}.
	 * 
	 * @param type
	 *        The type of the form.
	 * @param model
	 *        The {@link FormDefinition} describing the structure of the displayed form.
	 * @param resPrefix
	 *        The {@link ResPrefix} to construct a form context of.
	 * @param isInEditMode
	 *        Whether the form editor is in edit mode.
	 */
	protected FormEditorDisplayControl(TLStructuredType type, FormDefinition model, ResPrefix resPrefix,
			boolean isInEditMode) {
		super(model, null);

		_type = type;
		_resPrefix = resPrefix;
		_isInEditMode = isInEditMode;
	}

	/**
	 * The {@link TLStructuredType} to take the displayed attributes from.
	 */
	protected TLStructuredType getType() {
		return _type;
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);
		if (_isInEditMode) {
			out.append("edit");
		}
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		AttributeFormContext outerContext = new AttributeFormContext(_resPrefix);

		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);
		out.endBeginTag();
		writeContent(context, out, outerContext);
		out.endTag(DIV);
	}

	abstract void writeContent(DisplayContext context, TagWriter out, AttributeFormContext formContext)
			throws IOException;

}
