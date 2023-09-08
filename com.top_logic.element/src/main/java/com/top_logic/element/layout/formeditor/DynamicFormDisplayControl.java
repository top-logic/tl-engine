/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.element.layout.formeditor.definition.TLFormDefinition;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ConstantControl;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.model.TLObject;
import com.top_logic.model.form.definition.FormDefinition;

/**
 * A control which creates templates for elements of a {@link TLFormDefinition}.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public abstract class DynamicFormDisplayControl extends ConstantControl<FormDefinition> {

	/**
	 * @see #getContextModel()
	 */
	private final TLObject _contextModel;

	private static final String CSS = "cFormEditorDisplay";

	/**
	 * Creates a {@link DynamicFormDisplayControl}.
	 * @param formDefinition
	 *        The {@link FormDefinition} to display, see {@link #getModel()}.
	 * @param contextModel
	 *        See {@link #getContextModel()}.
	 */
	protected DynamicFormDisplayControl(FormDefinition formDefinition, TLObject contextModel) {
		super(formDefinition);
		_contextModel = contextModel;
	}

	/**
	 * The {@link TLObject} to take the values of.
	 */
	protected TLObject getContextModel() {
		return _contextModel;
	}

	@Override
	protected String getTypeCssClass() {
		return CSS;
	}

	/**
	 * Create a {@link Control} for the given {@link FormMember} and writes it.
	 * 
	 * @return The rendered control.
	 */
	public static Control writeTemplate(DisplayContext context, TagWriter out, FormMember member) throws IOException {
		Control ctrl = createControl(member);
		if (ctrl != null) {
			ctrl.write(context, out);
		}
		return ctrl;
	}

	/**
	 * Creates a {@link Control} to display the given {@link FormMember} using the
	 * {@link DefaultFormFieldControlProvider}.
	 */
	protected static Control createControl(FormMember member) {
		return DefaultFormFieldControlProvider.INSTANCE.createControl(member);
	}

}